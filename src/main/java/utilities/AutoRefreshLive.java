package utilities;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
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
    TwitchScrape twitch = new TwitchScrape();
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static void buildSchedule(String group,String nickName) throws IOException {
        HashMap<String, String> memberIDMap = fillHashMap("data//"+nickName+"MemberID.txt");
        Set<String> keySet = memberIDMap.keySet();
        Collection<String> values = memberIDMap.values(); //ids
        ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
        ArrayList<String> listOfValues = new ArrayList<String>(keySet);
        ArrayList<String> schedule = new ArrayList<String>();
        for (int i = 0; i < listOfValues.size(); i++) {
            System.out.println("Now Building Schedule for: " + listOfKeys.get(i)+" "+(i+1)+" of "+listOfKeys.size());
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
                    schedule.add(("\n" + listOfKeys.get(i) + ":" + unixTime));

                } else {
                }

            } catch (Exception e) {
            }
        }

        PrintWriter writer = new PrintWriter("data//"+group+".txt"); //Clearing the text file
        writer.print("");
        writer.close();
        for (int i = 0;i<schedule.size();i++){
            Files.write(Paths.get("data//"+group+".txt"), schedule.get(i).getBytes(), StandardOpenOption.APPEND);
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
    public static void updateFileFromSite(String fileName){
        try {
            URL url = new URL("https://pinapelz.github.io/vTuberDiscordBot/"+fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            FileWriter writer = new FileWriter("data//"+fileName);
            while ((line = in.readLine()) != null) {
                writer.write(line+"\n");
            }
            writer.close();
            in.close();
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        String delimiter = ":";
        HashMap<String, String> map = new HashMap<>();
        try(Stream<String> lines = Files.lines(Paths.get("data//"+fileName))){
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[0]
                            , line.split(delimiter)[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Message> getCurrentlyLiveMessage(ArrayList<String> currentlyLiveYTData,ArrayList<String> currentlyLiveTwitchData){
        ArrayList<Message> messageQueue = new ArrayList<Message>();
        //Scrape through all the youtube links
        for(int i = 0;i< currentlyLiveYTData.size();i++){
            try {
                LocalDateTime now = LocalDateTime.now();
                Pattern idPattern = Pattern.compile("\"id\":\"(.*?)\",");
                Matcher idMatcher = idPattern.matcher(currentlyLiveYTData.get(i));
                Pattern namePattern = Pattern.compile("\"english_name\":\"(.*?)\"}}");
                Matcher nameMatcher = namePattern.matcher(currentlyLiveYTData.get(i));
                Pattern titlePattern = Pattern.compile("\"title\":\"(.*?)\",");
                Matcher titleMatcher = titlePattern.matcher(currentlyLiveYTData.get(i));
                idMatcher.find();
                nameMatcher.find();
                titleMatcher.find();
                EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/2560px-YouTube_full-color_icon_%282017%29.svg.png")
                        .setColor(new Color(0x181819))
                        .setFooter("Retreived at " + dtf.format(now) + "- DS","https://64.media.tumblr.com/c4de87a6b466871a11f0f428efc2fccb/bca622fcfec2b690-cd/s400x600/802ad91ca198b58ef8c54bc6f7cd704f9528d7a3.jpg")
                        .setTitle(nameMatcher.group(1) + " is Live!")
                        .setDescription(titleMatcher.group(1))
                        .setImage("https://img.youtube.com/vi/" + idMatcher.group(1) + "/hqdefault.jpg");
                embed.addField("Link", "https://www.youtube.com/watch?v=" + idMatcher.group(1), false);
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                messageQueue.add(messageBuilder.build());
            }
            catch(Exception e){
                System.out.println(e);
            }
        }
        for(int i = 0;i< currentlyLiveTwitchData.size();i++){
            try {
                System.out.println("Processing: " + currentlyLiveTwitchData.get(i));
                LocalDateTime now = LocalDateTime.now();
                Pattern namePattern = Pattern.compile("\"name\":\"(.*?) - Twitch\",");
                Matcher nameMatcher = namePattern.matcher(currentlyLiveTwitchData.get(i));
                Pattern titlePattern = Pattern.compile("\"description\":\"(.*?)\",");
                Matcher titleMatcher = titlePattern.matcher(currentlyLiveTwitchData.get(i));
                nameMatcher.find();
                System.out.println(nameMatcher.group(1)+" wigouwogjwigwr");
                titleMatcher.find();

                EmbedBuilder embed = new EmbedBuilder().setThumbnail("http://assets.stickpng.com/images/580b57fcd9996e24bc43c540.png")
                        .setColor(new Color(0x181819))
                        .setFooter("Retreived at " + dtf.format(now) + "- DS","https://64.media.tumblr.com/c4de87a6b466871a11f0f428efc2fccb/bca622fcfec2b690-cd/s400x600/802ad91ca198b58ef8c54bc6f7cd704f9528d7a3.jpg")
                        .setTitle(nameMatcher.group(1) + " is Live!")
                        .setDescription(titleMatcher.group(1))
                        .setImage("https://static-cdn.jtvnw.net/previews-ttv/live_user_"+nameMatcher.group(1).toLowerCase()+"-640x360.jpg");
                embed.addField("Link", "https://www.twitch.tv/" + nameMatcher.group(1).toLowerCase(), false);
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                messageQueue.add(messageBuilder.build());
            }
            catch(Exception e){
                System.out.println("Error with scrape");
                System.out.println(e);
            }
        }

        return messageQueue;

    }
    public ArrayList<String> getCurrentlyLiveYoutubeChannels(String group, String nickName) throws FileNotFoundException {
        ArrayList<String> currLiveData = new ArrayList<String>();
        HashMap<String, String> memberIDMap = fillHashMap("data//"+nickName+"MemberID.txt");
        Set<String> keySet = memberIDMap.keySet();
        Collection<String> values = memberIDMap.values(); //ids
        ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
        ArrayList<String> listOfValues = new ArrayList<String>(keySet);
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
    public ArrayList<String> getCurrentlyLiveTwitchChannels(String group, String nickName) throws FileNotFoundException {
        ArrayList<String> currLiveData = new ArrayList<String>();
        HashMap<String, String> memberIDMap = fillHashMap("data//"+nickName+"TwitchID.txt");
        Set<String> keySet = memberIDMap.keySet();
        Collection<String> values = memberIDMap.values(); //ids
        ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
        ArrayList<String> listOfValues = new ArrayList<String>(keySet);
        for (int i = 0;i<listOfValues.size();i++){
            if(twitch.isLive(listOfValues.get(i))){
                currLiveData.add(twitch.returnLiveData(listOfValues.get(i)));
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