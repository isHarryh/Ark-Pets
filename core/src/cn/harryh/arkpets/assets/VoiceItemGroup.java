package cn.harryh.arkpets.assets;

import java.util.HashMap;


public class VoiceItemGroup {
    private HashMap<String, VoiceItem> variations;

    public VoiceItemGroup(HashMap<String, VoiceItem> map) {
        variations = map;
    }

    public VoiceItemGroup() {
        this(new HashMap<>());
    }

    public HashMap<String, VoiceItem> getVariations() {
        return variations;
    }
}
