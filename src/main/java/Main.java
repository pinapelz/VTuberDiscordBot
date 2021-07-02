import audio.*;
import hololive.HololiveTools;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nijisanji.NijisanjiTools;
import org.w3c.dom.ls.LSOutput;
import utilities.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends ListenerAdapter{
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive = new HololiveTools();
    static NijisanjiTools nijisanji = new NijisanjiTools();
    public static JDABuilder jdabuilder = JDABuilder.createDefault(getDiscordKey()).addEventListeners(new Main());
    public static JDA jda;
    public static BotTool bottool = new BotTool();
    public static void main(String args[]) {
        Runnable scheduleRunner = new Runnable(){
                    public void run(){
                        while(true){
                            try {
                                System.out.println("Rebuilding Nijisanji and Hololive Schedule");
                                if(System.getProperty("os.name").toString().contains("Windows")){
                                    hololive.buildSchedule();
                                }
                                else{
                                    hololive.buildScheduleLinux();
                                }
                                TimeUnit.SECONDS.sleep(180);
                            } catch (InterruptedException e) {
                                System.out.println("[ERROR]:   Error Building Schedule");
                            }
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
          //  nijisanji.buildNijiSchedule();
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
        String readToken = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings//discordToken.txt"));
            readToken = br.readLine();

        }
        catch(Exception e){
            System.out.println(readToken);
            System.out.println("Invalid Token for Bot");
            System.exit(0);
        }
        return readToken;

    }
}

