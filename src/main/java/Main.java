import audio.*;
import hololive.HololiveTools;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nijisanji.NijisanjiTools;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utilities.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends ListenerAdapter {
    private static ArrayList<Message> currentlyLiveHoloQueue = new ArrayList<Message>();
    private static ArrayList<Message> currentlyLiveNijiQueue = new ArrayList<Message>();
    private static ArrayList<Message> currentlyLiveMiscQueue = new ArrayList<Message>();
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private static LocalDateTime now = LocalDateTime.now();
    private static HololiveTools hololive = new HololiveTools();
    private static NijisanjiTools nijisanji = new NijisanjiTools();
    private static AutoRefreshLive autoRefresh = new AutoRefreshLive();
    public static JDABuilder jdabuilder = JDABuilder.createDefault(readSetting("discordToken")).addEventListeners(new Main());
    public static JDA jda;
    public static BotTool bottool = new BotTool();

    public static void main(String args[]) {
        try {
            jdabuilder.addEventListeners(bottool);
            jdabuilder.addEventListeners(nijisanji);
            jdabuilder.addEventListeners(hololive);
            autoRefresh.updateFileFromSite("nijiTwitchID.txt");
            autoRefresh.updateFileFromSite("holoTwitchID.txt");
            autoRefresh.updateFileFromSite("miscTwitchID.txt");
            autoRefresh.updateFileFromSite("miscMemberID.txt");
            currentlyLiveHoloQueue = autoRefresh.getCurrentlyLiveMessage(autoRefresh.getCurrentlyLiveYoutubeChannels("hololive", "holo"),
                    autoRefresh.getCurrentlyLiveTwitchChannels("hololive","holo"));
            currentlyLiveNijiQueue = autoRefresh.getCurrentlyLiveMessage(autoRefresh.getCurrentlyLiveYoutubeChannels("nijisanji","niji"),
                    autoRefresh.getCurrentlyLiveTwitchChannels("nijisanji","niji"));
            currentlyLiveMiscQueue = autoRefresh.getCurrentlyLiveMessage(autoRefresh.getCurrentlyLiveYoutubeChannels("others","misc"),
                    autoRefresh.getCurrentlyLiveTwitchChannels("others","misc"));
            autoRefresh.buildSchedule("hololive","holo");
            autoRefresh.buildSchedule("nijisanji","niji");
            jdabuilder.addEventListeners(new Music(jda,readSetting("youtubeApi")));
            Runnable runnable = () -> {
                autoRefresh();
            };
            Thread thread = new Thread(runnable);
            thread.start();
            jda = jdabuilder.build();
            System.out.println(returnTimestamp() + " Bot Succsessfully Started!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to Login");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.startsWith("!forcebuild")){
            e.getChannel().sendMessage("Understood. Refreshing now!").queue();
            try {
                autoRefresh.buildSchedule("nijisanji","niji");
                autoRefresh.buildSchedule("hololive","holo");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.getChannel().sendMessage("Build complete!").queue();
        }
    }

    public void logCommand(MessageReceivedEvent e, String message) {
        System.out.println(returnTimestamp() + " " + e.getAuthor() + " requested " + message);
    }

    public static String returnTimestamp() {
        now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }

    public static String readSetting(String parameter) {
        Object obj = null;
        try {
            obj = new JSONParser().parse(new FileReader("settings//config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject jo = (JSONObject) obj;
        return (String) jo.get(parameter);

    }

    private static void autoRefresh() {
        int refreshLiveTimer = 0;
        int refreshUpcomingTimer = 0;
        while (true) {

            try {
                TimeUnit.MINUTES.sleep(1);
                if (refreshUpcomingTimer >=15) {
                    System.out.println("15 Minutes Passed. Refreshing Live Status");
                    autoRefresh.buildSchedule("hololive", "holo");
                    autoRefresh.buildSchedule("nijisanji", "niji");
                    refreshUpcomingTimer= 0;


                } else if (refreshLiveTimer>=7) { //using a range in case its caught in some other tasl
                    System.out.println("7 Minutes Passed. Refreshing Live Status");
                    currentlyLiveHoloQueue = autoRefresh.getCurrentlyLiveMessage(autoRefresh.getCurrentlyLiveYoutubeChannels("hololive", "holo"),
                            autoRefresh.getCurrentlyLiveTwitchChannels("hololive","holo"));
                    currentlyLiveNijiQueue = autoRefresh.getCurrentlyLiveMessage(autoRefresh.getCurrentlyLiveYoutubeChannels("nijisanji","niji"),
                            autoRefresh.getCurrentlyLiveTwitchChannels("nijisanji","niji"));
                    currentlyLiveMiscQueue = autoRefresh.getCurrentlyLiveMessage(autoRefresh.getCurrentlyLiveYoutubeChannels("others","misc"),
                            autoRefresh.getCurrentlyLiveTwitchChannels("others","misc"));
                    refreshNijisanjiCurrLive();
                    refreshHololiveCurrLive();
                    refreshOthersCurrLive();
                  refreshLiveTimer = 0;

                }

            } catch (Exception e) {
            }
            refreshUpcomingTimer++;
            refreshLiveTimer++;
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        try{
            refreshHololiveCurrLive();
            refreshNijisanjiCurrLive();
           refreshOthersCurrLive();
            System.out.println("All Processes Finished Loading");
        }
        catch(Exception e){

        }


    }
    public static void refreshOthersCurrLive() {

        List<TextChannel> channels = jda.getTextChannelsByName("misc-live", true);
        for (TextChannel textChannel : channels) {
            textChannel.createCopy().queue();
            textChannel.delete().queue();
        }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<TextChannel> freshChannel = jda.getTextChannelsByName("misc-live", true);
        for (TextChannel textChannel : freshChannel) {
            for (int i = 0; i < currentlyLiveMiscQueue.size(); i++) {
                try {
                    textChannel.sendMessage(currentlyLiveMiscQueue.get(i)).queue();
                }
                catch (Exception e){

                }
            }
        }

    }
    public static void refreshNijisanjiCurrLive() {

            List<TextChannel> channels = jda.getTextChannelsByName("nijisanji-live", true);
            for (TextChannel textChannel : channels) {
                textChannel.createCopy().queue();
                textChannel.delete().queue();
            }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<TextChannel> freshChannel = jda.getTextChannelsByName("nijisanji-live", true);
            for (TextChannel textChannel : freshChannel) {
                for (int i = 0; i < currentlyLiveNijiQueue.size(); i++) {
                    try {
                        textChannel.sendMessage(currentlyLiveNijiQueue.get(i)).queue();
                    }
                    catch (Exception e){

                    }
                }
            }

    }
    public static void refreshHololiveCurrLive() {
            List<TextChannel> channels = jda.getTextChannelsByName("hololive-live", true);
            for (TextChannel textChannel : channels) {
                textChannel.createCopy().queue();
                textChannel.delete().queue();
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<TextChannel> freshChannel = jda.getTextChannelsByName("hololive-live", true);
            for (TextChannel textChannel : freshChannel) {
                for (int i = 0; i < currentlyLiveHoloQueue.size(); i++) {
                    try {
                        textChannel.sendMessage(currentlyLiveHoloQueue.get(i)).queue();
                    }
                    catch(Exception e){

                    }
                }
            }


        }


}

