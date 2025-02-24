package musicplayer.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import java.nio.ByteBuffer;

public class AudioForwarder implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final MutableAudioFrame mutableAudioFrame;
    private final ByteBuffer byteBuffer;
    public AudioForwarder(AudioPlayer audioPlayer) {
        mutableAudioFrame = new MutableAudioFrame();
        byteBuffer = ByteBuffer.allocate(1024);
        this.audioPlayer = audioPlayer;
        mutableAudioFrame.setBuffer(byteBuffer);
    }
    @Override
    public boolean canProvide() {
        return audioPlayer.provide(mutableAudioFrame);
    }
    @Override
    public ByteBuffer provide20MsAudio() { return byteBuffer.flip(); }
    @Override
    public boolean isOpus() {
        return true;
    }
}