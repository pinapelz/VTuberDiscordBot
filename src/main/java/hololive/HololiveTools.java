package hololive;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;


public class HololiveTools extends ListenerAdapter {
    static HashMap<String, String> memberIDMap = fillHashMapFromSite("holoMemberID.txt");
    static ArrayList<Message> messageQueue = new ArrayList<Message>();
    static HashMap<String, Integer> schedule = new HashMap<String, Integer>(); //
    static Set<String> keySet = memberIDMap.keySet();
    static Collection<String> values = memberIDMap.values(); //ids
    static HashMap<String, String> nijisanjiID = fillHashMap("data//holoMemberID.txt");
    static ArrayList<String> listOfKeys = new ArrayList<String>(values); //names
    static ArrayList<String> listOfValues = new ArrayList<String>(keySet);
    static ArrayList<Integer> scheduleTimes=new ArrayList<Integer>();
    static ArrayList<String> scheduleNames=new ArrayList<String>();
    static ArrayList<String> finalSchedule = new ArrayList<String>();
    static ArrayList<String> finalScheduleLine2 = new ArrayList<String>();
    static HashMap<String, Integer> sortedSchedule = new HashMap<String, Integer>();
    static ArrayList<String> individualSchedule = new ArrayList<String>();
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.startsWith("!holo")&&!msg.startsWith("!holoschedule")){
            msg = msg.replaceAll("!holo", "");
            msg = msg.replaceAll("\\s+", "");
            int index = Integer.parseInt(msg);
            index--;
            e.getChannel().sendMessage(individualSchedule.get(index)).queue();
        }
        if(msg.startsWith("!holoschedule")){
            LocalDateTime now = LocalDateTime.now();
            finalSchedule.clear();
            individualSchedule.clear();
            scheduleNames.clear();
            scheduleTimes.clear();
            sortedSchedule.clear();
            schedule = fillHashMapIntString("data//hololive.txt");
            sortedSchedule = sortByValue(schedule); //sorted from least to greatest
            for (Map.Entry<String, Integer> en : sortedSchedule.entrySet()) {
                scheduleNames.add(en.getKey());
                scheduleTimes.add(en.getValue());
            }
            for(int i = 0;i<scheduleTimes.size();i++){
                Date date = new java.util.Date(scheduleTimes.get(i) * 1000L);
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm z");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("PST"));
                finalSchedule.add(scheduleNames.get(i)+" - "+ sdf.format(date));
                finalScheduleLine2.add("https://www.youtube.com/channel/"+nijisanjiID.get(scheduleNames.get(i))+"/live");
                individualSchedule.add(scheduleNames.get(i)+"         "+ sdf.format(date)+"\nhttps://www.youtube.com/channel/"+nijisanjiID.get(scheduleNames.get(i))+"/live");
            }
            if(finalSchedule.size()>25){
                EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Hololive_triangles_logo.svg/1200px-Hololive_triangles_logo.svg.png").setColor(new Color(0x181819))
                        .setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                                "https://upload.wikimedia.org/wikipedia/commons/9/9b/Cover_Corp_vertical_logo_1.png")
                        .setDescription("For more info about each stream use");
                int index = 1;
                for(int i=0;i<26;i++){
                    embed.addField(index+". "+finalSchedule.get(i),finalScheduleLine2.get(i),false);
                    index++;
                }
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                messageQueue.add(messageBuilder.build());
                EmbedBuilder embed2 = new EmbedBuilder().setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                        "https://upload.wikimedia.org/wikipedia/commons/9/9b/Cover_Corp_vertical_logo_1.png").setColor(new Color(3725533));
                index = 26;
                for(int i=26;i<finalSchedule.size();i++){
                    embed.addField(index+". "+finalSchedule.get(i),finalScheduleLine2.get(i),false);
                    index++;
                }
                MessageBuilder messageBuilder2 = (MessageBuilder) new MessageBuilder().setEmbeds(embed2.build());
                messageQueue.add(messageBuilder2.build());
                e.getChannel().sendMessage(messageQueue.get(0)).queue();
                e.getChannel().sendMessage(messageQueue.get(1)).queue();

            }
            else{
                EmbedBuilder embed = new EmbedBuilder().setThumbnail("https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Hololive_triangles_logo.svg/1200px-Hololive_triangles_logo.svg.png").setColor(new Color(0x1A5387))
                        .setFooter("Retreived at PST " + dtf.format(now) + "- DS",
                                "https://upload.wikimedia.org/wikipedia/commons/9/9b/Cover_Corp_vertical_logo_1.png")
                        .setDescription("For more info about each stream use");
                int index = 1;
                for(int i=0;i<finalSchedule.size();i++){
                    embed.addField(index+". "+finalSchedule.get(i),finalScheduleLine2.get(i),false);
                    index++;
                }
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                e.getChannel().sendMessage(messageBuilder.build()).queue();
            }


        }
    }

    public static HashMap<String, Integer> fillHashMapIntString(String file){
        String delimiter = ":";
        HashMap<String, Integer> map = new HashMap<>();
        try(Stream<String> lines = Files.lines(Paths.get(file))){
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[0]
                            , Integer.parseInt(line.split(delimiter)[1])));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    public static HashMap<String, String> fillHashMap(String file){
        String delimiter = ":";
        HashMap<String, String> map = new HashMap<>();
        try(Stream<String> lines = Files.lines(Paths.get(file))){
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[1]
                            , line.split(delimiter)[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
    private ArrayList<Integer> getIntegerArray(ArrayList<String> stringArray) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for(String stringValue : stringArray) {
            try {
                result.add(Integer.parseInt(stringValue));
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);

            }
        }
        return result;
    }
    public static HashMap<String, String> fillHashMapFromSite(String fileName){
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
        return map;
    }

}