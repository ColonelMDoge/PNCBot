package musicplayer.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class GuildManager {
    private final TrackScheduler trackScheduler;
    private final AudioForwarder audioForwarder;

    public GuildManager(AudioPlayerManager audioPlayerManager, MessageChannel messageChannel) {
        AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        audioPlayer.setVolume(50);
        trackScheduler = new TrackScheduler(audioPlayer, messageChannel);
        audioPlayer.addListener(trackScheduler);
        audioForwarder = new AudioForwarder(audioPlayer);
    }
    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }
    public AudioForwarder getAudioForwarder() {
        return audioForwarder;
    }
}
