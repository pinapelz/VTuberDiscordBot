package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Music  extends ListenerAdapter {
    ArrayList<String> hololiveMusicURL = new ArrayList<String>();
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

   public Music() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
       System.out.println("Filling Hololive Music List");

    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
    private void fillHololiveMusic(){
        Scanner s = null;
        try {
            s = new Scanner(new File("holoCli/hololiveMusic.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (s.hasNext()){
            hololiveMusicURL.add(s.nextLine());
        }
        s.close();
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if ("!play".equals(command[0]) && command.length == 2) {
            loadAndPlay(event.getChannel(), command[1],true);
        }
        else if ("!leave".equals(command[0]))
        {
            guild.getAudioManager().setSendingHandler(null);
            guild.getAudioManager().closeAudioConnection();
        }
        else if ("!pause".equals(command[0]))
        {
            if (player.getPlayingTrack() == null)
            {
                event.getChannel().sendMessage("Cannot pause or resume player because no track is loaded for playing.").queue();
                return;
            }

            player.setPaused(!player.isPaused());
            if (player.isPaused())
                event.getChannel().sendMessage("The player has been paused.").queue();
            else
                event.getChannel().sendMessage("The player has resumed playing.").queue();
        }

        else if ("!stop".equals(command[0]))
        {
            scheduler.queue.clear();
            player.stopTrack();
            player.setPaused(false);
            event.getChannel().sendMessage("Playback has been completely stopped and the queue has been cleared.").queue();
        }

        else if ("!skip".equals(command[0])) {
            skipTrack(event.getChannel());
        }

        else if ("!volume".equals(command[0]))
        {
            if (command.length == 1)
            {
                event.getChannel().sendMessage("Current player volume: **" + player.getVolume() + "**").queue();
            }
            else
            {
                try
                {
                    int newVolume = Math.max(10, Math.min(100, Integer.parseInt(command[1])));
                    int oldVolume = player.getVolume();
                    player.setVolume(newVolume);
                    event.getChannel().sendMessage("Player volume changed from `" + oldVolume + "` to `" + newVolume + "`").queue();
                }
                catch (NumberFormatException e)
                {
                    event.getChannel().sendMessage("`" + command[1] + "` is not a valid integer. (10 - 100)").queue();
                }
            }
        }
        else if("!holomusic".startsWith(command[0])){
            fillHololiveMusic();
            Collections.shuffle(hololiveMusicURL);
            int songsToQueue = Integer.parseInt(command[1]);
            System.out.println("Requesting to queue " + command[1] + " songs");
            System.out.println("Queueing all Hololive Music");
            event.getChannel().sendMessage("Queueing " + command[1] + " Hololive songs").queue();
            for (int i = 0;i<songsToQueue;i++){
                loadAndPlay(event.getChannel(), hololiveMusicURL.get(i),false);
            }


        }

        else if ("!nowplaying".equals(command[0]) || "!np".equals(command[0]))
        {
            AudioTrack currentTrack = player.getPlayingTrack();
            if (currentTrack != null)
            {
                String title = currentTrack.getInfo().title;
                String position = getTimestamp(currentTrack.getPosition());
                String duration = getTimestamp(currentTrack.getDuration());

                String nowplaying = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                        title, position, duration);

                event.getChannel().sendMessage(nowplaying).queue();
            }
            else
                event.getChannel().sendMessage("The player is not currently playing anything!").queue();
        }
        else if ("!list".equals(command[0]))
        {
            Queue<AudioTrack> queue = scheduler.queue;
            synchronized (queue)
            {
                if (queue.isEmpty())
                {
                    event.getChannel().sendMessage("The queue is currently empty!").queue();
                }
                else
                {
                    int trackCount = 0;
                    long queueLength = 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Current Queue: Entries: ").append(queue.size()).append("\n");
                    for (AudioTrack track : queue)
                    {
                        queueLength += track.getDuration();
                        if (trackCount < 10)
                        {
                            sb.append("`[").append(getTimestamp(track.getDuration())).append("]` ");
                            sb.append(track.getInfo().title).append("\n");
                            trackCount++;
                        }
                    }
                    sb.append("\n").append("Total Queue Time Length: ").append(getTimestamp(queueLength));

                    event.getChannel().sendMessage(sb.toString()).queue();
                }
            }
        }
        else if ("!shuffle".equals(command[0]))
        {
            if (scheduler.queue.isEmpty())
            {
                event.getChannel().sendMessage("The queue is currently empty!").queue();
                return;
            }

            scheduler.shuffle();
            event.getChannel().sendMessage("The queue has been shuffled!").queue();
        }

        super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl,boolean returnMessage) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(returnMessage) {
                    channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                }

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                if(returnMessage){
                    channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
                }


                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if(returnMessage) {
                    channel.sendMessage("Could not play: " + exception.getMessage()).queue();
                }
            }
        });
    }


    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
       //Reference MP3 Here in the Future?
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }
    private static String getTimestamp(long milliseconds)
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours   = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }


}
