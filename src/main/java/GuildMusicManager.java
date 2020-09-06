import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    public final AudioPlayer player;
    public final TrackScheduler trackScheduler;

    public GuildMusicManager(AudioPlayerManager manager){
        this.player= manager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
    }

    public AudioPlayerSendHandler getSendHandler(){
        return new AudioPlayerSendHandler(player);
    }
}
