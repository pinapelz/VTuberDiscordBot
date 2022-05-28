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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends ListenerAdapter{
    private static ArrayList<Message> currentlyLiveHoloQueue = new ArrayList<Message>();
    private static ArrayList<Message> currentlyLiveNijiQueue = new ArrayList<Message>();
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
            //currentlyLiveNijiQueue = autoRefresh.getCurrentlyLiveMessage("979833458165678100",autoRefresh.getCurrentlyLiveChannels("nijisanji","niji"));
            autoRefresh.buildSchedule("hololive","holo");
            autoRefresh.buildSchedule("nijisanji","niji");
            jdabuilder.addEventListeners(new Music(jda));
            Runnable runnable = () -> { autoRefresh();};
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


    }
    public void logCommand(MessageReceivedEvent e, String message) {
        System.out.println(returnTimestamp() + " " + e.getAuthor() + " requested " + message);
    }
    public static String returnTimestamp() {
        now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }

    public static String readSetting(String parameter){
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
    private static void autoRefresh(){

        int minutesElapsed = 0;
        while(true){
            try {

                TimeUnit.MINUTES.sleep(1);
                minutesElapsed++;
                if(minutesElapsed == 20){
                    autoRefresh.buildSchedule("hololive","holo");
                    autoRefresh.buildSchedule("nijisanji","niji");
                    minutesElapsed = 0;
                }
                else if(minutesElapsed == 10){
                    TextChannel textChannel = jda.getTextChannelById("794409510063308820");
                    textChannel.sendMessage("testMethod").queue();
                }

            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }

        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        TextChannel textChannel = jda.getTextChannelById("794409510063308820");
        textChannel.sendMessage("testMethod").queue();
    }


}

