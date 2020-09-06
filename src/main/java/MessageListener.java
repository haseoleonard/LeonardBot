import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import users.UsersDAO;
import users.UsersDTO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MessageListener extends ListenerAdapter{
    private final String DEFAULT_PREFIX = "!";
    private String prefix = DEFAULT_PREFIX;
    private AudioPlayerManager audioPlayerManager;
    private final Map<Long,GuildMusicManager> musicManagerMap;

    public MessageListener() {
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        this.musicManagerMap = new HashMap<>();
    }

    private synchronized GuildMusicManager getGuildMusicManager(Guild guild){
        long guildID = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagerMap.get(guildID);
        if(musicManager==null){
            musicManager = new GuildMusicManager(audioPlayerManager);
            musicManagerMap.put(guildID,musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
//
        Message msg = event.getMessage();
        String Message = msg.getContentRaw();
        Guild guild = event.getGuild();
        Member member = msg.getMember();
        MessageChannel channel = event.getChannel();
//
//        try {
            if (Message.startsWith(prefix)) {
//
                String command = Message.substring(prefix.length()).split(" ", 2)[0].toLowerCase();
                String arg = "";
                if (Message.contains(" ")) {
                    arg = Message.substring(1).split(" ", 2)[1];
                }
//
                if (command.equals("prefix")) {
                    if (arg.trim().isEmpty()) {
                        setPrefix(DEFAULT_PREFIX);
                        channel.sendMessage("Prefix has been set to: " + DEFAULT_PREFIX).queue();
                    } else if (arg.contains(" ")) {
                        channel.sendMessage("Not a valid Prefix!!!").queue();
                    } else {
                        setPrefix(arg);
                        channel.sendMessage("Prefix has been set to: " + prefix).queue();
                    }
                } else if (command.equals("ping")) {
                    ping(msg);
                } else if (command.equals("avatar")) {
                    avatar(msg);
                } else if (command.equals("join")) {
                    joinVoiceChannel(msg);
                } else if (command.equals("leave")) {
                    leaveVoiceChannel(msg);
                } else if (command.equals("play")) {
                    loadAndPlay(msg, arg);
                } else if (command.equals("skip")) {
                    skipTrack(event.getChannel());
                } else if (command.equals("repeat")) {
                    setRepeat(event.getChannel());
                } else if (command.equals("shuffle")) {
                    shuffle(event.getChannel());
                } else if (command.equals("queue")) {
                    showQueue(event.getChannel());
                } else if (command.equals("userlist")) {
//                    sendUserList(msg);
                }
            }
//        }catch (SQLException | ClassNotFoundException ex){
//            ex.printStackTrace();
//        }
        super.onGuildMessageReceived(event);
    }

    private void ping(@NotNull Message message){
        long time = System.currentTimeMillis();
        MessageChannel channel = message.getChannel();
        channel.sendMessage(message.getAuthor().getAsMention()+" pong!").queue(response ->{
            response.editMessageFormat("Pong: %d ms",System.currentTimeMillis()-time).queue();
        });
    }

    private void sendUserList(@NotNull Message message) throws SQLException, ClassNotFoundException {
        StringBuilder builder = new StringBuilder();
        UsersDAO dao = new UsersDAO();
        int totalUser = dao.getUserList();
        if(totalUser>0){
            builder.append("Total Users: "+totalUser + "\n");
            List<UsersDTO> userList = dao.getUsersDTOList();
            for(UsersDTO user:userList){
                builder.append(user + "\n");
            }
        }else{
            builder.append("No Users in List");
        }
        message.getChannel().sendMessage(builder).queue();
    }
    private void setPrefix(String prefix){
        this.prefix = prefix;
    }

    private void avatar(@NotNull Message message){
        MessageChannel channel = message.getChannel();
        if(!message.getMentionedMembers().isEmpty()){
            channel.sendMessage(message.getMentionedMembers().get(0).getUser().getAvatarUrl()).queue();
        }else {
            channel.sendMessage(message.getAuthor().getAvatarUrl()).queue();
        }
    }

    private void joinVoiceChannel(@NotNull Message message) {
        Member member = message.getMember();
        VoiceChannel connectedVoiceChannel = member.getVoiceState().getChannel();
        AudioManager audioManager = message.getGuild().getAudioManager();
        if (!audioManager.isConnected()){
            if (connectedVoiceChannel != null) {
                member.getGuild().getAudioManager().openAudioConnection(connectedVoiceChannel);
            } else {
                message.getChannel().sendMessage("You must join a voice channel before using this feature!").queue();
            }
        }else{
            message.getChannel().sendMessage("Already Connected!!").queue();
        }
    }

    private void leaveVoiceChannel(@NotNull Message message){
        if(message.getGuild().getAudioManager().isConnected()) {
//            GuildMusicManager musicManager = getGuildMusicManager(message.getGuild());
            message.getGuild().getAudioManager().closeAudioConnection();
            message.getChannel().sendMessage("Disconnected from voice channel!").queue();
        }
    }
    private void loadAndPlay(final Message message, final String trackUrl){
        TextChannel channel = message.getTextChannel();
        Member member = message.getMember();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        GuildMusicManager musicManager = getGuildMusicManager(message.getGuild());

        audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue: "+ track.getInfo().title).queue();

                play(channel.getGuild().getAudioManager(),voiceChannel,musicManager,track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if(firstTrack==null){
                    firstTrack=playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue: "+firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild().getAudioManager(),voiceChannel ,musicManager,firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Couldn't play: "+exception.getMessage()).queue();
            }
        });
    }

    private void setRepeat(TextChannel textChannel){
        AudioManager audioManager = textChannel.getGuild().getAudioManager();
        if(!audioManager.isConnected()){
            return;
        }
        GuildMusicManager musicManager = getGuildMusicManager(textChannel.getGuild());

        int repeatmode = musicManager.trackScheduler.Repeat();
        String mode = "";
        if(repeatmode==0){
            mode="NOT REPEAT";
        }else if(repeatmode==1){
            mode="REPEAT ALL";
        }else if(repeatmode==2){
            mode="REPEAT ONE";
        }
        textChannel.sendMessage("Repeat Mode has been set to: "+mode).queue();
    }

    private void shuffle(TextChannel channel){
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        musicManager.trackScheduler.shuffle();

        channel.sendMessage("Queue Shuffled!");
    }

    private void showQueue(TextChannel channel) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        List<String> queue = musicManager.trackScheduler.getQueueList();
        StringBuilder builder = new StringBuilder();

        builder.append("```");
        if (queue != null){
            for (int i = 0; i < queue.size(); i++) {
                builder.append(i+1);
                builder.append(" - " + queue.get(i) + "\n");
            }
        }else{
            builder.append("Queue is empty.");
        }
        builder.append("```");
        channel.sendMessage(builder.toString()).queue();
    }
    private void play(AudioManager audioManager,VoiceChannel voiceChannel, GuildMusicManager guildMusicManager, AudioTrack audioTrack){
        if(!audioManager.isConnected()){
            audioManager.openAudioConnection(voiceChannel);
        }
        guildMusicManager.trackScheduler.queue(audioTrack);
    }

    private void skipTrack(TextChannel channel){
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        musicManager.trackScheduler.nextTrack(true);

        channel.sendMessage("Skipping to next track.").queue();
    }
}
