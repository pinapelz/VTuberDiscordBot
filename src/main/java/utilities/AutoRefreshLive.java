package utilities;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import holodex.HolodexApi;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AutoRefreshLive {
    HolodexApi holodex = new HolodexApi();
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static void buildSchedule(String group,String nickName) throws IOException {
        HashMap<String, String> memberIDMap = fillHashMap("data//"+nickName+"MemberID.txt");
        Set<String> keySet = memberIDMap.keySet();
        Collection<String> values = memberIDMap.values(); //ids
        ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
        ArrayList<String> listOfValues = new ArrayList<String>(keySet);
        PrintWriter writer = new PrintWriter("data//"+group+".txt"); //Clearing the text file
        writer.print("");
        writer.close();
        for (int i = 0; i < listOfValues.size(); i++) {
            System.out.println("Building " + group+ " schedule for " + listOfKeys.get(i) + "  " + (i+1) +" of " + listOfValues.size());
            String unixTime = "0";
            String html = Jsoup.connect("https://www.youtube.com/channel/" + listOfValues.get(i) + "/live").get().html();
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

            }
            try {
                Pattern pattern = Pattern.compile("\"scheduledStartTime\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(youtubeVariables.toString());
                if (matcher.find()) {
                    unixTime = matcher.group(1);
                }
                Date date = new java.util.Date(Integer.parseInt(unixTime) * 1000L);
                Date currentDate = new Date();
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-7"));
                long difference_In_Time = date.getTime() - currentDate.getTime();
                int difference_In_Days = (int) Math.abs(difference_In_Time / (8.64 * Math.pow(10, 7))); //conversion of ms to days
                if (difference_In_Days < 5 && difference_In_Days >= 0) {

                    Files.write(Paths.get("data//"+group+".txt"), ("\n" + listOfKeys.get(i) + ":" + unixTime).getBytes(), StandardOpenOption.APPEND);
                } else {
                }

            } catch (Exception e) {


            }
        }
        removeBlankLines("data//"+group+".txt");
    }

    private static void removeBlankLines(String filename) {
        try {
            List<String> lines = FileUtils.readLines(new File(filename), "UTF-8");

            Iterator<String> i = lines.iterator();
            while (i.hasNext()) {
                String line = i.next();
                if (line.trim().isEmpty())
                    i.remove();
            }

            FileUtils.writeLines(new File(filename), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Message> getCurrentlyLiveMessage(String discordChannelID,ArrayList<String> currentlyLiveData){
        ArrayList<Message> messageQueue = new ArrayList<Message>();
        String videoId = "";
        String title = "";
        String name = "";
        for(int i = 0;i< currentlyLiveData.size();i++){
            LocalDateTime now = LocalDateTime.now();
            Pattern idPattern = Pattern.compile("\"id\":\"(.*?)\",");
            Matcher idMatcher = idPattern.matcher(currentlyLiveData.get(i).toString());
            Pattern namePattern = Pattern.compile("\"english_name\":\"(.*?)\"}");
            Matcher nameMatcher = namePattern.matcher(currentlyLiveData.get(i).toString());
            Pattern titlePattern = Pattern.compile("\"title\":\"(.*?)\",");
            Matcher titleMatcher = titlePattern.matcher(currentlyLiveData.get(i).toString());
            idMatcher.find();
            nameMatcher.find();
            titleMatcher.find();
            EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://pbs.twimg.com/profile_images/1335777549343883264/rVsyH8Jo.jpg").setColor(new Color(0x181819))
                    .setFooter("Retreived at " + dtf.format(now) + "- DS",
                            "https://img.discogs.com/B416C4GICJEQPsATudXjk95wJbo=/fit-in/300x300/filters:strip_icc():format(jpeg):mode_rgb():quality(40)/discogs-images/L-1773362-1582250333-9292.jpeg.jpg")
                    .setTitle(nameMatcher.group(1)+" is Live!")
                    .setDescription(titleMatcher.group(1))
                    .setImage("https://img.youtube.com/vi/"+idMatcher.group(1)+"/hqdefault.jpg");
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
            messageQueue.add(messageBuilder.build());
            System.out.println(idMatcher.group(1)+"   " + nameMatcher.group(1)+"   "+titleMatcher.group(1));
        }
        return messageQueue;

    }
    public ArrayList<String> getCurrentlyLiveChannels(String group, String nickName) throws FileNotFoundException {
        ArrayList<String> currLiveData = new ArrayList<String>();
        HashMap<String, String> memberIDMap = fillHashMap("data//"+nickName+"MemberID.txt");
        Set<String> keySet = memberIDMap.keySet();
        Collection<String> values = memberIDMap.values(); //ids
        ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
        ArrayList<String> listOfValues = new ArrayList<String>(keySet);
        PrintWriter writer = new PrintWriter("data//"+group+".txt"); //Clearing the text file
        writer.print("");
        writer.close();
       for (int i = 0;i<listOfValues.size();i++){
           if (holodex.isLiveData(listOfValues.get(i)).equals("[]")) { //Condition of if channel is not live
               //Do nothing
           }
           else{
               currLiveData.add(holodex.isLiveData(listOfValues.get(i)));
           }
       }
        for (int i = 0;i<currLiveData.size();i++){ //Final clean up to get rid of blank lines and random empty cells
            //This shouldn't even need to happen but I guess when the API messes up this will fix it
            if(currLiveData.get(i).equals("[]")||currLiveData.get(i).equals("")){
                currLiveData.remove(i);
            }

        }

        return currLiveData;
    }


    private static HashMap<String, String> fillHashMap(String file) {
        String delimiter = ":";
        HashMap<String, String> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get(file))) {
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[0]
                            , line.split(delimiter)[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

}