package cn.harryh.arkpets.assets;

import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Pattern;



public class VoiceDataset {
    public final HashMap<String, File> storageDirectory;
    public final HashMap<String, HashMap<String, String>> localizations;
    public final HashMap<String, VoiceItemGroup> data;
    public final HashMap<String, Pattern> audioTypes;
    public final String gameDataVersionDescription;
    public final String gameDataServerRegion;
    public final String audioFormat;
    public final Version arkPetsCompatibility;

    public VoiceDataset(JSONObject object) {
        this(object.toJavaObject(VoiceDatasetBean.class));
    }

    protected VoiceDataset(VoiceDatasetBean bean) {
        storageDirectory = new HashMap<>();
        if (bean.storageDirectory == null || bean.storageDirectory.isEmpty())
            throw new DatasetKeyException("storageDirectory");
        for (String key : bean.storageDirectory.keySet())
            storageDirectory.put(key, Path.of(bean.storageDirectory.get(key)).toFile());
        data = new HashMap<>();
        for (String key : bean.data.keySet()) {
            JSONObject variations = bean.data.get(key).getJSONObject("variations");
            HashMap<VoiceLang, VoiceItem> vMap = variations.toJavaObject(new TypeReference<>() {
            });
            data.put(key, new VoiceItemGroup(vMap));
        }
        audioTypes = new HashMap<>();
        for (String key : bean.audioTypes.keySet())
            audioTypes.put(key, Pattern.compile(bean.audioTypes.get(key)));
        gameDataVersionDescription = bean.gameDataVersionDescription;
        gameDataServerRegion = bean.gameDataServerRegion;
        localizations = bean.localizations;
        audioFormat = bean.audioFormat;
        arkPetsCompatibility = new Version(bean.arkPetsCompatibility);
    }

    public String getLocalizedName(String lang,String id) {
        return localizations.get(lang).get(id);
    }

    protected static class VoiceDatasetBean implements Serializable {
        private HashMap<String, String> storageDirectory;
        private String gameDataVersionDescription;
        private String gameDataServerRegion;
        private String audioFormat;
        private HashMap<String, String> audioTypes;
        private HashMap<String, JSONObject> data;
        private HashMap<String, HashMap<String, String>> localizations;
        private int[] arkPetsCompatibility;

        public void setStorageDirectory(HashMap<String, String> storageDirectory) {
            this.storageDirectory = storageDirectory;
        }

        public void setAudioTypes(HashMap<String, String> audioTypes) {
            this.audioTypes = audioTypes;
        }

        public void setGameDataVersionDescription(String gameDataVersionDescription) {
            this.gameDataVersionDescription = gameDataVersionDescription;
        }

        public void setGameDataServerRegion(String gameDataServerRegion) {
            this.gameDataServerRegion = gameDataServerRegion;
        }

        public void setAudioFormat(String audioFormat) {
            this.audioFormat = audioFormat;
        }

        public void setData(HashMap<String, JSONObject> data) {
            this.data = data;
        }

        public void setLocalizations(HashMap<String, HashMap<String, String>> localizations) {
            this.localizations = localizations;
        }

        public void setArkPetsCompatibility(int[] arkPetsCompatibility) {
            this.arkPetsCompatibility = arkPetsCompatibility;
        }
    }
}
