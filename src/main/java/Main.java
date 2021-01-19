import audio.Music;
import hololive.HololiveTools;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Main extends ListenerAdapter {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive = new HololiveTools();
    public static JDABuilder jdabuilder = JDABuilder.createDefault("NDI1ODgxOTE5NzAwMzM2NjQy.WrHncg.EwP_DlU_iRqciL4Kn9kn9ytytUI").addEventListeners(new Main());
    public static JDA jda;
    public static void main(String args[]) {
        BotTool bottool = new BotTool();
        hololive.buildScheduleLinux();
        hololive.fillMemberList();
        hololive.fillSubCountList();
        try {
            jdabuilder.addEventListeners(bottool);
            jdabuilder.addEventListeners(hololive);
            jdabuilder.addEventListeners(new Music());
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

        if (msg.equals("!rescrape")) {
            e.getChannel().sendMessage("Rescraping Schedule and Sub Counts").queue();


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

