/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.assets;

import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

import static cn.harryh.arkpets.ArkConfig.getWorkingDirectory;


public class ModelsDataset {
    public final HashMap<String, File> storageDirectory;
    public final HashMap<String, String> sortTags;
    public final String gameDataVersionDescription;
    public final String gameDataServerRegion;
    public final AssetItemGroup data;
    public final Version arkPetsCompatibility;

    public ModelsDataset(JSONObject jsonObject) {
        this(jsonObject.toJavaObject(ModelsDatasetBean.class));
    }

    protected ModelsDataset(ModelsDatasetBean bean) {
        Objects.requireNonNull(bean);

        storageDirectory = new HashMap<>();
        if (bean.storageDirectory == null || bean.storageDirectory.isEmpty())
            throw new DatasetKeyException("storageDirectory");
        for (String key : bean.storageDirectory.keySet())
            storageDirectory.put(key, Path.of(bean.storageDirectory.get(key)).toFile());

        sortTags = bean.sortTags;
        gameDataVersionDescription = bean.gameDataVersionDescription;
        gameDataServerRegion = bean.gameDataServerRegion;

        if (bean.data == null || bean.data.isEmpty())
            throw new DatasetKeyException("data");
        data = new AssetItemGroup();
        for (String key : bean.data.keySet()) {
            // Pre deserialization
            AssetItem assetItem = bean.data.get(key).toJavaObject(AssetItem.class);
            // Make up for `assetDir` field
            if (assetItem == null || !storageDirectory.containsKey(assetItem.type))
                throw new DatasetKeyException("type");
            assetItem.assetDir = Path.of(getWorkingDirectory(), storageDirectory.get(assetItem.type).toString(), key).toFile();
            // Compatible to lower version dataset
            if (assetItem.assetList == null && assetItem.assetId != null && assetItem.checksum != null) {
                HashMap<String, Object> defaultFileMap = new HashMap<>();
                for (String fileType : AssetItem.extensions)
                    defaultFileMap.put(fileType, assetItem.assetId + fileType);
                assetItem.assetList = new JSONObject(defaultFileMap);
            }
            data.add(assetItem);
        }
        data.sort();

        arkPetsCompatibility = new Version(bean.arkPetsCompatibility);
    }


    public static class DatasetKeyException extends IllegalArgumentException {
        public DatasetKeyException(String keyName) {
            super("The key \"" + keyName + "\" not found or invalid.");
        }
    }


    protected static class ModelsDatasetBean implements Serializable {
        private HashMap<String, String> storageDirectory;
        private HashMap<String, String> sortTags;
        private String gameDataVersionDescription;
        private String gameDataServerRegion;
        private HashMap<String, JSONObject> data;
        private int[] arkPetsCompatibility;

        @JSONField
        public void setStorageDirectory(HashMap<String, String> storageDirectory) {
            this.storageDirectory = storageDirectory;
        }

        @JSONField
        public void setSortTags(HashMap<String, String> sortTags) {
            this.sortTags = sortTags;
        }

        @JSONField
        public void setGameDataVersionDescription(String gameDataVersionDescription) {
            this.gameDataVersionDescription = gameDataVersionDescription;
        }

        @JSONField
        public void setGameDataServerRegion(String gameDataServerRegion) {
            this.gameDataServerRegion = gameDataServerRegion;
        }

        @JSONField
        public void setData(HashMap<String, JSONObject> data) {
            this.data = data;
        }

        @JSONField
        public void setArkPetsCompatibility(int[] arkPetsCompatibility) {
            this.arkPetsCompatibility = arkPetsCompatibility;
        }
    }
}
