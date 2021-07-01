import audio.*;
import hololive.HololiveTools;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nijisanji.NijisanjiTools;
import org.w3c.dom.ls.LSOutput;
import utilities.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class Main extends ListenerAdapter {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive = new HololiveTools();

    public static JDABuilder jdabuilder = JDABuilder.createDefault(getDiscordKey()).addEventListeners(new Main());
    public static JDA jda;
    public static BotTool bottool = new BotTool();
    public static void main(String args[]) {

        hololive.buildScheduleLinux();
        hololive.fillSubCountList();
        try {
            jdabuilder.addEventListeners(bottool);
            jdabuilder.addEventListeners(hololive);
            jdabuilder.addEventListeners(new ReactRoles());
            jdabuilder.addEventListeners(new Music(jda));
            jda = jdabuilder.build();
            System.out.println(returnTimestamp() + " Bot Succsessfully Started!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to Login");
        }
      /*  private String[] messages = { "message 1", "message 2" };
        private int currentIndex = 0;
        private ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
//run this once
        threadPool.scheduleWithFixedDelay(() -> {
            jda.getPresence().setActivity(Activity.playing(messages[currentIndex]));
            currentIndex = (currentIndex + 1) % messages.length;
        }, 0, 30, TimeUnit.SECONDS);
//when you want to stop it (e.g. when the bot is stopped)
        threadPool.shutdown();*/

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

