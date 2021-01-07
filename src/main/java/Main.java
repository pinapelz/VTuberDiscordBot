import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive = new HololiveTools();
    public static JDABuilder jdabuilder = JDABuilder.createDefault("NDI1ODgxOTE5NzAwMzM2NjQy.WrHncg.EwP_DlU_iRqciL4Kn9kn9ytytUI").addEventListeners(new Main());
    public static JDA jda;

    public static void main(String args[]) {
        BotTool bottool = new BotTool();
        hololive.buildScheduleLinux();
        try {
            jdabuilder.addEventListeners(bottool);
            jda = jdabuilder.build();
            System.out.println(returnTimestamp() + " Bot Succsessfully Started");


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

        if (msg.startsWith("!holoen") || msg.startsWith("!hlen")) {
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            try{
                hololive.buildScheduleLinux();
            }
            catch(Exception ex){
                System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
            }
            msg = msg.replaceAll("!holoen","");
            msg = msg.replaceAll("!hlen","");
            msg = msg.replaceAll("\\s+", "");
            String timezone = msg;
            logCommand(e, "hololive EN schedule");
            ArrayList<Message> messages = new ArrayList<Message>();
            messages = hololive.holoENSchedule(timezone);
            e.getChannel().sendMessage(messages.get(0)).queue();
            hololive.buildScheduleLinux();
        }





        if (msg.equals("!sourcecode")) {
            e.getChannel().sendMessage("Source Code [Python and Java IDE needed]: https://drive.google.com/drive/folders/1vX1MTgExX7NerD9CvtsfgxScnayKwXWZ?usp=sharing").queue();
        }
    }

    public void logCommand(MessageReceivedEvent e, String message) {
        System.out.println(returnTimestamp() + " " + e.getAuthor() + " requested " + message);
    }

    public static String returnTimestamp() {
        now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }


}

