import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive  = new HololiveTools();
    public static  JDABuilder jdabuilder= JDABuilder.createDefault("NDI1ODgxOTE5NzAwMzM2NjQy.WrHncg.Sihz2o59sjvsyGpNORmkx1UvBnI").addEventListeners(new Main());
    public static JDA jda;
    public static void main(String args[]){
        PingPong ping = new PingPong();
        try {
         jdabuilder.addEventListeners(ping);
        jdabuilder.build();
            System.out.println(returnTimestamp()+ " Bot Succsessfully Started");


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to Login");
        }

    }
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();

        if(msg.startsWith("!setplaying")){
            String playingMessage = msg.replaceAll("!setplaying","");
            System.out.println(returnTimestamp()+ " Request to change playing message received");
            jdabuilder.setActivity(Activity.playing(playingMessage));
            e.getChannel().sendMessage("Playing Status Succsessfully Changed").queue();
            try {
                jdabuilder.build();
            } catch (LoginException loginException) {
                loginException.printStackTrace();
            }
        }
        else if(msg.startsWith("!setwatching")){
            String watchingMessage = msg.replaceAll("!setwatching","");
            System.out.println(returnTimestamp()+ " Request to change watching message received");
            jdabuilder.setActivity(Activity.watching(watchingMessage));
            e.getChannel().sendMessage("Watching Status Succsessfully Changed").queue();
            try {
                jdabuilder.build();
            } catch (LoginException loginException) {
                loginException.printStackTrace();
            }
        }
        if(msg.startsWith("!hololive all")||msg.startsWith("!hl all")){
            System.out.println(returnTimestamp() + " Full Schedule Requested");
            msg = msg.replaceAll("!hololive all","");
            msg = msg.replaceAll("!hl all","");
            msg = msg.replaceAll("\\s+","");
            if(msg.equals("")||msg.equals(null)){
                msg = "JST";
            }
            msg = msg.toUpperCase();
            ArrayList<Message> messages = new ArrayList<Message>();
            logCommand(e,"full hololive schedule " + msg);
            messages = hololive.getAllSchedule(msg);
            if(messages.size()==2){
                e.getChannel().sendMessage(messages.get(0)).queue();
                e.getChannel().sendMessage(messages.get(1)).queue();
            }
            else{
                e.getChannel().sendMessage(messages.get(0)).queue();
            }

        }
        if(msg.startsWith("!hl")&&!msg.contains("!hololive all")){
            msg = msg.replaceAll("!hololive","");
            msg = msg.replaceAll("!hl","");
            Scanner parser = new Scanner(msg);
            String strIndex = parser.next();
            int index = Integer.parseInt(strIndex);
            msg = msg.replaceAll(strIndex,"");
            msg = msg.replaceAll("\\s+","");
            String timezone = msg;
            timezone = timezone.toUpperCase();

            if(msg.equals("")||msg.equals(null)) {
                timezone = "JST";
            }
            logCommand(e,"hololive schedule index " + index + " in " + timezone);
            hololive.buildSchedule();
            e.getChannel().sendMessage(hololive.getSchedule(timezone,index)).queue();
        }

        if(msg.equals("!sourcecode")){
            e.getChannel().sendMessage("Source Code [Python and Java IDE needed]: https://drive.google.com/drive/folders/1vX1MTgExX7NerD9CvtsfgxScnayKwXWZ?usp=sharing").queue();
        }
    }
    public void logCommand(MessageReceivedEvent e, String message){
        System.out.println(returnTimestamp()+" " + e.getAuthor()+" requested " + message);
    }
    public static String returnTimestamp(){
        now = LocalDateTime.now();
        return "["+dtf.format(now)+"]";
    }

}

