import audio.Music;
import hololive.HololiveTools;
import hololive.KusoNihongoConverter;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nijisanji.NijisanjiTools;
import utilities.ReactRoles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Main extends ListenerAdapter {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static LocalDateTime now = LocalDateTime.now();
    static HololiveTools hololive = new HololiveTools(getYoutubeKey());
    public static JDABuilder jdabuilder = JDABuilder.createDefault(getDiscordKey()).addEventListeners(new Main());
    //public static NijisanjiTools nijiTools = new NijisanjiTools(getManualUpdateSetting(),getChromeDriverPath());
    public static KusoNihongoConverter kusoNihongo = new KusoNihongoConverter();
    public static JDA jda;
    public static BotTool bottool = new BotTool();
    public static void main(String args[]) {

        hololive.buildSchedule();
        hololive.fillMemberList();
        hololive.fillSubCountList();
        try {
            jdabuilder.addEventListeners(bottool);
            jdabuilder.addEventListeners(hololive);
            //jdabuilder.addEventListeners(nijiTools);
            jdabuilder.addEventListeners(kusoNihongo);
            jdabuilder.addEventListeners(new ReactRoles());
            jdabuilder.addEventListeners(new Music(jda,getYoutubeKey()));
            jda = jdabuilder.build();
            System.out.println(returnTimestamp() + " Bot Succsessfully Started!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to Login");
        }

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
    }
    public void logCommand(MessageReceivedEvent e, String message) {
        System.out.println(returnTimestamp() + " " + e.getAuthor() + " requested " + message);
    }
    public static String returnTimestamp() {
        now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }
    public static boolean getManualUpdateSetting(){
        try (BufferedReader br = new BufferedReader(new FileReader("settings/youtubeApiKey.txt"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                return Boolean.parseBoolean(line);

            }
        }
        catch(Exception e){

        }
        return false;
    }

    public static String getYoutubeKey(){

        try (BufferedReader br = new BufferedReader(new FileReader("settings/youtubeApiKey.txt"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("Read API Key as " + line);
                return line;

            }
        }
        catch(Exception e){

        }
        return "ERROR";
    }

    public static String getDiscordKey(){
        try (BufferedReader br = new BufferedReader(new FileReader("settings/discordToken.txt"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("Logging in as " + line);
                return line;

            }
        }
        catch(Exception e){

        }
        return "ERROR";
    }
    public static String getChromeDriverPath(){
        try (BufferedReader br = new BufferedReader(new FileReader("settings/chromeDriverPath.txt"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                return line;

            }
        }
        catch(Exception e){

        }
        return "ERROR";
    }

}

