package musicplayer.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private static PlayerManager playerManager;
    private final MessageChannel messageChannel;
    private final Map<Long, GuildManager> guildManagerMap = new HashMap<>();

    private PlayerManager(MessageChannel messageChannel) {
        YoutubeAudioSourceManager youtubeAudioSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager();
        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        this.messageChannel = messageChannel;
    }
    public static PlayerManager get(MessageChannel messageChannel) {
        if (playerManager == null) {
            playerManager = new PlayerManager(messageChannel);
        }
        return playerManager;
    }
    public GuildManager getGuildManager(Guild guild) {
        return guildManagerMap.computeIfAbsent(guild.getIdLong(), (guildID) -> {
            GuildManager guildManager = new GuildManager(audioPlayerManager, messageChannel);
            guild.getAudioManager().setSendingHandler(guildManager.getAudioForwarder());
            return guildManager;
        });
    }
    public void play(Guild guild, String track) {
        GuildManager guildManager = getGuildManager(guild);
        TrackScheduler trackScheduler = guildManager.getTrackScheduler();
        audioPlayerManager.loadItemOrdered(guildManager, track, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                trackScheduler.queue(audioTrack);
                if (!trackScheduler.getBlockingQueue().isEmpty()) {
                    EmbedBuilder queuedSong = new EmbedBuilder();
                    queuedSong.setDescription("**Queued Song:** " + audioTrack.getInfo().title);
                    queuedSong.setColor(new Color(179,179,179));
                    messageChannel.sendMessage("").setEmbeds(queuedSong.build()).queue();
                }
            }
            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                if (audioPlaylist.isSearchResult()) {
                    trackScheduler.queue(audioPlaylist.getTracks().getFirst());
                    if (!trackScheduler.getBlockingQueue().isEmpty()) {
                        EmbedBuilder queuedSong = new EmbedBuilder();
                        queuedSong.setDescription("**Queued Song:** " + audioPlaylist.getTracks().getFirst().getInfo().title);
                        queuedSong.setColor(new Color(179,179,179));
                        messageChannel.sendMessage("").setEmbeds(queuedSong.build()).queue();
                    }
                    return;
                }
                for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
                    trackScheduler.queue(audioTrack);
                }
                if (!trackScheduler.getBlockingQueue().isEmpty() && trackScheduler.getBlockingQueue().size()!= audioPlaylist.getTracks().size() - 1) {
                    EmbedBuilder queuedPlaylist = new EmbedBuilder();
                    queuedPlaylist.setDescription("**Queued Playlist:** " + audioPlaylist.getName());
                    queuedPlaylist.setColor(new Color(179,179,179));
                    messageChannel.sendMessage("").setEmbeds(queuedPlaylist.build()).queue();
                }
            }
            @Override
            public void noMatches() {
                messageChannel.sendMessage("There were no matches found! Please try again.").queue();
            }
            @Override
            public void loadFailed(FriendlyException e) {
                messageChannel.sendMessage("The song failed to load!").queue();
            }
        });
    }
}
