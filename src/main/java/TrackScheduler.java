import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.*;

public class TrackScheduler extends AudioEventAdapter {
    private final int NOT_REPEAT = 0;
    private final int REPEAT_ALL = 1;
    private final int REPEAT_ONE = 2;
    private final AudioPlayer player;
    private final Queue<AudioTrack> queue;
    //
    private int repeat = NOT_REPEAT;
    private AudioTrack curTrack;

    //
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }

    public int Repeat() {
        this.repeat++;
        if (this.repeat > 2) this.repeat = 0;
        return this.repeat;
    }

    public void queue(AudioTrack track) {
        this.curTrack = track.makeClone();
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack(boolean force) {
        AudioTrack nextTrack = null;
        if (this.repeat == NOT_REPEAT) {
            nextTrack = queue.poll();
            if(nextTrack!=null)this.curTrack = nextTrack.makeClone();
        } else if (this.repeat == REPEAT_ONE && !force) {
            nextTrack = this.curTrack.makeClone();
        } else if (this.repeat == REPEAT_ALL || force) {
            nextTrack = queue.poll();
            if(nextTrack!=null){
                curTrack = nextTrack.makeClone();
                queue.offer(this.curTrack.makeClone());
            }
        }
        player.startTrack(nextTrack,false);
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

    public List<String> getQueueList() {
        Queue<AudioTrack> tmp = new LinkedList<>();
        tmp.addAll(queue);
        List<String> queueList = new ArrayList<>();
        while (!tmp.isEmpty()) {
            queueList.add(tmp.poll().getInfo().title);
        }
        if (queueList.isEmpty()) queueList = null;
        return queueList;
    }
//    @Override
//    public void onTrackStart(AudioPlayer player, AudioTrack track) {
//
//    }


    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack(false);
        }
    }
}
