package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.assets.VoiceItem;
import com.badlogic.gdx.backends.lwjgl3.audio.OggInputStream;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class AudioSlicer {
    private ByteBuffer pcmBuffer;
    private int sampleRate;
    private int channels;
    private int bits = 16;
    private int bytesPerSample = 0;
    private final HashMap<String, Float> startTime = new HashMap<>();
    private final HashMap<String, Float> durationTime = new HashMap<>();

    public AudioSlicer(File oggFile, VoiceItem group) {
        try {
            OggInputStream oggInput = new OggInputStream(new FileInputStream(oggFile));
            this.sampleRate = oggInput.getSampleRate();
            this.channels = oggInput.getChannels();
            ByteArrayOutputStream pcmOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            while (!oggInput.atEnd()) {
                int length = oggInput.read(buffer);
                if (length == -1) break;
                pcmOutput.write(buffer, 0, length);
            }
            this.pcmBuffer = ByteBuffer.wrap(pcmOutput.toByteArray());
            this.bytesPerSample = sampleRate * channels * bits / 8;
        } catch (IOException e) {
            Logger.error("Audio", "Failed to read ogg file.", e);
        }
        for (int i = 0; i < group.clips().size(); i++) {
            float duration,start;
            VoiceItem.VoiceClip clip = group.clips().get(i);
            VoiceItem.VoiceClip nextClip = null;
            if (i < group.clips().size() - 1) {
                nextClip = group.clips().get(i + 1);
            }
            start = clip.start();
            startTime.put(clip.name(), start);
            if (nextClip != null) {
                duration = nextClip.start() - start;
                durationTime.put(clip.name(), duration);
            } else {
                duration = group.duration() - start;
                durationTime.put(clip.name(), duration);
            }
            Logger.debug("Audio", "Slice " + clip.name() + " Start: " + start + " Duration: " + duration);
        }
    }


    public byte[] getSlice(String name) {
        float start, duration;
        if(!(startTime.containsKey(name) && durationTime.containsKey(name))) {
            Logger.warn("Audio", "Slice " + name + " not found.");
            return null;
        }
        start = startTime.get(name);
        duration = durationTime.get(name);
        int startByte = (int) Math.ceil(start * bytesPerSample);
        int endByte = (int) Math.ceil(startByte + duration * bytesPerSample);
        if (endByte > pcmBuffer.limit()) {
            endByte = pcmBuffer.limit();
        }
        int pcmLength = endByte - startByte;
        byte[] dst = new byte[pcmLength+1];
        pcmBuffer.get(startByte, dst, 0, pcmLength);
        return dst;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannels() {
        return channels;
    }
}
