package utilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeScrape {
    public String getVideoIDFromChannelID(String channelID) throws IOException {
        String html = Jsoup.connect("https://www.youtube.com/channel/"+channelID+"/live").get().html();
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
        try{
            Pattern pattern = Pattern.compile("\"videoId\":\"(.*?)\",");
            Matcher matcher = pattern.matcher(youtubeVariables.toString());
            matcher.find();
            return matcher.group(1);
        }
        catch(Exception e){

        }
        return "Error with ID";
    }
    public String getTitleFromChannelID(String channelID) throws IOException {
        String html = Jsoup.connect("https://www.youtube.com/channel/"+channelID+"/live").get().html();
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
        try{
            Pattern pattern = Pattern.compile("\"title\":\"(.*?)\",");
            Matcher matcher = pattern.matcher(youtubeVariables.toString());
            matcher.find();
            return matcher.group(1);
        }
        catch(Exception e){

        }
        return "Error with ID";
    }
}
