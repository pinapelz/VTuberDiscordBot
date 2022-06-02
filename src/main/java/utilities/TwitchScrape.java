package utilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitchScrape {
    public Boolean isLive(String twitchName){
        String html = null;
        try {
            html = Jsoup.connect("https://www.twitch.tv/"+twitchName).get().html();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        try{
            Pattern pattern = Pattern.compile("\"isLiveBroadcast\":(.*?)}");
            Matcher matcher = pattern.matcher(html);
            matcher.find();
            return true;
        }
        catch(Exception ex){
            return false;
        }
    }
    public String returnLiveData(String twitchName){
        String html = null;
        try {
            html = Jsoup.connect("https://www.twitch.tv/"+twitchName).get().html();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Document doc = Jsoup.parse(html);
        Elements scriptElements = doc.getElementsByTag("script");
        DataNode youtubeVariables = null;
        try {
            if (youtubeVariables.equals(null)) {
            }
        } catch (Exception ex) {

        }
        return scriptElements.get(0).toString();
    }

}
