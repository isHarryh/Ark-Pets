package cn.harryh.arkpets.audio;

import cn.harryh.arkpets.assets.VoiceItem;
import cn.harryh.arkpets.assets.VoiceItemGroup;
import cn.harryh.arkpets.assets.VoiceLang;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Timer;

import java.io.File;
import java.util.HashMap;


public class AudioPlayer {
    private final Music music;
    private final HashMap<String, Float> startTime = new HashMap<>();
    private final HashMap<String, Float> durationTime = new HashMap<>();
    private final Timer pauseTimer = new Timer();
    private boolean disposed;

    private AudioPlayer(VoiceItem group, File audio) {
        for (int i = 0; i < group.clips().size(); i++) {
            VoiceItem.VoiceClip clip = group.clips().get(i);
            VoiceItem.VoiceClip nextClip = null;
            if (i < group.clips().size() - 1) {
                nextClip = group.clips().get(i + 1);
            }
            startTime.put(clip.name(), clip.start());
            if (nextClip != null) {
                durationTime.put(clip.name(), nextClip.start() - clip.start());
            } else {
                durationTime.put(clip.name(), group.duration() - clip.start());
            }
        }
        music = Gdx.audio.newMusic(Gdx.files.absolute(audio.getAbsolutePath()));
        music.setLooping(false);
    }

    public static AudioPlayer loadAudio(File oggPath, VoiceItemGroup group, VoiceLang lang) {
        return new AudioPlayer(group.getVariation(lang), oggPath);
    }

    public void playAudio(String name, float pan, float vol) {
        if (disposed) throw new IllegalStateException();
        float start, duration;
        start = startTime.get(name);
        duration = durationTime.get(name);
        Logger.debug("Audio",
                "Playing " + name + " Pan: " + pan + " Vol: " + vol
                        + " Start: " + start + " Duration: " + duration
        );
        music.play();
        music.setPosition(start);
        music.setPan(pan, vol);
        pauseTimer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                music.pause();
            }
        }, duration);
    }

    public void dispose() {
        music.dispose();
        disposed = true;
    }
}
