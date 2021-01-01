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
    HololiveTools hololive  = new HololiveTools();
    public static  JDABuilder jdabuilder= JDABuilder.createDefault("APIKEY").addEventListeners(new Main());
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
            messages = hololive.getAllSchedule(msg);
            //e.getChannel().sendMessage( hololive.getAllScheduleOld(msg)).queue();
            if(messages.size()==2){
                e.getChannel().sendMessage(messages.get(0)).queue();
                e.getChannel().sendMessage(messages.get(1)).queue();
            }
            else{
                e.getChannel().sendMessage(messages.get(0)).queue();
            }

        }
        if(msg.startsWith("!hololive")||msg.startsWith("!hl")&&!msg.contains("!hololive all")){
            msg = msg.replaceAll("!hololive","");
            msg = msg.replaceAll("!hl","");
            Scanner parser = new Scanner(msg);
            String strIndex = parser.next();
            System.out.println(strIndex);
            int index = Integer.parseInt(strIndex);
            msg = msg.replaceAll(strIndex,"");
            msg = msg.replaceAll("\\s+","");
            String timezone = msg;
            if(msg.equals("")||msg.equals(null)) {
                timezone = "JST";
            }
            hololive.buildSchedule();
            e.getChannel().sendMessage(hololive.getSchedule(timezone,index)).queue();
        }
    }
    public static String returnTimestamp(){
        now = LocalDateTime.now();
        return "["+dtf.format(now)+"]";
    }
}

