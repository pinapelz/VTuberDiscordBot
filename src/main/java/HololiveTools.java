import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HololiveTools {

    ArrayList<String> schedule = new ArrayList<String>();
    String[] validTimezones = {"GMT","UTC","ECT","EET","ART","EAT","MET","NET",
            "PLT","IST","BST","VST","CTT","JST","ACT","AET","SST","NST","MIT","HST","AST","PST",
            "PNT","MST","CST","EST","IET","PRT","CNT","AGT","BET","CAT"};
    String DATE_FORMAT = "HH::MM";

public String getSchedule(String timezone, int index){
    String s = schedule.get(index);
    if(!validTimezone(timezone)){
    return "Sorry that's not a supported timezone";
    }
    Scanner parser = new Scanner(s);
    String time = parser.next();
    SimpleDateFormat isoFormat = new SimpleDateFormat("HH:mm");
    isoFormat.setTimeZone(TimeZone.getTimeZone("JST"));
    String local = "";
    try {
        Date date = isoFormat.parse(time);
     local = convertDate(date,"HH:mm",timezone);
     s = s.replaceAll(time,local + " " + timezone);
    } catch (ParseException e) {
        e.printStackTrace();
    }

return s;
}
public boolean validTimezone(String timezoneCheck){
    for(int i = 0;i< validTimezones.length;i++){
        if(validTimezones[i].equalsIgnoreCase(timezoneCheck)){
            return true;
        }
    }
    return false;
}
    public void buildScheduleLinux(){
        schedule.clear();
        final String dir = System.getProperty("user.dir");
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"bash","-c","cd holoCli && python3 main.py"});
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (Exception e) {
            e.printStackTrace();
        }



        try {

            try ( BufferedReader br = new BufferedReader(new FileReader("holoCli/hololive.txt"))) {
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
public void buildSchedule(){
   schedule.clear();
    final String dir = System.getProperty("user.dir");
    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd holoCli && python main.py");
    builder.redirectErrorStream(true);

    try {
        Process p = builder.start();
        try ( BufferedReader br = new BufferedReader(new FileReader("holoCli/hololive.txt"))) {
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
                try{
        buildScheduleLinux();
        }
        catch(Exception ex){
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
        return messages;

    }
    else {
        for (int i = 1; i < schedule.size() ; i++) {
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
        return messages;
    }
}

public ArrayList<Message> getAllSchedule(String timezone){
    try{
        buildScheduleLinux();
        }
        catch(Exception ex){
          System.out.println("Failed to build schedule. Possible scraper script error or incorrect name formatting");
        }
    ArrayList<Message> messages = new ArrayList<Message>();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    LocalDateTime now = LocalDateTime.now();
    EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://static.wikia.nocookie.net/fc620067-166e-48d9-baa7-44abee59e6e1").setColor(new Color(3725533))
            .setFooter("Retreived at PST "+dtf.format(now) +"- DS",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Cover_Corp_vertical_logo_1.png/220px-Cover_Corp_vertical_logo_1.png")
            .setDescription("For more info about each stream use \n!hololive *[index number]* or !hl *[index number]*");
    if(!validTimezone(timezone)){
    embed.addField("An Error has occured","Sorry this is not a valid timezone",false);
        MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder()
                .append("**Recent Hololive and Holostars Schedule**\nIf Index is too long first few may become un-crossed out\n Use !hololive upcoming [timezone] to filter already started and finished streams")
                .setEmbed(embed.build());
        messages.add(messageBuilder.build());
        return messages;
    }

    String[] info = new String[5];

    if(schedule.size()>25){

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
        EmbedBuilder embed2 = new EmbedBuilder().setFooter("Retreived at PST "+dtf.format(now) +"- DS",
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
    }

    else {
        for (int i = 1; i < schedule.size() ; i++) {
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
public boolean hasPassed(String stream, String currentTime, int index){
    stream = stream.replaceAll(":","    ");
    currentTime = currentTime.replaceAll(":","    ");
    Scanner parser = new Scanner(stream);
    Scanner parser2 = new Scanner(currentTime);
    String strStreamHour = parser.next();
    int streamHour = Integer.parseInt(strStreamHour);
    stream = stream.replaceAll(strStreamHour," ");
    int streamMinutes = Integer.parseInt(parser.next());

    String strCurrentHour = parser2.next();
    int currentHour = Integer.parseInt(strCurrentHour);
    currentTime = currentTime.replaceAll(strCurrentHour," ");
    int currentMinutes = Integer.parseInt(parser2.next());
    if(currentHour==0){
        currentHour = 24;
    }
    if(streamHour==0){
        streamHour=24;
    }
    if(index<10&&streamHour==24){
        return true;
    }
    if(currentHour>=streamHour&&currentHour!=streamHour){

        return true;
    }
    else if(currentHour<streamHour&&currentHour!=streamHour){

        return false;
    }
    else if(currentHour==streamHour){
        if(currentMinutes>streamMinutes){

            return true;
        }
        else if(currentMinutes<streamMinutes){
            return false;
        }

    }
    else{
        return false;
    }

    return false;
}
public String[] getInfo(int index, String timezone){
    String rawData = schedule.get(index);
    System.out.println(rawData);
    rawData = rawData.replaceAll("~", "");
    Scanner input = new Scanner(rawData);
    String time  = input.next();
    rawData = rawData.replaceAll(time,"");
    String firstName = input.next();
    rawData = rawData.replaceAll(firstName,"");
    String lastName  = input.next();
    rawData = rawData.replaceAll(lastName,"");

    String link = input.next();
    String[]  information = new String[5];
    time = time.replaceAll("\\s+","");
    SimpleDateFormat isoFormat = new SimpleDateFormat("HH:mm");
    isoFormat.setTimeZone(TimeZone.getTimeZone("JST"));
    try {
        Date date = isoFormat.parse(time);
        information[0] = convertDate(date,"HH:mm",timezone);
        String streamTime = convertDate(date,"HH:mm",timezone);
        String streamTimeJST = convertDate(date,"HH:mm","JST");
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date dateNow = new Date();
        String localtoJST = convertDate(dateNow,"HH:mm","JST");

        if(hasPassed(streamTimeJST,localtoJST,index)){

            information[4] = "passed";

        }
        else{
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
public void fixNames(){
    for (int i = 1; i < schedule.size(); i++) {
        String index  = schedule.get(i);

        index = index.replaceAll("Risu","Ayunda Risu");
        index = index.replaceAll("Iofi","Airani Iofifteen");
        index = index.replaceAll("Ina", "Ninomae Ina'nis");
        index = index.replaceAll("holostars", "Holostars Ch.");
        index = index.replaceAll("AZKi","AZKi Music");
        index = index.replaceAll("Rikka", "Rikka Ch.");
        index = index.replaceAll("Arurandeisu","Arurandeisu Ch.");
        index = index.replaceAll("Ollie","Kureiji Ollie");
        index = index.replaceAll("Anya", "Anya Melfissa");
        index = index.replaceAll("Reine","Pavolia Reine");
        index = index.replaceAll("holoID", "HololiveID Ch.");
        index = index.replaceAll("Calli", "Mori Calliope");
        index = index.replaceAll("Kiara", "Takanashi Kiara");
        index = index.replaceAll("Gura", "Gawr Gura");
        index = index.replaceAll("Amelia", "Amelia Watson");
        index = index.replaceAll("holoEN","HololiveEN Ch.");
        index = index.replaceAll("Roboco-san","Roboco Ch.");
        index = index.replaceAll("Yuzuki Choko Sub","Yuzuki Choco");
        index = index.replaceAll("hololive","Hololive Ch.");

        index = index.replaceAll("~","");
        schedule.set(i, index);
    }
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

}
