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
    static ArrayList<String> listOfKeys = new ArrayList<String>(keySet); //names
    static Collection<String> values = memberIDMap.values(); //ids
    static ArrayList<String> listOfValues = new ArrayList<String>(values);
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.startsWith("!nijischedule")){
            e.getChannel().sendMessage("Scraping the ranking page. Thank you for your patience").queue();
        }
    }
    public static HashMap<String, String> memberChannelID(){
        String delimiter = ":";
        HashMap<String, String> map = new HashMap<>();
        try(Stream<String> lines = Files.lines(Paths.get("nijiMemberID.txt"))){
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[0]
                    , line.split(delimiter)[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}

