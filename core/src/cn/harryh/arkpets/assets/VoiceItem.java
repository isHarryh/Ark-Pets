package cn.harryh.arkpets.assets;

import java.util.List;


public record VoiceItem(float duration, int size, List<VoiceClip> clips) {

    public record VoiceClip(
            String name,
            float start
    ) {
    }
}
