/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.*;


/** The Asset Controller corresponding to a specified asset
 */
public class AssetCtrl {
    public File assetDir;
    @Deprecated public String assetId;
    public String type;
    public String style;
    public String name;
    public String appellation;
    public String skinGroupId;
    public String skinGroupName;
    public JSONArray sortTags;
    public JSONObject assetList;

    private AssetAccessor accessor;

    protected static final String separator = File.separator;
    protected static final String[] extensions = {".atlas", ".png", ".skel"};

    private AssetCtrl() {
    }

    /** Get the directory where the asset files located in.
     * @return A relative path string.
     */
    public String getLocation() {
        return assetDir.toString();
    }

    /** Get the Asset Accessor of this asset.
     * @return An Asset Accessor instance.
     */
    public AssetAccessor getAccessor() {
        if (accessor == null)
            accessor = new AssetAccessor(assetList);
        return accessor;
    }

    /** Get a single asset controller instance using the given asset directory.
     * @param $assetDir File instance of the specified asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return The asset controller instance.
     */
    public static AssetCtrl getAssetCtrl(File $assetDir, JSONObject $modelsDataset) {
        if (AssetVerifier.isValidAsset($assetDir, $modelsDataset)) {
            AssetCtrl assetCtrl = $modelsDataset.getObject($assetDir.getName(), AssetCtrl.class);
            assetCtrl.assetDir = $assetDir;
            if (assetCtrl.assetList == null && assetCtrl.assetId != null) {
                // Compatible to lower version
                HashMap<String, Object> defaultFileMap = new HashMap<>();
                for (String fileType : extensions)
                    defaultFileMap.put(fileType, assetCtrl.assetId + fileType);
                assetCtrl.assetList = new JSONObject(defaultFileMap);
            }
            return assetCtrl;
        }
        return new AssetCtrl();
    }

    /** Get asset controller instances from the given root directory.
     * @param $rootDir File instance of the specified root directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return The list containing asset controller instances.
     */
    public static ArrayList<AssetCtrl> getAllAssetCtrls(File $rootDir, JSONObject $modelsDataset) {
        ArrayList<AssetCtrl> list = new ArrayList<>();
        /* WorkDir
         *  +-rootDir
         *  |-+-subDir1(as key)
         *  | |---xxx.atlas
         *  | |---xxx.png
         *  | |---xxx.skel
         *  |---subDir2
         *  |---...
         */
        if ($rootDir.isDirectory()) {
            File[] subDirs = $rootDir.listFiles(fp -> fp.getParent().equals($rootDir.getPath()));
            if (subDirs == null)
                return list;
            for (File subDir : subDirs) {
                if ($modelsDataset.containsKey(subDir.getName())) {
                    if (!AssetVerifier.isExistedAsset(subDir, $modelsDataset)) {
                        //System.out.println("Not Integral: " + subDir.getName());
                        continue;
                    }
                    list.add(getAssetCtrl(subDir, $modelsDataset));
                }
            }
        }
        return list;
    }

