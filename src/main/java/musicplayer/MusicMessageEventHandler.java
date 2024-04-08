package musicplayer;

import musicplayer.lavaplayer.PlayerManager;
import musicplayer.lavaplayer.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MusicMessageEventHandler extends ListenerAdapter {
    final String CHANNEL_NAME = "bot-channel";
    final String VOICE_CHANNEL_NAME = "general";
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if (!event.isFromGuild()) return;

        MessageChannel messageChannel = event.getChannel();
        String messageLine = event.getMessage().getContentRaw();
        VoiceChannel voiceChannel = event.getGuild().getVoiceChannelsByName(VOICE_CHANNEL_NAME,true).getFirst();
        AudioManager audioManager = voiceChannel.getGuild().getAudioManager();
        PlayerManager playerManager = PlayerManager.get(messageChannel);
        TrackScheduler trackScheduler = PlayerManager.get(messageChannel).getGuildManager(event.getGuild()).getTrackScheduler();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(179,179,179));

        if (event.getAuthor().isBot()) return;
        if (!messageChannel.getName().equals(CHANNEL_NAME)) return;
        if (messageLine.equals("!help")) {
            embedBuilder.setTitle("PNCBot Music Player!");
            embedBuilder.setDescription("""
                The PNCBot Music Player can play music from either Youtube or Soundcloud!
                Made by ``ColonelMDoge`` with help from ``devoxin``.

                Here are the list of the commands this bot can do:""");
            embedBuilder.addField("Commands:", """
                ``!help`` - Opens this message again.
                ``!play URL`` - Plays a song with the provided URL.
                ``!search ...`` - Searches for a YouTube song and plays the first occurrence.
                ``!pause`` - Pauses the current playing song.
                ``!resume`` - Resumes the current paused song.
                ``!loop`` - Loops the current song.
                ``!unloop`` - Unloops the current song.
                ``!volume #`` - Sets the volume to the provided number.
                ``!time #:##`` - Sets the timestamp of the song.
                ``!skip`` - Skips the current playing song.
                ``!skip #`` - Skips the specified number of times.
                ``!stop`` - Stops the bot from playing music.``""", false);
            embedBuilder.setFooter("This feature of the bot was made with https://github.com/devoxin/lavaplayer");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.startsWith("!play ") && messageLine.split(" ")[1] != null) {
            audioManager.openAudioConnection(voiceChannel);
            playerManager.play(event.getGuild(), messageLine.split(" ")[1]);
        }
        if (messageLine.startsWith("!search ") && messageLine.split(" ")[1] != null) {
            audioManager.openAudioConnection(voiceChannel);
            playerManager.play(event.getGuild(), "ytsearch:" + messageLine.replace("!search ", ""));
        }
        if (messageLine.equals("!stop")) {
            audioManager.closeAudioConnection();
            trackScheduler.getBlockingQueue().clear();
            trackScheduler.setRepeat(false);
            trackScheduler.getAudioPlayer().stopTrack();
        }
        if (messageLine.equals("!pause")) {
            trackScheduler.getAudioPlayer().setPaused(true);
            embedBuilder.setDescription("Current song paused...");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.equals("!resume")) {
            trackScheduler.getAudioPlayer().setPaused(false);
            embedBuilder.setDescription("Current song resumed...");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.equals("!loop")) {
            trackScheduler.setRepeat(true);
            embedBuilder.setDescription("Current song looped!");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.equals("!unloop")) {
            trackScheduler.setRepeat(false);
            embedBuilder.setDescription("Current song un-looped!");
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.startsWith("!volume ") && messageLine.split(" ")[1] != null) {
            trackScheduler.getAudioPlayer().setVolume(Integer.parseInt(messageLine.split(" ")[1]));
            int currentVolume = trackScheduler.getAudioPlayer().getVolume();
            embedBuilder.setDescription(currentVolume > 100 ? "Current volume: " + currentVolume + " **(WARNING: LOUD!)**" : "Current volume: " + currentVolume);
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.startsWith("!time ") && messageLine.split(" ")[1] != null) {
            String[] time = messageLine.split(" ")[1].split(":");
            trackScheduler.getAudioPlayer().getPlayingTrack().setPosition((Integer.parseInt(time[0]) * 60000L) + (Integer.parseInt(time[1]) * 1000L));
            long timeInMillis = trackScheduler.getAudioPlayer().getPlayingTrack().getPosition();
            embedBuilder.setDescription("Timestamp set to: " + (timeInMillis / 60000) + ":" + (timeInMillis % 60000 / 1000) + (timeInMillis % 60000 / 1000 > 10 ? "" : "0"));
            messageChannel.sendMessage("").setEmbeds(embedBuilder.build()).queue();
        }
        if (messageLine.equals("!skip")) { trackScheduler.getAudioPlayer().stopCurrentTrack(); }
        if (messageLine.startsWith("!skip ") && messageLine.split(" ")[1] != null) {
            for (int i = 0; i < Integer.parseInt(messageLine.split(" ")[1]) - 1; i++) {
                trackScheduler.getBlockingQueue().poll();
            }
            trackScheduler.getAudioPlayer().stopCurrentTrack();
        }
    }
}
