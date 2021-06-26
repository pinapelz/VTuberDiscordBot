package nijisanji;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utilities.ScreenShotTool;

import java.io.File;
import java.util.List;

public class NijisanjiTools extends ListenerAdapter{

    boolean updateNijiManual = false;
    String chromeDriverPath  = "/usr/lib/chromium-browser/chromedriver";
    public NijisanjiTools(boolean manualUpdate,String chromeDriverPath){
    this.updateNijiManual = manualUpdate;
    this.chromeDriverPath = chromeDriverPath;
    ssTool.buildNijisanjiSchedule();
    }
    ScreenShotTool ssTool = new ScreenShotTool(chromeDriverPath);
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.equals("!nijirefresh")){
                ssTool.buildNijisanjiSchedule();

        }
        if(msg.equals("!nijischedule")){
            if(msg.equals("!nijischedule")) {
                if (updateNijiManual) {
                    ssTool.buildNijisanjiSchedule();
                }

            }
            System.out.println(e.getMember().getUser()+" requested Nijisanji Schedule");
            EmbedBuilder embed = new EmbedBuilder();
            File image1 = new File("nijisanji.png");
            embed.setImage("attachment://nijisanji.png")
                    .setTitle("Nijisanji Schedule P.1");
            e.getChannel().sendMessage(embed.build())
                    .addFile(image1, "nijisanji.png")
                    .queue(msgg->{
                        msgg.addReaction("⬅").queue();
                        msgg.addReaction("➡").queue();
                        msgg.addReaction("❌").queue();
                    });



        }

    }
    void loadNijisanji(int index, GuildMessageReactionAddEvent e){
        String messageID = e.getMessageId();
            EmbedBuilder embed = new EmbedBuilder();
            File image1 = new File("nijisanji.png");
            File image2 = new File("nijisanji2.png");
            File image3 = new File("nijisanji3.png");
            if (index == 1) {
                embed.setImage("attachment://nijisanji.png")
                        .setTitle("Nijisanji Schedule P.1");
                e.getChannel().retrieveMessageById(messageID).complete().delete().queue();
                e.getChannel().sendMessage(embed.build())
                        .addFile(image1, "nijisanji.png")
                        .queue(msgg->{
                            msgg.addReaction("⬅").queue();
                            msgg.addReaction("➡").queue();
                            msgg.addReaction("❌").queue();
                        });

            }
            if (index == 2) {
                embed.setImage("attachment://nijisanji2.png")
                        .setTitle("Nijisanji Schedule P.2");
                e.getChannel().retrieveMessageById(messageID).complete().delete().queue();
                e.getChannel().sendMessage(embed.build())
                        .addFile(image2, "nijisanji2.png")
                        .queue(msgg->{
                            msgg.addReaction("⬅").queue();
                            msgg.addReaction("➡").queue();
                            msgg.addReaction("❌").queue();
                        });

            }
            if (index == 3) {
                embed.setImage("attachment://nijisanji3.png")
                        .setTitle("Nijisanji Schedule P.3");
                e.getChannel().retrieveMessageById(messageID).complete().delete().queue();
                e.getChannel().sendMessage(embed.build())
                        .addFile(image3, "nijisanji3.png")
                        .queue(msgg->{
                            msgg.addReaction("⬅").queue();
                            msgg.addReaction("➡").queue();
                            msgg.addReaction("❌").queue();
                        });

            }
        }



    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e){

            if(e.getReactionEmote().getName().equals("⬅")&&!e.getMember().getUser().isBot()){
           Message message =  (Message)e.getChannel().retrieveMessageById(e.getMessageId()).complete();
           e.getChannel().retrieveMessageById(e.getMessageId()).complete().removeReaction(
                   "⬅",e.getChannel().retrieveMessageById(e.getMessageId()).complete().getAuthor());
           List<MessageEmbed> embeds = message.getEmbeds();
           if(embeds.get(0).getTitle().equals("Nijisanji Schedule P.1")){

           }
           else if(embeds.get(0).getTitle().equals("Nijisanji Schedule P.2")){
            loadNijisanji(1,e);
           }
           else if(embeds.get(0).getTitle().equals("Nijisanji Schedule P.3")){
               loadNijisanji(2,e);
                }
            }
            else if(e.getReactionEmote().getName().equals("➡")&&!e.getMember().getUser().isBot()){
                Message message =  (Message)e.getChannel().retrieveMessageById(e.getMessageId()).complete();
                List<MessageEmbed> embeds = message.getEmbeds();
                if(embeds.get(0).getTitle().equals("Nijisanji Schedule P.1")){
                    loadNijisanji(2,e);
                }
                else if(embeds.get(0).getTitle().equals("Nijisanji Schedule P.2")){
                    loadNijisanji(3,e);
                }
                else if(embeds.get(0).getTitle().equals("Nijisanji Schedule P.3")){

                }
            }
            else if(e.getReactionEmote().getName().equals("❌")&&!e.getMember().getUser().isBot()){
               e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
                System.out.println(e.getMember().getUser()+" deleted a Nijisanji Embed");
            }
            }
    }



