import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Scanner;

public class PingPong extends ListenerAdapter{
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        if(e.getMessage().getContentRaw().equalsIgnoreCase("!ping")){
            System.out.println("Pong Request Received");
            e.getChannel().sendMessage("Pong").queue();
        }
    }
}
