package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.concurrent.SocketClient;

import java.util.LinkedList;
import java.util.Queue;


public class AudioManager {
    private final Queue<AudioPlayer.PlayRequest> queue = new LinkedList<AudioPlayer.PlayRequest>();
    private final AudioPlayer player;
    private SocketClient client;

    public AudioManager(AudioPlayer player,SocketClient client) {
        this.player = player;
        this.client = client;
    }

    public void enqueue(String name,float pan,float vol) {
        queue.add(new AudioPlayer.PlayRequest(name,pan,vol));
        //client.se();
    }

    public void dequeue() {
        player.playAudio(queue.poll());
    }
}
