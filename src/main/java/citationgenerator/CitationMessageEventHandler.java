package citationgenerator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CitationMessageEventHandler extends ListenerAdapter {
    final String CHANNEL_NAME = "bot-channel";
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;
        if (!messageChannel.getName().equals(CHANNEL_NAME)) return;

        String messageLine = event.getMessage().getContentRaw();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(179,179,179));
        if (messageLine.equals("!help citation")) {
            embedBuilder.setTitle("PNCBot Citation Generator!");
            embedBuilder.setDescription("""
                The PNCBot Citation Generator can mass cite your sources with ease!
                Made by ``ColonelMDoge`` using the ``Bibify`` citation machine.
                Takes about 3 seconds per citation, so please be patient!
                
                **Copy and paste your sources on separate new lines after the command line!**
                
                **Example Input Line:**
                ```!citeAPA
                https://example.com
                https://example.com
                ...```
                
                Copy and paste the citation list to Google Docs and fix the formatting to the required format.
                Adjust the indent sliders to match the indent requirement as well.
                Document is usually in 12pt Times New Roman double spaced font.
                                  
                Here are the list of the commands this bot can do:""");
            embedBuilder.addField("Commands:", """
                ``!citeAPA`` - Cites using the American Psychological Association 7th Edition.
                ``!citeMLA`` - Cites using the Modern Language Association 9th Edition.
                ``!citeCMS`` - Cites using the Chicago Manual Style 17th Edition.
                """, false);
            embedBuilder.setFooter("This feature of the bot was made with https://bibify.org");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.contains("!citeAPA\n") && messageLine.split("\n")[1] != null) {
            String[] sources = formatSourcesIntoArray(messageLine, "!citeAPA");
            CitationGenerator generator = new CitationGenerator(CitationGenerator.APA);
            sendCitationList(messageChannel, generator, sources);
        }
        if (messageLine.contains("!citeMLA\n") && messageLine.split("\n")[1] != null) {
            String[] sources = formatSourcesIntoArray(messageLine, "!citeMLA");
            CitationGenerator generator = new CitationGenerator(CitationGenerator.MLA);
            sendCitationList(messageChannel, generator, sources);
        }
        if (messageLine.contains("!citeCMS\n") && messageLine.split("\n")[1] != null) {
            String[] sources = formatSourcesIntoArray(messageLine, "!citeCMS");
            CitationGenerator generator = new CitationGenerator(CitationGenerator.CMS);
            sendCitationList(messageChannel, generator, sources);
        }
    }
    private void sendCitationList(MessageChannel messageChannel, CitationGenerator generator, String[] sources) {
        messageChannel.sendMessage("Please wait for the citations to generate...").queue();
        StringBuilder citationList = new StringBuilder();
        for (int i = 1; i < sources.length; i++) {
            messageChannel.sendMessage("Citations left: " + (sources.length - i)).queue();
            String citation = generator.retrieveCitation(sources[i]);
            if (citationList.length() + citation.length() >= 2000) {
                messageChannel.sendMessage(citationList).queue();
                citationList = new StringBuilder().append("> ").append(citation).append(System.lineSeparator());
            } else {
                citationList.append("> ").append(citation).append(System.lineSeparator());
            }
        }
        if (!citationList.isEmpty()) messageChannel.sendMessage(citationList).queue();
        generator.quitDriver();
    }
    private String[] formatSourcesIntoArray(String message, String command) {
        return message.replace(command, "").replaceAll(" ","").replaceAll("\n", " ").split(" ");
    }
}

