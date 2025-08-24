package cn.harryh.arkpets.audio;

import cn.harryh.arkpets.concurrent.SocketClient;

import java.util.LinkedList;
import java.util.Queue;


public class AudioManager {
    private final Queue<AudioPlayer.PlayRequest> queue = new LinkedList<>();
    private final AudioPlayer player;
    private final SocketClient client;
    private boolean voicing;

    public AudioManager(AudioPlayer player,SocketClient client) {
        this.player = player;
        this.client = client;
    }

    public void enqueue(String name,float pan,float vol) {
        queue.add(new AudioPlayer.PlayRequest(name,pan,vol));
    }

    public void dequeue() {
        player.playAudio(queue.poll());
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public boolean isVoicing() {
        return voicing;
    }

    public void setVoicing(boolean voicing) {
        this.voicing = voicing;
    }

    @Override
    public String toString() {
        return "AudioManager [Pending %d, Voicing %b], Queue %s".formatted(queue.size(),voicing,queue.toString());
    }
}
