import audio.*;
import hololive.HololiveTools;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nijisanji.NijisanjiTools;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.ls.LSOutput;
import utilities.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends ListenerAdapter{
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive = new HololiveTools();
    static ArrayList<Message> hololiveLive = new ArrayList<Message>();
    static ArrayList<Message> nijisanjiLive = new ArrayList<Message>();
    static NijisanjiTools nijisanji = new NijisanjiTools();
    static AutoRefreshLive autoRefresh = new AutoRefreshLive();
    public static JDABuilder jdabuilder = JDABuilder.createDefault(getDiscordKey()).addEventListeners(new Main());
    public static JDA jda;
    public static BotTool bottool = new BotTool();
    public static void main(String args[]) {

        Runnable scheduleRunner = new Runnable(){
                    public void run(){
                        if(System.getProperty("os.name").toString().contains("Windows")){
                            hololive.buildSchedule();
                        }
                        else{
                            hololive.buildScheduleLinux();
                        }
                        int hololiveRefresh = 0;
                        int nijisanjiRefresh = 0;
                        while(true){
                                if(hololiveRefresh==180){
                                    System.out.println("Rebuilding Nijisanji and Hololive Schedule");
                                    if(System.getProperty("os.name").toString().contains("Windows")){
                                        hololive.buildSchedule();
                                    }
                                    else{
                                        hololive.buildScheduleLinux();
                                    }
                                    hololiveRefresh=0;
                                }
                            if(nijisanjiRefresh==600){
                                try {
                                    autoRefresh.buildNijiSchedule();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                           hololiveRefresh++;
                            nijisanjiRefresh++;

                        }
                    }
                };

        try {
            if(System.getProperty("os.name").toString().contains("Windows")){
                hololive.buildSchedule();
            }
            else{
                hololive.buildScheduleLinux();
            }
            hololive.fillSubCountList();
            hololive.fillMemberList();
           nijisanji.buildNijiSchedule();
            jdabuilder.addEventListeners(bottool);
            jdabuilder.addEventListeners(hololive);
            jdabuilder.addEventListeners(nijisanji);
            jdabuilder.addEventListeners(new ReactRoles());
            jdabuilder.addEventListeners(new Music(jda));
            jda = jdabuilder.build();
            System.out.println(returnTimestamp() + " Bot Succsessfully Started!");

            Thread thread = new Thread(scheduleRunner);
            thread.start();

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



    public static String getDiscordKey(){
        Object obj = null;
        try {
            obj = new JSONParser().parse(new FileReader("settings//config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject jo = (JSONObject) obj;
        return (String) jo.get("discordToken");

    }
}

