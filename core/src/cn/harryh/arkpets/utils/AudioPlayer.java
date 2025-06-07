package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.assets.VoiceItem;
import cn.harryh.arkpets.assets.VoiceItemGroup;
import cn.harryh.arkpets.assets.VoiceLang;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.OggInputStream;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;
import com.badlogic.gdx.utils.Timer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class AudioPlayer {
    private final HashMap<String, Sound> gdxSounds = new HashMap<>();
    private AudioSlicer slicer;
    private ByteBuffer pcmBuffer;
    private final Timer resetTimer = new Timer();
    private Status status = Status.UNAVAILABLE;
    private int sample;
    private int channel;

    private AudioPlayer(VoiceItem group, File audio) {
        try {
            OggInputStream oggInput = new OggInputStream(new FileInputStream(audio));
            ByteArrayOutputStream pcmOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            while (!oggInput.atEnd()) {
                int length = oggInput.read(buffer);
                if (length == -1) break;
                pcmOutput.write(buffer, 0, length);
            }
            this.sample = oggInput.getSampleRate();
            this.channel = oggInput.getChannels();
            this.pcmBuffer = ByteBuffer.wrap(pcmOutput.toByteArray());
            this.slicer = new AudioSlicer(group,oggInput.getSampleRate(),oggInput.getChannels());
            this.status = Status.READY;
        } catch (IOException e) {
            Logger.error("Audio", "Failed to read ogg file.", e);
        }
    }

    public static AudioPlayer loadAudio(File oggPath, VoiceItemGroup group, VoiceLang lang) {
        return new AudioPlayer(group.getVariation(lang), oggPath);
    }

    public void playAudio(String name, float pan, float vol) {
        if (status == Status.UNAVAILABLE) throw new IllegalStateException();
        if (status == Status.PLAYING) return;
        Sound sound = fetchSound(name);
        sound.play(vol, 1, pan);
        //playing = true;
    }

    public void dispose() {
        for (Sound sound : gdxSounds.values()) {
            sound.dispose();
        }
        status = Status.UNAVAILABLE;
    }

    private Sound fetchSound(String name) {
        Sound sound = gdxSounds.get(name);
        if (sound == null) {
            Logger.debug("Audio", "Loading sound: " + name);
            byte[] data = slicer.getSlice(pcmBuffer,name);
            sound = new OpenALSound((OpenALLwjgl3Audio) Gdx.audio);
            try {
                Method setupMethod = OpenALSound.class.getDeclaredMethod("setup", byte[].class, int.class, int.class);
                setupMethod.setAccessible(true);
                setupMethod.invoke(sound, data,channel,sample);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            gdxSounds.put(name, sound);
        }
        return sound;
    }

    public enum Status{
        UNAVAILABLE,
        READY,
        PLAYING,
    }
}
