package hololive;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class KusoNihongoConverter extends ListenerAdapter {
    HashMap<String, String> kusoNihongoDictionary = getKSJPDictionary();

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if (msg.startsWith("!kusonihongo")) {
            e.getChannel().sendMessage(convertToKusoNihongo(msg.replaceAll("!kusonihongo",""))).queue();
        }

    }

    private HashMap<String, String> getKSJPDictionary() {

        String delimiter = ",";
        HashMap<String, String> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get("kusoDictionary.txt"))) {
            lines.filter(line -> line.contains(delimiter)).forEach(line ->
                    map.putIfAbsent(line.split(delimiter)[0], line.split(delimiter)[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private String convertToKusoNihongo(String input) {
        for (Map.Entry<String, String> entry : kusoNihongoDictionary.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        input = input.replaceAll("\\p{Punct}"," peko.");
        input = input.concat(" peko");
        return input;
    }
}
