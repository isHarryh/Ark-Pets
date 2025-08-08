package cn.harryh.arkpets.assets;

import java.util.TreeMap;


public class VoiceItemGroup {
    private final String key;
    private final TreeMap<VoiceLang, VoiceItem> variations;

    public VoiceItemGroup(String key,TreeMap<VoiceLang, VoiceItem> map) {
        this.key = key;
        variations = map;
    }

    public VoiceItemGroup() {
        this("",new TreeMap<>());
    }

    public TreeMap<VoiceLang, VoiceItem> getVariations() {
        return variations;
    }

    public VoiceItem getVariation(VoiceLang lang) {
        return variations.get(lang);
    }

    public String getKey() {
        return key;
    }
}
