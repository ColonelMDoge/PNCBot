import musicplayer.MusicMessageEventHandler;
import citationgenerator.CitationMessageEventHandler;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class PNCBot {
    public static void main(String[] args) {
        final String TOKEN = System.getenv("TOKEN");
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        jdaBuilder.setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new MusicMessageEventHandler(), new CitationMessageEventHandler())
                .build();
    }
}
