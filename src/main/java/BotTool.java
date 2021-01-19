import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BotTool extends ListenerAdapter{

    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if (msg.startsWith("!setplaying")) {
            boolean allowChange = checkAdmin(e);
            if (allowChange) {
                String playingMessage = msg.replaceAll("!setplaying", "");
                System.out.println(returnTimestamp() + " Request to change playing message received");
                jda.getPresence().setActivity(Activity.playing(playingMessage));
                e.getChannel().sendMessage("Playing Status Succsessfully Changed").queue();
            } else {
                e.getChannel().sendMessage("You have no authority to tell me what to do").queue();
            }
        }


        else if (msg.startsWith("!setwatching")) {
          boolean allowChange= checkAdmin(e);
            if (allowChange) {
                String watchingMessage = msg.replaceAll("!setwatching", "");
                System.out.println(returnTimestamp() + " Request to change watching message received");
                jda.getPresence().setActivity(Activity.watching(watchingMessage));
                e.getChannel().sendMessage("Watching Status Succsessfully Changed").queue();
            } else {
                e.getChannel().sendMessage("You have no authority to tell me what to do").queue();
            }
        }

        if(msg.equals("!maintenance on")){
            boolean allowChange= checkAdmin(e);
            if (allowChange) {
                e.getChannel().sendMessage("The Bot is now in Maintenance Mode").queue();
                jda.shutdown();
            } else {
                e.getChannel().sendMessage("You have no authority to tell me what to do").queue();
            }
        }
        if(msg.equals("!help")){
            MessageBuilder helpMessage = new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle("Holobot !help available commands", "https://github.com/pinapelz/holoDiscord")
                    .setDescription("A list of available commands v.1.5.25b")
                    .setColor(new Color(8877218))
                    .setTimestamp(OffsetDateTime.parse("2021-01-05T22:18:31.856Z"))
                    .addField("!hl all [timezone]", "Shows the recent streams of \nHolostars and Hololive members", true)
                    .addField("!hl [index] [timezone]", "Show more info about a stream after \ngetting the index from !hl all [timezone]", true)
                    .addField("!hl upcoming", "an informative error should show up, and this view will remain as-is until all issues are fixed", false)
                    .addField("!holoen [timezone]", "Shows recent streams for only Hololive English members", true)
                    .addField("!hlranking", "Ranks Hololive members by subscribers", false)
                    .build());
            e.getChannel().sendMessage(helpMessage.build()).queue();

        }



    }
    public static String returnTimestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }
    public boolean checkAdmin(MessageReceivedEvent e){
        boolean allowChange = false;
        Role admin = e.getGuild().getRoleById("794482971830648843");
        for (int i = 0; i < e.getMember().getRoles().size(); i++) {
            if (e.getMember().getRoles().get(i).equals(admin)) {
                return true;
            }
        }
        return false;
    }

}
