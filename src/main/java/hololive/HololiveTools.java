package hololive;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utilities.NumericalStringComparator;

public class HololiveTools extends ListenerAdapter {
    final String apiKey = "AIzaSyBGi44EH2qpW7_8ENH6RB32r1HyZLpe_7k";
    final String yagooChannelID = "UCu2DMOGLeR_DSStCyeQpi5Q";
    ArrayList<String> memberList = new ArrayList<String>();
    HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
        public void initialize(HttpRequest request) throws IOException {
        }
    };
    ArrayList<String> subcountList = new ArrayList<>();

    YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequestInitializer).setApplicationName("RikoBot").build();

    ArrayList<String> schedule = new ArrayList<String>();
    String[] validTimezones = {"GMT", "UTC", "ECT", "EET", "ART", "EAT", "MET", "NET",
            "PLT", "IST", "BST", "VST", "CTT", "JST", "ACT", "AET", "SST", "NST", "MIT", "HST", "AST", "PST",
            "PNT", "MST", "CST", "EST", "IET", "PRT", "CNT", "AGT", "BET", "CAT"};
    String DATE_FORMAT = "HH::MM";
    String[] enMembers = {"Gawr Gura", "Amelia Watson", "Ninomae Ina'nis", "Takanashi Kiara", "Mori Calliope"};
    HashMap<String, String> memberIDMap = memberChannelID();

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

    public Message returnSubRankings(){

        Collections.sort(subcountList, new NumericalStringComparator());
        String rankings  = "";
        int ranking  = 1;
        for (int i = subcountList.size()-1;i>-1;i--){
            String[] parts = subcountList.get(i).split(":");
            String subcount = parts[0];
            String name = parts[1];
            if(ranking<10){
                rankings = rankings+ ranking+".   "+name+":  " + NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(subcount))+"\n";

            }
            else{
                rankings = rankings+ ranking+".  "+name+":  " + NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(subcount))+"\n";

            }

            ranking++;
        }
        MessageBuilder messageBuilder =new MessageBuilder().append("Hololive Members Ranked by Subscribers ```"+rankings+"```");
        return messageBuilder.build();
    }

    public HashMap<String, String> memberChannelID(){

        String delimiter = ":";
        HashMap<String, String> map = new HashMap<>();
        try(Stream<String> lines = Files.lines(Paths.get("memberID.txt"))){
            lines.filter(line -> line.contains(delimiter)).forEach(line -> map.putIfAbsent(getFixedString(line.split(delimiter)[0]), line.split(delimiter)[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    public void fillSubCountListPaid() {
        ArrayList<String> members = fillMemberArrayList();
        for(int i =0;i<members.size();i++){
            String[] parts = members.get(i).split(":");
            String name = parts[0];
            String url = parts[1];
            subcountList.add(getSubcount(url)+":"+getFixedString(name));
        }
    }

public ArrayList<String> fillMemberArrayList(){
    ArrayList<String> listOfLines = new ArrayList<>();
    try {
        BufferedReader bufReader = new BufferedReader(new FileReader("memberID.txt"));

        String line = bufReader.readLine();
        while (line != null) {
            listOfLines.add(line);
            line = bufReader.readLine();
        }
        bufReader.close();
    }
    catch(Exception e){

    }
    return listOfLines;
}



    public void fillSubCountList(){
        try {
            System.out.println("Building the subscriber rankings");
            for (int i = 0;i<memberList.size();i++){
                String html = Jsoup.connect("https://trackholo.live/en/member/?name="+getTrackHoloString(memberList.get(i))).get().html();
                Document document = Jsoup.parseBodyFragment(html);
                Element body = document.body();
                Elements paragraphs = body.getElementsByTag("h6");
                Element info = paragraphs.get(0);
                subcountList.add(info.text().replaceAll(",","")+":"+getFixedString(memberList.get(i)));
            }
            subcountList.add(getSubcount(yagooChannelID)+":"+"tanigox (YAGOO)");
            System.out.println("Finished building the subscriber rankings");

        } catch (IOException ex) {
            System.out.println("Error Getting Subcount");
        }
    }
    public int getSubcount(String id){
        try {
            BigInteger subs;
            YouTube.Channels.List search = youTube.channels().list("statistics");
            search.setId(id);
            search.setKey(apiKey);
            ChannelListResponse response = search.execute();
            List<Channel> channels = response.getItems();
            for (Channel channel : channels) {
                subs = channel.getStatistics().getSubscriberCount();
                return subs.intValue();
            }
        }catch(Exception e){

        }
        return 0;

    }
    public String getSchedule(String timezone, int index) {
        String info[] = getInfo(index,timezone);
        String name = info[1].replaceAll("\\s+","") + " " +info[2]
                .replaceAll("\\s+","");
        String s = schedule.get(index) + "      " + getSubcount(memberIDMap.get(name)) + " subscribers" ;

        if (!validTimezone(timezone)) {
            return "Sorry that's not a supported timezone";
        }
        Scanner parser = new Scanner(s);
        String time = parser.next();
        SimpleDateFormat isoFormat = new SimpleDateFormat("HH:mm");
        isoFormat.setTimeZone(TimeZone.getTimeZone("JST"));
        String local = "";
        try {
            Date date = isoFormat.parse(time);
            local = convertDate(date, "HH:mm", timezone);
            s = s.replaceAll(time, local + " " + timezone);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return s;
    }

    public boolean validTimezone(String timezoneCheck) {
        for (int i = 0; i < validTimezones.length; i++) {
            if (validTimezones[i].equalsIgnoreCase(timezoneCheck)) {
                return true;
            }
        }
        return false;
    }

    public void buildScheduleLinux() {
        schedule.clear();
        final String dir = System.getProperty("user.dir");
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "cd holoCli && python3 main.py"});
            p.waitFor();
            System.out.println("Done Building Schedule");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            try (BufferedReader br = new BufferedReader(new FileReader("holoCli/hololive.txt"))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    schedule.add(line);

                }
                fixNames();
            }
        } catch (Exception e) {
            System.err.println("ERROR was not able to run the scraper\nPlease check that the holoCli directory is present and that Python is installed");
        }
    }

    public void buildSchedule() {
        schedule.clear();
        final String dir = System.getProperty("user.dir");
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd holoCli && python main.py");
        builder.redirectErrorStream(true);

        try {
            Process p = builder.start();
            p.waitFor();
            System.out.println("Done Building Schedule");
            try (BufferedReader br = new BufferedReader(new FileReader("holoCli/hololive.txt"))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    schedule.add(line);
                }
                fixNames();
            }
        } catch (Exception e) {
            System.err.println("ERROR was not able to run the scraper\nPlease check that the holoCli directory is present and that Python is installed");
        }
    }

    public ArrayList<Message> getUpcomingStreams(String timezone) {

        try {
            buildScheduleLinux();
        } catch (Exception ex) {
            System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
        }
        ArrayList<Message> messages = new ArrayList<Message>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        LocalDateTime now = LocalDateTime.now();
        EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://static.wikia.nocookie.net/fc620067-166e-48d9-baa7-44abee59e6e1").setColor(new Color(3725533))
                .setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Cover_Corp_vertical_logo_1.png/220px-Cover_Corp_vertical_logo_1.png")
                .setDescription("For more info about each stream use \n!hololive *[index number]* or !hl *[index number]*");
        if (!validTimezone(timezone)) {
            embed.addField("An Error has occured", "Sorry this is not a valid timezone", false);
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            return messages;
        }

        String[] info = new String[5];
        if (schedule.size() > 25) {

            for (int i = 1; i < 26; i++) {
                info = getInfo(i, timezone);
                if (info[4].equals("passed")) {

                } else {
                    embed.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);
                }


            }
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            EmbedBuilder embed2 = new EmbedBuilder().setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Cover_Corp_vertical_logo_1.png/220px-Cover_Corp_vertical_logo_1.png").setColor(new Color(3725533));

            for (int i = 26; i < schedule.size(); i++) {
                info = getInfo(i, timezone);
                if (info[4].equals("passed")) {
                    embed2.addField("~~" + i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone + "~~", " ~~ " + info[3] + " ~~ ", false);


                } else {
                    embed2.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);


                }
            }
            MessageBuilder messageBuilder2 = (MessageBuilder) new MessageBuilder()
                    .setEmbed(embed2.build());
            messages.add(messageBuilder2.build());

        } else {
            for (int i = 1; i < schedule.size(); i++) {
                info = getInfo(i, timezone);
                if (info[4].equals("passed")) {

                } else {
                    embed.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);

                }

            }
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
        }
        return messages;
    }

    public ArrayList<Message> holoENSchedule(String timezone) {
        try {
            buildScheduleLinux();
        } catch (Exception ex) {
            System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
        }
        ArrayList<Message> messages = new ArrayList<Message>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        LocalDateTime now = LocalDateTime.now();
        EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://static.wikia.nocookie.net/fc620067-166e-48d9-baa7-44abee59e6e1").setColor(new Color(3725533))
                .setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Cover_Corp_vertical_logo_1.png/220px-Cover_Corp_vertical_logo_1.png")
                .setDescription("For more info about each stream use \n!hololive *[index number]* or !hl *[index number]*");

        if (!validTimezone(timezone)) {
            embed.addField("An Error has occured", "Sorry this is not a valid timezone", false);
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            return messages;
        }

        String[] info = new String[5];
            for (int i = 1; i < schedule.size(); i++) {
                    info = getInfo(i, timezone);
                    String fullName = info[1]+" "+info[2];
                if (arrayContainsString(enMembers,fullName)){
                if (info[4].equals("passed")) {
                    embed.addField("~~" + i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone + "~~", " ~~ " + info[3] + " ~~ ", false);
                } else {
                    embed.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);

                }
            }

            }
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            return messages;
        }



    public ArrayList<Message> getAllSchedule(String timezone) {
        try {
            buildScheduleLinux();
        } catch (Exception ex) {
            System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
        }
        ArrayList<Message> messages = new ArrayList<Message>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        LocalDateTime now = LocalDateTime.now();
        EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://static.wikia.nocookie.net/fc620067-166e-48d9-baa7-44abee59e6e1").setColor(new Color(3725533))
                .setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Cover_Corp_vertical_logo_1.png/220px-Cover_Corp_vertical_logo_1.png")
                .setDescription("For more info about each stream use \n!hololive *[index number]* or !hl *[index number]*");
        if (!validTimezone(timezone)) {
            embed.addField("An Error has occured", "Sorry this is not a valid timezone", false);
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            return messages;
        }

        String[] info = new String[5];

        if (schedule.size() > 25) {

            for (int i = 1; i < 26; i++) {
                info = getInfo(i, timezone);
                if (info[4].equals("passed")) {
                    embed.addField("~~" + i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone + "~~", "~~ " + info[3] + " ~~", false);
                } else {
                    embed.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);
                }

            }
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            EmbedBuilder embed2 = new EmbedBuilder().setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Cover_Corp_vertical_logo_1.png/220px-Cover_Corp_vertical_logo_1.png").setColor(new Color(3725533));

            for (int i = 26; i < schedule.size(); i++) {

                info = getInfo(i, timezone);
                if (info[4].equals("passed")) {
                    embed2.addField("~~" + i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone + "~~", " ~~ " + info[3] + " ~~ ", false);


                } else {
                    embed2.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);


                }
            }
            MessageBuilder messageBuilder2 = (MessageBuilder) new MessageBuilder()
                    .setEmbed(embed2.build());
            messages.add(messageBuilder2.build());

            return messages;
        } else {
            for (int i = 1; i < schedule.size(); i++) {
                info = getInfo(i, timezone);
                if (info[4].equals("passed")) {
                    embed.addField("~~" + i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone + "~~", " ~~ " + info[3] + " ~~ ", false);
                } else {
                    embed.addField(i + ". " + info[1] + " " + info[2] + " - " + info[0] + " " + timezone, info[3], false);

                }

            }
            MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                    .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                    .setEmbed(embed.build());
            messages.add(messageBuilder.build());
            return messages;
        }


    }

    public boolean hasPassed(String stream, String currentTime, int index) {
        stream = stream.replaceAll(":", "    ");
        currentTime = currentTime.replaceAll(":", "    ");
        Scanner parser = new Scanner(stream);
        Scanner parser2 = new Scanner(currentTime);
        String strStreamHour = parser.next();
        int streamHour = Integer.parseInt(strStreamHour);
        stream = stream.replaceAll(strStreamHour, " ");
        int streamMinutes = Integer.parseInt(parser.next());

        String strCurrentHour = parser2.next();
        int currentHour = Integer.parseInt(strCurrentHour);
        currentTime = currentTime.replaceAll(strCurrentHour, " ");
        int currentMinutes = Integer.parseInt(parser2.next());
        if (currentHour == 0) {
            currentHour = 24;
        }
        if (streamHour == 0) {
            streamHour = 24;
        }
        if (index < 10 && streamHour == 24) {
            return true;
        }
        if (currentHour >= streamHour && currentHour != streamHour) {

            return true;
        } else if (currentHour < streamHour && currentHour != streamHour) {

            return false;
        } else if (currentHour == streamHour) {
            if (currentMinutes > streamMinutes) {

                return true;
            } else if (currentMinutes < streamMinutes) {
                return false;
            }

        } else {
            return false;
        }

        return false;
    }

    public String[] getInfo(int index, String timezone) {
        String rawData = schedule.get(index);
        rawData = rawData.replaceAll("~", "");
        Scanner input = new Scanner(rawData);
        String time = input.next();
        rawData = rawData.replaceAll(time, "");
        String firstName = input.next();
        rawData = rawData.replaceAll(firstName, "");
        String lastName = input.next();
        rawData = rawData.replaceAll(lastName, "");

        String link = input.next();
        String[] information = new String[5];
        time = time.replaceAll("\\s+", "");
        SimpleDateFormat isoFormat = new SimpleDateFormat("HH:mm");
        isoFormat.setTimeZone(TimeZone.getTimeZone("JST"));
        try {
            Date date = isoFormat.parse(time);
            information[0] = convertDate(date, "HH:mm", timezone);
            String streamTime = convertDate(date, "HH:mm", timezone);
            String streamTimeJST = convertDate(date, "HH:mm", "JST");
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date dateNow = new Date();
            String localtoJST = convertDate(dateNow, "HH:mm", "JST");

            if (hasPassed(streamTimeJST, localtoJST, index)) {

                information[4] = "passed";

            } else {
                information[4] = "not";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        information[1] = firstName;
        information[2] = lastName;
        information[3] = link;
        return information;
    }

    public void fixNames() {
        for (int i = 1; i < schedule.size(); i++) {
            String index = schedule.get(i);
            index = index.replaceAll("Risu", "Ayunda Risu");
            index = index.replaceAll("Moona", "Moona Hoshinova");
            index = index.replaceAll("Iofi", "Airani Iofifteen");
            index = index.replaceAll("Ina", "Ninomae Ina'nis");
            index = index.replaceAll("holostars", "Holostars Ch.");
            index = index.replaceAll("AZKi", "AZKi Music");
            index = index.replaceAll("Rikka", "Rikka Ch.");
            index = index.replaceAll("Arurandeisu", "Arurandeisu Ch.");
            index = index.replaceAll("Ollie", "Kureiji Ollie");
            index = index.replaceAll("Anya", "Anya Melfissa");
            index = index.replaceAll("Reine", "Pavolia Reine");
            index = index.replaceAll("Sishiro Botan","Shishiro Botan");
            index = index.replaceAll("holoID", "HololiveID Ch.");
            index = index.replaceAll("Calli", "Mori Calliope");
            index = index.replaceAll("Kiara", "Takanashi Kiara");
            index = index.replaceAll("Gura", "Gawr Gura");
            index = index.replaceAll("Amelia", "Amelia Watson");
            index = index.replaceAll("holoEN", "HololiveEN Ch.");
            index = index.replaceAll("Roboco-san", "Roboco Ch.");
            index = index.replaceAll("Yuzuki Choko Sub", "Yuzuki Choco");
            index = index.replaceAll("hololive", "Hololive Ch.");
            index = index.replaceAll("Hoshimatsi Suisei", "Hoshimachi Suisei");

            index = index.replaceAll("~", "");
            schedule.set(i, index);
        }
    }
    public String getFixedString(String index) {
            index = index.replaceAll("Risu", "Ayunda Risu");
            index = index.replaceAll("Moona", "Moona Hoshinova");
            index = index.replaceAll("Iofi", "Airani Iofifteen");
            index = index.replaceAll("Ina", "Ninomae Ina'nis");
            index = index.replaceAll("holostars", "Holostars Ch.");
            index = index.replaceAll("AZKi", "AZKi Music");
        index = index.replaceAll("Sishiro Botan","Shishiro Botan");
            index = index.replaceAll("Rikka", "Rikka Ch.");
            index = index.replaceAll("Arurandeisu", "Arurandeisu Ch.");
            index = index.replaceAll("Ollie", "Kureiji Ollie");
            index = index.replaceAll("Anya", "Anya Melfissa");
            index = index.replaceAll("Reine", "Pavolia Reine");
            index = index.replaceAll("holoID", "HololiveID Ch.");
        index = index.replaceAll("AkiRose","Aki Rosenthal");
            index = index.replaceAll("Calli", "Mori Calliope");
            index = index.replaceAll("Kiara", "Takanashi Kiara");
            index = index.replaceAll("Gura", "Gawr Gura");
            index = index.replaceAll("Amelia", "Amelia Watson");
            index = index.replaceAll("holoEN", "HololiveEN Ch.");
            index = index.replaceAll("Roboco-san", "Roboco Ch.");
            index = index.replaceAll("Yuzuki Choko Sub", "Yuzuki Choco");
            index = index.replaceAll("hololive", "Hololive Ch.");
            index = index.replaceAll("Hoshimatsi Suisei", "Hoshimachi Suisei");
            index = index.replaceAll("~", "");
            return index;

    }
    public String getTrackHoloString(String index) {
        index = index.replaceAll("Risu", "Ayunda Risu");
        index = index.replaceAll("Moona", "Moona Hoshinova");
        index = index.replaceAll("Iofi", "Airani Iofifteen");
        index = index.replaceAll("Ina", "ninomaeinanis");
        index = index.replaceAll("holostars", "Holostars Ch.");
        index = index.replaceAll("AZKi", "azki");
        index = index.replaceAll("Rikka", "Rikka Ch.");
        index = index.replaceAll("Arurandeisu", "Arurandeisu Ch.");
        index = index.replaceAll("Ollie", "Kureiji Ollie");
        index = index.replaceAll("Anya", "Anya Melfissa");
        index = index.replaceAll("Reine", "Pavolia Reine");
        index = index.replaceAll("holoID", "HololiveID Ch.");
        index = index.replaceAll("Calli", "Mori Calliope");
        index = index.replaceAll("Kiara", "Takanashi Kiara");
        index = index.replaceAll("Gura", "Gawr Gura");
        index = index.replaceAll("Amelia", "watsonamelia");
        index = index.replaceAll("holoEN", "HololiveEN Ch.");
        index = index.replaceAll("Sishiro Botan","shishirobotan");
        index = index.replaceAll("Roboco-san", "robocosan");
        index = index.replaceAll("Aki Rose","akirosenthal");
        index = index.replaceAll("Yuzuki Choko Sub", "Yuzuki Choco");
        index = index.replaceAll("hololive", "Hololive Ch.");
        index = index.replaceAll("Hoshimatsi Suisei", "Hoshimachi Suisei");
        index = index.replaceAll("~", "");
        index = index.replaceAll("\\s+", "");
        index = index.toLowerCase();
        return index;

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

            e.getChannel().sendMessage("Scraping the ranking page. Thank you for your patience").queue();
            subcountList.removeAll(subcountList);
            fillSubCountList();
            e.getChannel().sendMessage(returnSubRankings()).queue();
        }
        if(msg.startsWith("!testcommand")){

            e.getChannel().sendMessage("Scraping the ranking page. Thank you for your patience").queue();
            subcountList.removeAll(subcountList);
            fillSubCountListPaid();
            e.getChannel().sendMessage(returnSubRankings()).queue();
        }
        if (msg.startsWith("!holoen") || msg.startsWith("!hlen")) {

            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();

            try{
                buildScheduleLinux();
            }
            catch(Exception ex){
                System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
            }
            msg = msg.replaceAll("!holoen","");
            msg = msg.replaceAll("!hlen","");
            msg = msg.replaceAll("\\s+", "");
            msg = msg.toUpperCase();
            if (msg.equals("") || msg.equals(null)) {
                msg = "JST";
            }
            String timezone = msg;
            logCommand(e, "hololive EN schedule");
            ArrayList<Message> messages = new ArrayList<Message>();
            messages = holoENSchedule(timezone);
            e.getChannel().sendMessage(messages.get(0)).queue();
            buildScheduleLinux();
        }

        if (msg.startsWith("!hololive all") || msg.startsWith("!hl all")) {
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            msg = msg.replaceAll("!hololive all", "");
            msg = msg.replaceAll("!hl all", "");
            msg = msg.replaceAll("\\s+", "");
            if (msg.equals("") || msg.equals(null)) {
                msg = "JST";
            }
            msg = msg.toUpperCase();
            ArrayList<Message> messages = new ArrayList<Message>();
            logCommand(e, "full hololive schedule " + msg);
            messages = getAllSchedule(msg);
            if (messages.size() == 2) {
                e.getChannel().sendMessage(messages.get(0)).queue();
                e.getChannel().sendMessage(messages.get(1)).queue();
            }
            else if(messages.size()==0){
                logCommand(e,"Error Value Returned");
            }
            else {
                e.getChannel().sendMessage(messages.get(0)).queue();
            }

        }



        else if (msg.startsWith("!hl") &&!msg.contains("!hlsearch")&& !msg.contains("!hololive all") &&
                !msg.contains("!hl all") && !msg.contains("!hl upcoming") &&
                !msg.contains("!hololive upcoming")&&!msg.startsWith("!hlranking")) {
            msg = msg.replaceAll("!hololive", "");
            msg = msg.replaceAll("!hl", "");
            Scanner parser = new Scanner(msg);
            String strIndex = parser.next();
            int index = Integer.parseInt(strIndex);
            msg = msg.replaceAll(strIndex, "");
            msg = msg.replaceAll("\\s+", "");
            String timezone = msg;
            timezone = timezone.toUpperCase();

            if (msg.equals("") || msg.equals(null)) {
                timezone = "JST";
            }
            logCommand(e, "hololive schedule index " + index + " in " + timezone);
            e.getChannel().sendMessage(getSchedule(timezone, index)).queue();
            try{
                buildScheduleLinux();
            }
            catch(Exception ex){
                System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
            }
        }



        else if (msg.startsWith("!hl upcoming") || msg.startsWith("!hololive upcoming")) {
            System.out.println("Upcoming");
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            msg = msg.replaceAll("!hololive upcoming", "");
            msg = msg.replaceAll("!hl upcoming", "");
            Scanner parser = new Scanner(msg);
            msg = msg.replaceAll("\\s+", "");
            String timezone = msg;
            timezone = timezone.replaceAll("\\s+", "");
            timezone = timezone.toUpperCase();

            if (msg.equals("") || msg.equals(null)) {
                timezone = "JST";
            }
            logCommand(e, "hololive upcoming streams " + " in " + timezone);
            ArrayList<Message> messages = new ArrayList<Message>();
            logCommand(e, "full hololive schedule " + msg);
            try{
                buildScheduleLinux();
            }
            catch(Exception ex){
                System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
            }
            messages = getUpcomingStreams(timezone);
            if (messages.size() == 2) {
                e.getChannel().sendMessage(messages.get(0)).queue();
                e.getChannel().sendMessage(messages.get(1)).queue();
            } else {
                e.getChannel().sendMessage(messages.get(0)).queue();
            }
        }
        if (msg.equals("!hololive refresh") || msg.equals("!hl refresh")) {
            e.getChannel().sendMessage("Scraping the website. Thank you for your patience").queue();
            try{
                buildScheduleLinux();
            }
            catch(Exception ex){
                System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
            }
            logCommand(e, "manual hololive schedule refresh");
            e.getChannel().sendMessage("Hololive Schedule has been manually refreshed").queue();
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