    /** Sort the asset controllers.
     * @param $assetList The specified asset controllers.
     * @return The sorted list.
     */
    public static ArrayList<AssetCtrl> sortAssetCtrls(ArrayList<AssetCtrl> $assetList) {
        ArrayList<AssetCtrl> newList = new ArrayList<>();
        for (AssetCtrl i : $assetList) {
            boolean flag = true;
            for (AssetCtrl j : newList) {
                if (j.equals(i)) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                newList.add(i);
        }
        return newList;
    }

    /** Get all assets' locations of the given asset controllers.
     * @param $assetList The specified asset controllers.
     * @return The list containing asset locations.
     */
    public static ArrayList<String> getAssetLocations(ArrayList<AssetCtrl> $assetList) {
        ArrayList<String> result = new ArrayList<>();
        for (AssetCtrl asset : $assetList)
            result.add(asset.getLocation());
        return result;
    }

    /** Search assets by keywords in the given asset controller list.
     * @param $keywords The keywords.
     * @param $assetList The specified asset controller list.
     * @return The list containing asset controller instances that matches the keywords.
     */
    public static ArrayList<AssetCtrl> searchByKeyWords(String $keywords, ArrayList<AssetCtrl> $assetList) {
        if ($keywords.isEmpty())
            return $assetList;
        String[] wordList = $keywords.split(" ");
        ArrayList<AssetCtrl> result = new ArrayList<>();
        for (AssetCtrl asset : $assetList) {
            for (String word : wordList) {
                if (asset.name != null &&
                        asset.name.toLowerCase().contains(word.toLowerCase())) {
                    result.add(asset);
                }
            }
            for (String word : wordList) {
                if (asset.appellation != null &&
                        asset.appellation.toLowerCase().contains(word.toLowerCase())) {
                    if (!result.contains(asset))
                        result.add(asset);
                }
            }
        }
        return result;
    }

    /** Search index by relative asset path in the given asset controller list.
     * @param $assetRelPath The relative asset path (without ext), like {@code "models\xxx_xxx"}.
     * @param $assetList The specified asset controller list.
     * @return The index of the 1st matched asset, otherwise {@code 0} will be return by default.
     */
    public static int searchByAssetRelPath(String $assetRelPath, ArrayList<AssetCtrl> $assetList) {
        if ($assetRelPath.isEmpty())
            return 0;
        String assetId = new File($assetRelPath).getName();
        for (int i = 0; i < $assetList.size(); i++) {
            if ($assetList.get(i).assetDir.getName().equalsIgnoreCase(assetId))
                return i;
        }
        return 0;
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
        if (obj instanceof AssetCtrl) {
            return ((AssetCtrl)obj).assetDir.equals(assetDir);
        }
        return false;
    }

    /** The Asset Accessor providing methods to get the resource files of the asset
     * @since 2.2
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
                Logger.error("AssetCtrl", "Failed to establish an asset accessor, details see below.", e);
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
            Logger.warn("AssetCtrl", "getAllFilesOf() Method has returned an empty list.");
            return new String[0];
        }

        public String getFirstFileOf(String fileType) {
            String[] all = getAllFilesOf(fileType);
            if (all != null && all.length > 0)
                return all[0];
            Logger.warn("AssetCtrl", "getFirstFileOf() Method has returned null.");
            return null;
        }

        public boolean isAvailable() {
            return !map.isEmpty() && !list.isEmpty();
        }
    }

    public enum AssetStatus {
        NONE, VALID, EXISTED, CHECKED
    }

    /** The Asset Verifier providing methods to verify the integrity of the asset
     * @since 2.2
     */
    public static class AssetVerifier {
        private final JSONObject data;

        /** Initialize an Asset Verifier using the given dataset.
         * @param $modelsDataset The specified dataset.
         */
        public AssetVerifier(JSONObject $modelsDataset) {
            data = $modelsDataset;
        }

        /** Verify the integrity of the given asset.
         * @param $assetDir The asset directory.
         * @return Asset Status enumeration.
         */
        public AssetStatus verify(File $assetDir) {
            AssetStatus result = AssetStatus.NONE;
            if (isValidAsset($assetDir, data)) {
                result = AssetStatus.VALID;
                if (isExistedAsset($assetDir, data)) {
                    result = AssetStatus.EXISTED;
                    if (isCheckedAsset($assetDir, data)) {
                        result = AssetStatus.CHECKED;
                    }
                }
            }
            return result;
        }

        private static boolean isValidAsset(File $assetDir, JSONObject $modelsDataset) {
            if ($assetDir == null || $modelsDataset == null)
                return false;
            if (!$modelsDataset.containsKey($assetDir.getName())) {
                Logger.warn("Verifier", "The asset directory " + $assetDir.getPath() + " is undefined.");
                return false;
            }
            return true;
        }

        private static boolean isExistedAsset(File $assetDir, JSONObject $modelsDataset) {
            try {
                if (!$assetDir.isDirectory()) {
                    Logger.warn("Verifier", "The asset directory " + $assetDir.getPath() + " is missing.");
                    return false;
                }
                return true;
            } catch (Exception e) {
                Logger.warn("Verifier", "Failed to handle the asset " + $assetDir.getName());
                return false;
            }
        }

        private static boolean isCheckedAsset(File $assetDir, JSONObject $modelsDataset) {
            try {
                AssetCtrl assetCtrl = getAssetCtrl($assetDir, $modelsDataset);
                if (!assetCtrl.getAccessor().isAvailable())
                    return true; // Skip data-emptied asset (marked as verified)
                ArrayList<String> existed = new ArrayList<>(List.of(Objects.requireNonNull($assetDir.list())));
                existed.replaceAll(String::toLowerCase);
                for (String fileName : assetCtrl.getAccessor().getAllFiles()) {
                    fileName = fileName.toLowerCase();
                    if (!existed.contains(fileName)) {
                        Logger.warn("Verifier", "The asset file " + fileName + " (" + $assetDir + ") is missing.");
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                Logger.warn("Verifier", "Failed to handle the asset " + $assetDir.getName());
                return false;
            }
        }
    }
}
