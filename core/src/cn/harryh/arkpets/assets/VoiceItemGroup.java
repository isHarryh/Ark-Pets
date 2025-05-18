package cn.harryh.arkpets.assets;

import java.util.HashMap;


public class VoiceItemGroup {
    private HashMap<VoiceLang, VoiceItem> variations;

    public VoiceItemGroup(HashMap<VoiceLang, VoiceItem> map) {
        variations = map;
    }

    public VoiceItemGroup() {
        this(new HashMap<>());
    }

    public HashMap<VoiceLang, VoiceItem> getVariations() {
        return variations;
    }

    public VoiceItem getVariation(VoiceLang lang) {
        return variations.get(lang);
    }
}
