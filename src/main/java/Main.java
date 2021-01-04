import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.time.LocalDateTime;
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
        PingPong ping = new PingPong();
        try {
            jdabuilder.addEventListeners(ping);
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

        if (msg.startsWith("!setplaying")) {
            boolean allowChange = false;
            Role admin = e.getGuild().getRoleById("794482971830648843");
            for (int i = 0; i < e.getMember().getRoles().size(); i++) {
                if (e.getMember().getRoles().get(i).equals(admin)) {
                    allowChange = true;
                }
            }
            if (allowChange) {
                String playingMessage = msg.replaceAll("!setplaying", "");
                System.out.println(returnTimestamp() + " Request to change playing message received");
                jda.getPresence().setActivity(Activity.playing(playingMessage));
                e.getChannel().sendMessage("Playing Status Succsessfully Changed").queue();
            } else {
                e.getChannel().sendMessage("You have no authority to tell me what to do").queue();
            }


        } else if (msg.startsWith("!setwatching")) {
            boolean allowChange = false;
            Role admin = e.getGuild().getRoleById("794482971830648843");
            for (int i = 0; i < e.getMember().getRoles().size(); i++) {
                if (e.getMember().getRoles().get(i).equals(admin)) {
                    allowChange = true;
                }
            }
            if (allowChange) {
                String watchingMessage = msg.replaceAll("!setwatching", "");
                System.out.println(returnTimestamp() + " Request to change watching message received");
                jda.getPresence().setActivity(Activity.watching(watchingMessage));
                e.getChannel().sendMessage("Watching Status Succsessfully Changed").queue();
            } else {
                e.getChannel().sendMessage("You have no authority to tell me what to do").queue();
            }
        }

        if (msg.equals("!hololive refresh") || msg.equals("!hl refresh")) {
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            try{
            hololive.buildScheduleLinux();
            }
            catch(Exception ex){
              System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
            }
            logCommand(e, "manual hololive schedule refresh");
            e.getChannel().sendMessage("Hololive Schedule has been manually refreshed").queue();
        }
        if (msg.startsWith("!hololive all") || msg.startsWith("!hl all")) {
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            System.out.println(returnTimestamp() + " Full Schedule Requested");
            msg = msg.replaceAll("!hololive all", "");
            msg = msg.replaceAll("!hl all", "");
            msg = msg.replaceAll("\\s+", "");
            if (msg.equals("") || msg.equals(null)) {
                msg = "JST";
            }
            msg = msg.toUpperCase();
            ArrayList<Message> messages = new ArrayList<Message>();
            logCommand(e, "full hololive schedule " + msg);
            messages = hololive.getAllSchedule(msg);
            if (messages.size() == 2) {
                e.getChannel().sendMessage(messages.get(0)).queue();
                e.getChannel().sendMessage(messages.get(1)).queue();
            } else {
                e.getChannel().sendMessage(messages.get(0)).queue();
            }

        } else if (msg.startsWith("!hl") && !msg.contains("!hololive all") && !msg.contains("!hl all") && !msg.contains("!hl upcoming") && !msg.contains("!hololive upcoming")) {
            msg = msg.replaceAll("!hololive", "");
            msg = msg.replaceAll("!hl", "");
            Scanner parser = new Scanner(msg);
            String strIndex = parser.next();
            int index = Integer.parseInt(strIndex);
            msg = msg.replaceAll(strIndex, "");
            msg = msg.replaceAll("\\s+", "");
            String timezone = msg;
            timezone = timezone.toUpperCase();

            if (msg.equals("") || msg.equals(null)) {
                timezone = "JST";
            }
            logCommand(e, "hololive schedule index " + index + " in " + timezone);
            e.getChannel().sendMessage(hololive.getSchedule(timezone, index)).queue();
            try{
                hololive.buildScheduleLinux();
                }
                catch(Exception ex){
                  System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
                }
        } else if (msg.startsWith("!hl upcoming") || msg.startsWith("!hololive upcoming")) {
            System.out.println("Upcoming");
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            msg = msg.replaceAll("!hololive upcoming", "");
            msg = msg.replaceAll("!hl upcoming", "");
            Scanner parser = new Scanner(msg);
            msg = msg.replaceAll("\\s+", "");
            String timezone = msg;
            timezone = timezone.replaceAll("\\s+", "");
            timezone = timezone.toUpperCase();
            if (msg.equals("") || msg.equals(null)) {
                timezone = "JST";
            }
            logCommand(e, "hololive upcoming streams " + " in " + timezone);
            ArrayList<Message> messages = new ArrayList<Message>();
            logCommand(e, "full hololive schedule " + msg);
            try{
                hololive.buildScheduleLinux();
                }
                catch(Exception ex){
                  System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
                }
            messages = hololive.getUpcomingStreams(timezone);
            if (messages.size() == 2) {
                e.getChannel().sendMessage(messages.get(0)).queue();
                e.getChannel().sendMessage(messages.get(1)).queue();
            } else {
                e.getChannel().sendMessage(messages.get(0)).queue();
            }
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

