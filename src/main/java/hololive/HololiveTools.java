package hololive;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class HololiveTools extends ListenerAdapter {
    String apiKey = "";
    ArrayList<String> memberList = new ArrayList<String>();

    ArrayList<String> subcountList = new ArrayList<>();

    public HololiveTools(String apiKey){
        this.apiKey = apiKey;
    }

    public void fillMemberList(){
        Scanner s = null;
        try {
            s = new Scanner(new File("memberList.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (s.hasNext()){

            memberList.add(s.nextLine());
        }
        s.close();
    }



    public static String convertDate(Date date, String format, String timeZone) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (timeZone == null || "".equalsIgnoreCase(timeZone.trim())) {
            timeZone = Calendar.getInstance().getTimeZone().getID();
        }
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        return sdf.format(date);
    }

    public boolean arrayContainsString(String[] arr, String s) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.startsWith("!hlranking")){

            e.getChannel().sendMessage("Command is currently deprecated").queue();
            subcountList.removeAll(subcountList);


        }



        if (msg.startsWith("!holoschedule") || msg.startsWith("!hl all")) {


        }



        else if (msg.startsWith("!hl") &&!msg.contains("!hlsearch")&& !msg.contains("!hololive all") &&
                !msg.contains("!hl all") && !msg.contains("!hl upcoming") &&
                !msg.contains("!hololive upcoming")&&!msg.startsWith("!hlranking")) {

        }






    }
    public void logCommand(MessageReceivedEvent e, String message) {
        System.out.println(returnTimestamp() + " " + e.getAuthor() + " requested " + message);
    }
    public static String returnTimestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }

}
