package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;


import java.awt.*;
import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;


public class Music  extends ListenerAdapter {
    ArrayList<String> hololiveMusicURL = new ArrayList<String>();
    String apiKey = "";
    static String append = "!";

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    JDA jda;
   public Music(JDA jda, String apiKey) {
        this.musicManagers = new HashMap<>();
        this.jda = jda;
        this.apiKey = apiKey;
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
    private void populateHoloMusic(){
        try {
            URL url = new URL("https://pinapelz.github.io/vTuberDiscordBot/hololiveMusic.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            FileWriter writer = new FileWriter("data//hololiveMusic.txt");
            while ((line = in.readLine()) != null) {
                writer.write(line+"\n");
            }
            writer.close();
            in.close();
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }
    private void fillHololiveMusic(){
        populateHoloMusic();
        Scanner s = null;
        try {
            s = new Scanner(new File("data//hololiveMusic.txt"));
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
        Message message = event.getMessage();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;
        String rawMessage = message.getContentDisplay();
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if ((append+"play").equals(command[0]) && command.length == 2) {
                loadAndPlay(event.getChannel(), command[1],true);
        }
        if ((append+"splay").equals(command[0])) {
            String searchString = command[1];
            System.out.println(searchString);
            try {
                event.getChannel().sendMessage("Found Video: " +  returnTopVideoURL(searchString)).queue();
                loadAndPlay(event.getChannel(), returnTopVideoURL(searchString),true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if ((append+"leave").equals(command[0]))
        {
            guild.getAudioManager().setSendingHandler(null);
            guild.getAudioManager().closeAudioConnection();
        }
        else if ((append+"pause").equals(command[0]))
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

        else if ((append+"stop").equals(command[0]))
        {
            scheduler.queue.clear();
            player.stopTrack();
            player.setPaused(false);
            event.getChannel().sendMessage("Playback has been completely stopped and the queue has been cleared.").queue();
        }

        else if ((append+"skip").equals(command[0])) {
            skipTrack(event.getChannel());
        }

        else if ((append+"volume").equals(command[0]))
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
        else if(command[0].equals((append+"holomusic"))){
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

        else if ((append+"nowplaying").equals(command[0]) || (append+"np").equals(command[0]))
        {
            AudioTrack currentTrack = player.getPlayingTrack();
            if (currentTrack != null)
            {
                String title = currentTrack.getInfo().title;
                System.out.println(currentTrack.getIdentifier());
                String position = getTimestamp(currentTrack.getPosition());
                String duration = getTimestamp(currentTrack.getDuration());

                String nowplaying = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                        title, position, duration);
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(0x181819))
                        .setTitle("Now Playing: " + title)
                        .setDescription(currentTrack.getInfo().author)
                        .setImage("https://img.youtube.com/vi/" + currentTrack.getIdentifier() + "/hqdefault.jpg");
                embed.addField("Timestamp: ","**["+position+"/"+duration+"]**",false);
                embed.addField("", "https://www.youtube.com/watch?v=" + currentTrack.getIdentifier(), false);
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                event.getChannel().sendMessage(messageBuilder.build()).queue();
            }
            else {
                event.getChannel().sendMessage("The player is not currently playing anything!").queue();
            }
        }
        else if ((append+"list").equals(command[0])||(append+"queue").equals(command[0]))
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
                    sb.append("```Current Queue: Entries: ").append(queue.size()).append("\n");
                    for (AudioTrack track : queue)
                    {
                        queueLength += track.getDuration();
                        if (trackCount < 10)
                        {
                            sb.append("[").append(getTimestamp(track.getDuration())).append("] ");
                            sb.append(track.getInfo().title).append("\n");
                            trackCount++;
                        }
                    }
                    sb.append("\n").append("Total Queue Time Length: ").append(getTimestamp(queueLength)+"```");

                    event.getChannel().sendMessage(sb.toString()).queue();
                }
            }
        }
        else if ((append+"shuffle").equals(command[0]))
        {
            if (scheduler.queue.isEmpty())
            {
                event.getChannel().sendMessage("The queue is currently empty!").queue();
                return;
            }

            scheduler.shuffle();
            event.getChannel().sendMessage("The queue has been shuffled!").queue();
        }
        else if("!holoadd".equals(command[0])){
            addHoloSong(command[1]);
            event.getChannel().sendMessage("The url has been successfully added to the database").queue();
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
        BlockingQueue<AudioTrack> s = musicManager.scheduler.queue;

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
    private String returnTopVideoURL(String keyword) throws IOException {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q="+keyword+"&type=video&key="+apiKey;
        url = url.replaceAll(" ", "%20");
        String data = Jsoup.connect(url).ignoreContentType(true).execute().body();
        JSONObject obj = new JSONObject(data);
        JSONArray arr = obj.getJSONArray("items");
        String videoID = "";
        for (int i = 0; i < arr.length(); i++)
        {
           videoID = arr.getJSONObject(i).getJSONObject("id").getString("videoId");
            System.out.println("Parsed ID "+ videoID);
        }
        return "https://www.youtube.com/watch?v="+videoID;
    }

    private static void addHoloSong(String url){
        Writer output;
        try {
            output = new BufferedWriter(new FileWriter("data//pending.txt",true));
            output.append("\n"+url);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
