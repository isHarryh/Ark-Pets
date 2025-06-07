package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.assets.VoiceItem;


import java.nio.ByteBuffer;
import java.util.HashMap;


public class AudioSlicer {
    private final int bits = 16;
    private final int bytesPerSample;
    private final HashMap<String, Float> startTime = new HashMap<>();
    private final HashMap<String, Float> durationTime = new HashMap<>();

    public AudioSlicer(VoiceItem group,int sampleRate,int channels) {
        this.bytesPerSample = sampleRate * channels * bits / 8;
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


    public byte[] getSlice(ByteBuffer pcmBuffer,String name) {
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

    public float getDuration(String name) {
        if(!durationTime.containsKey(name)) return 0;
        return durationTime.get(name);
    }

    public float getStartTime(String name) {
        if(!startTime.containsKey(name)) return 0;
        return startTime.get(name);
    }
}
