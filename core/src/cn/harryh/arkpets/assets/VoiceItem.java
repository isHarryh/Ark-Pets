package cn.harryh.arkpets.assets;

import java.io.Serializable;
import java.util.List;


public record VoiceItem(float duration, int size, List<VoiceClip> clips) implements Serializable {

    public record VoiceClip(
            String name,
            float start
    ) {
    }
}
