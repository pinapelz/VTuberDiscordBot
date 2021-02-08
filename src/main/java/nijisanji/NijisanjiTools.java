package nijisanji;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;

public class NijisanjiTools extends ListenerAdapter {
    ScreenShotTool ssTool = new ScreenShotTool();
    public void buildNijisanjiSchedule(){
        ssTool.buildNijisanjiSchedule();
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.equals("!nijischedule")){
            ssTool.buildNijisanjiSchedule();
            File image1 = new File("nijisanji.png");
            File image2 = new File("nijisanji2.png");
            File image3 = new File("nijisanji3.png");
            e.getChannel().sendMessage("Upcoming Nijisanji Schedule").queue();
            e.getChannel().sendFile(image1,"nijisanji.png").queue();
            e.getChannel().sendFile(image2,"nijisanji2.png").queue();
            e.getChannel().sendFile(image3,"nijisanji3.png").queue();
        }

    }
}
