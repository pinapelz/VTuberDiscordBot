package nijisanji;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.List;

public class NijisanjiTools extends ListenerAdapter {
    static HashMap<String, String> memberIDMap = memberChannelID();
    static Set<String> keySet = memberIDMap.keySet();
    static Collection<String> values = memberIDMap.values(); //ids
    static ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
    static ArrayList<String> listOfValues = new ArrayList<String>(keySet);
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.startsWith("!nijischedule")){
            try {
                buildNijiSchedule();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.getChannel().sendMessage("Scraping Niisanji channels. Thank you for your patience").queue();
        }
    }
    public static HashMap<String, String> memberChannelID(){
        String delimiter = ":";
        HashMap<String, String> map = new HashMap<>();
        try(Stream<String> lines = Files.lines(Paths.get("data//nijiMemberID.txt"))){
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[0]
                    , line.split(delimiter)[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    public static void buildNijiSchedule() throws IOException {
        for (int i = 0;i<listOfValues.size();i++) {
            String html = Jsoup.connect("https://www.youtube.com/channel/"+listOfValues.get(i)+"/live").get().html();
            Document doc = Jsoup.parse(html);
            Elements scriptElements = doc.getElementsByTag("script");
            DataNode youtubeVariables = null;
            for (Element element : scriptElements) {
                for (DataNode node : element.dataNodes()) {
                    if (element.data().contains("var ytInitialPlayerResponse")) {
                        youtubeVariables = node;
                    }
                }
            }
            try {
                if (youtubeVariables.equals(null)) {
                }
            } catch (Exception e) {
                System.out.println(listOfKeys.get(i)+" doesn't have a stream scheduled");
            }
            try {
                Pattern pattern = Pattern.compile("\"scheduledStartTime\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(youtubeVariables.toString());

                String unixTime = "0";
                if (matcher.find()) {
                    unixTime = matcher.group(1);
                }
                Date date = new java.util.Date(Integer.parseInt(unixTime) * 1000L);
                Date currentDate = new Date();
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-7"));
                long difference_In_Time = date.getTime() - currentDate.getTime();
                int difference_In_Days = (int) Math.abs(difference_In_Time / (8.64 * Math.pow(10, 7))); //conversion of ms to days
                if(difference_In_Days<5&&difference_In_Days>=0){
                    System.out.println("Diff. " + difference_In_Days+ "       " + listOfKeys.get(i)+ " will be streaming at " + sdf.format(date));
                    Files.write(Paths.get("test.txt"),("\n"+listOfKeys.get(i)+ ":" + sdf.format(date)).getBytes(), StandardOpenOption.APPEND);
                }
                else{
                    System.out.println(listOfKeys.get(i)+" doesn't have a stream scheduled");
                }

            } catch (Exception e) {


            }
        }
    }
}

