package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.assets.VoiceItem;
import cn.harryh.arkpets.assets.VoiceItemGroup;
import cn.harryh.arkpets.assets.VoiceLang;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;
import com.badlogic.gdx.utils.Timer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;


public class AudioPlayer {
    private final HashMap<String, Sound> gdxSounds = new HashMap<>();
    private final AudioSlicer slicer;
    private final Timer resetTimer = new Timer();
    private boolean playing;
    private boolean disposed;

    private AudioPlayer(VoiceItem group, File audio) {
        this.slicer = new AudioSlicer(audio, group);
    }

    public static AudioPlayer loadAudio(File oggPath, VoiceItemGroup group, VoiceLang lang) {
        return new AudioPlayer(group.getVariation(lang), oggPath);
    }

    public void playAudio(String name, float pan, float vol) {
        if (disposed) throw new IllegalStateException();
        if (playing) return;
        Sound sound = fetchSound(name);
        sound.play(vol, 1, pan);
        //playing = true;
    }

    public void dispose() {
        for (Sound sound : gdxSounds.values()) {
            sound.dispose();
        }
        disposed = true;
    }

    private Sound fetchSound(String name) {
        Sound sound = gdxSounds.get(name);
        if (sound == null) {
            Logger.debug("Audio", "Loading sound: " + name);
            byte[] data = slicer.getSlice(name);
            sound = new OpenALSound((OpenALLwjgl3Audio) Gdx.audio);
            try {
                Method setupMethod = OpenALSound.class.getDeclaredMethod("setup", byte[].class, int.class, int.class);
                setupMethod.setAccessible(true);
                setupMethod.invoke(sound, data, slicer.getChannels(), slicer.getSampleRate());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            gdxSounds.put(name, sound);
        }
        return sound;
    }
}
