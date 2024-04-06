package musicplayer.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> blockingQueue;
    private final MessageChannel messageChannel;
    private boolean isRepeat = false;

    public TrackScheduler(AudioPlayer audioPlayer, MessageChannel messageChannel) {
        this.audioPlayer = audioPlayer;
        this.messageChannel = messageChannel;
        blockingQueue = new LinkedBlockingQueue<>();
    }
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (isRepeat) {
            audioPlayer.startTrack(track.makeClone(), false);
            return;
        }
        if (blockingQueue.peek() != null) {
            getTrackInformation(blockingQueue.peek(), messageChannel);
            audioPlayer.startTrack(blockingQueue.poll(), false);
        }
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void queue(AudioTrack audioTrack) {
        if(!audioPlayer.startTrack(audioTrack, true)) {
            blockingQueue.offer(audioTrack);
        } else {
            getTrackInformation(audioTrack, messageChannel);
        }
    }
    private void getTrackInformation(AudioTrack audioTrack, MessageChannel messageChannel) {
        EmbedBuilder trackInformation = new EmbedBuilder();
        long duration = audioTrack.getInfo().length;
        trackInformation.setAuthor("Now playing: " + audioTrack.getInfo().title);
        trackInformation.setDescription("By: " + audioTrack.getInfo().author + "\n" +
                                        "Duration: " + (duration / 60000) + " min(s) " +  String.format("%1.0f", (((duration / 60000.0) - (duration / 60000)) * 60)) + " sec" + "\n" +
                                        "Link: " + audioTrack.getInfo().uri);
        trackInformation.setThumbnail("https://img.youtube.com/vi/" + audioTrack.getInfo().identifier + "/0.jpg");
        trackInformation.setColor(new Color(179,179,179));
        messageChannel.sendMessage("").setEmbeds(trackInformation.build()).queue();
    }
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    public BlockingQueue<AudioTrack> getBlockingQueue() {
        return blockingQueue;
    }
    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }
}
