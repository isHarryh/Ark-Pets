/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.assets;

import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;


/** One Asset Item is corresponding to one certain local Spine asset.
 */
public class AssetItem implements Serializable {
    @JSONField(serialize = false)
    public File assetDir;
    @JSONField
    public String assetId;
    @JSONField
    public String type;
    @JSONField
    public String style;
    @JSONField
    public String name;
    @JSONField
    public String appellation;
    @JSONField
    public String skinGroupId;
    @JSONField
    public String skinGroupName;
    @JSONField
    public JSONArray sortTags;
    @JSONField
    public JSONObject assetList;
    /** @deprecated Legacy field in old version dataset */ @JSONField @Deprecated
    public JSONObject checksum;

    private AssetAccessor accessor;

    protected static final String[] extensions = {".atlas", ".png", ".skel"};

    private AssetItem() {
    }

    /** Gets the directory where the asset files located in.
     * @return A relative path string.
     */
    @JSONField(serialize = false)
    public String getLocation() {
        return assetDir.toString();
    }

    /** Gets the Asset Accessor of this asset.
     * @return An Asset Accessor instance.
     */
    @JSONField(serialize = false)
    public AssetAccessor getAccessor() {
        if (accessor == null)
            accessor = new AssetAccessor(assetList);
        return accessor;
    }

    /** Verifies the integrity of the necessary fields of this {@code AssetItem}.
     * @return {@code true} if all the following conditions are satisfied, otherwise {@code false}:
     *          1. Both {@code assetDir} and {@code type} are not {@code null}.
     *          2. The {@code AssetAccessor} is available.
     */
    @JSONField(serialize = false)
    public boolean isValid() {
        return assetDir != null && type != null && getAccessor().isAvailable();
    }

    /** Verifies the existence of the target local directory.
     * @return {@code true} if all the following conditions are satisfied, otherwise {@code false}:
     *          1. The method {@code isValid()} returns {@code true}.
     *          2. The local {@code assetDir} exists.
     */
    @JSONField(serialize = false)
    public boolean isExisted() {
        return isValid() && assetDir.isDirectory();
    }

    /** Verifies the integrity of the related local files.
     * @return {@code true} if all the following conditions are satisfied, otherwise {@code false}:
     *          1. The method {@code isExisted()} returns {@code true}.
     *          2. All the local files exist.
     */
    @JSONField(serialize = false)
    public boolean isChecked() {
        if (isExisted()) {
            try {
                ArrayList<String> existed = new ArrayList<>(List.of(Objects.requireNonNull(assetDir.list())));
                existed.replaceAll(String::toLowerCase);
                for (String fileName : getAccessor().getAllFiles()) {
                    fileName = fileName.toLowerCase();
                    if (!existed.contains(fileName)) {
                        Logger.warn("Asset", "The asset file " + fileName + " (" + assetDir.getName() + ") is missing.");
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                Logger.warn("Asset", "Failed to check the asset " + assetDir.getName());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return assetDir.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssetItem) {
            return ((AssetItem)obj).assetDir.equals(assetDir);
        }
        return false;
    }


    /** The Asset Accessor providing methods to get the resource files of the asset.
     * @since ArkPets 2.2
     */
    public static class AssetAccessor {
        private final ArrayList<String> list;
        private final HashMap<String, ArrayList<String>> map;

        public AssetAccessor(JSONObject fileMap) {
            ArrayList<String> list = new ArrayList<>();
            HashMap<String, ArrayList<String>> map = new HashMap<>();
            try {
                if (fileMap != null && !fileMap.isEmpty()) {
                    for (String fileType : fileMap.keySet()) {
                        try {
                            JSONArray someFiles; // Try to get as array
                            if ((someFiles = fileMap.getJSONArray(fileType)) != null) {
                                var temp = someFiles.toJavaList(String.class);
                                list.addAll(temp);
                                map.put(fileType, new ArrayList<>(temp));
                            }
                        } catch (com.alibaba.fastjson.JSONException | com.alibaba.fastjson2.JSONException ex) {
                            String oneFile; // Try to get as string
                            if ((oneFile = fileMap.getString(fileType)) != null) {
                                list.add(oneFile);
                                map.put(fileType, new ArrayList<>(List.of(oneFile)));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Asset", "Failed to establish an asset accessor, details see below.", e);
                list = new ArrayList<>();
                map = new HashMap<>();
            }
            this.list = list;
            this.map = map;
        }

        public String[] getAllFiles() {
            return list.toArray(new String[0]);
        }

        public String[] getAllFilesOf(String fileType) {
            if (map.containsKey(fileType))
                return map.get(fileType).toArray(new String[0]);
            Logger.warn("Asset", "getAllFilesOf() Method has returned an empty list.");
            return new String[0];
        }

        public String getFirstFileOf(String fileType) {
            String[] all = getAllFilesOf(fileType);
            if (all != null && all.length > 0)
                return all[0];
            Logger.warn("Asset", "getFirstFileOf() Method has returned null.");
            return null;
        }

        public boolean isAvailable() {
            return !map.isEmpty() && !list.isEmpty();
        }
    }


    /** The Asset Property Extractor specializing in extracting a specified property.
     * @param <T> The type of the specified property, typically {@code String}.
     * @since ArkPets 2.2
     */
    public interface PropertyExtractor<T> extends Function<AssetItem, Set<T>> {
        /** Extracts the specified property of the given Asset Item.
         * @param assetItem The given Asset Item.
         * @return A value {@link Set} of the extracted property.
         */
        @Override
        Set<T> apply(AssetItem assetItem);

        PropertyExtractor<String> ASSET_ITEM_TYPE            = item -> item.type          == null ? Set.of() : Set.of(item.type);
        PropertyExtractor<String> ASSET_ITEM_STYLE           = item -> item.style         == null ? Set.of() : Set.of(item.style);
        PropertyExtractor<String> ASSET_ITEM_SKIN_GROUP_NAME = item -> item.skinGroupName == null ? Set.of() : Set.of(item.skinGroupName);
        PropertyExtractor<String> ASSET_ITEM_SORT_TAGS       = item -> new HashSet<>(item.sortTags.toJavaList(String.class));
    }
}
