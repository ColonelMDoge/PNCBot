package groqbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.net.*;

public class GroqMessageEventHandler extends ListenerAdapter {
    final String CHANNEL_NAME = "bot-channel";
    final String MODEL_NAME = "llama-3.3-70b-versatile";
    final String API_KEY = System.getenv("GROQ_API_KEY");

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(179,179,179));

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;
        if (!messageChannel.getName().equals(CHANNEL_NAME)) return;

        String messageLine = event.getMessage().getContentRaw();
        if (messageLine.equals("!help ai")) {
            embedBuilder.setTitle("PNCBot AI!");
            embedBuilder.setDescription("""
                The PNCBot's AI can answer some questions asked from prompts!
                Note: The bot can be incorrect sometimes or not respond.
                Note: The bot is not able to send past Discord's 2000 character limit.
                Made by ``ColonelMDoge`` with the use of ``Groq AI``.

                Here are the list of the commands this bot can do:""");
            embedBuilder.addField("Commands:", """
                ``!help ai`` - Opens this message again.
                ``!ask <prompt>`` - Asks the bot a question with the given prompt.
                """, false);
            embedBuilder.setFooter("This feature of the bot was made with https://console.groq.com/playground");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.startsWith("!ask ") && messageLine.split(" ")[1] != null) {
            String question = messageLine.substring(5);
            try {
                URL url = new URI("https://api.groq.com/openai/v1/chat/completions").toURL();
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");

                String jsonBody = "{\"model\": \"" + MODEL_NAME + "\", " +
                        "\"messages\": [{\"role\": " +
                        "\"user\", " +
                        "\"content\": \"" + question + "\"},{\"role\": \"system\", " +
                        "\"content\": \"You are the PNCBot, the autonomous bot that monitors " +
                        "the actions of PNC Logistics to make sure all tasks perform smoothly. " +
                        "You do not explicitly state your purpose all the time.\"}]}";

                httpURLConnection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonBody);
                writer.flush();
                writer.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    output.append(line);
                }
                br.close();

                String start = "\"role\":\"assistant\",\"content\":\"";
                String end = "\"},\"logprobs\":null,\"finish_reason\":\"stop\"}";
                String parsedOutput = output.substring(output.indexOf(start) + start.length(), output.indexOf(end)).replace("\\n", System.lineSeparator());
                if (parsedOutput.length() >= 2000)  {
                    messageChannel.sendMessage("Looks like my output exceeds Discord's 2000 character limit. Sending as a file...").queue();
                    messageChannel.sendMessage("Here is your file:")
                            .addFiles(FileUpload.fromData(new ByteArrayInputStream(parsedOutput.getBytes()), "message.txt"))
                            .queue();
                } else {
                    messageChannel.sendMessage(parsedOutput).queue();
                }
            } catch (URISyntaxException | IOException e) {
                messageChannel.sendMessage("Looks like I broke somewhere! Please retype your request in a different way!").queue();
                throw new RuntimeException(e);
            }
        }
    }
}
