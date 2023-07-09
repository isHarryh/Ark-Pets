/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class AssetCtrl {
    public File assetDir;
    public String assetId;
    public String type;
    public String style;
    public String name;
    public String appellation;
    public String skinGroupId;
    public String skinGroupName;
    public JSONArray sortTags;
    public JSONObject checksum;
    static final String separator = File.separator;
    static final String[] extensions = {".atlas", ".png", ".skel"};

    private AssetCtrl() {
    }

    /** Get the asset file path with the given extension.
     * @param $fileExt The specified file extension.
     * @return The file path.
     */
    public String getAssetFilePath(String $fileExt) {
        return assetDir + separator + assetId + $fileExt;
    }

    /** Get the asset file path with the given extension.
     * @param $assetDir File instance of the asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @param $fileExt The specified file extension.
     * @return The file path.
     */
    public static String getAssetFilePath(File $assetDir, JSONObject $modelsDataset, String $fileExt) {
        String assetId = $modelsDataset.getJSONObject($assetDir.getName()).getObject("assetId", String.class);
        if (assetId == null)
            return null;
        return $assetDir.getPath() + separator + assetId + $fileExt;
    }

    /** Get the asset file name with the given extension.
     * @param $assetDir File instance of the asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @param $fileExt The specified file extension.
     * @return The file name.
     */
    public static String getAssetFileName(File $assetDir, JSONObject $modelsDataset, String $fileExt) {
        String assetId = $modelsDataset.getJSONObject($assetDir.getName()).getObject("assetId", String.class);
        if (assetId == null)
            return null;
        return assetId + $fileExt;
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
            return assetCtrl;
        }
        return new AssetCtrl();
    }

    /** Get a list of asset controller instances from the given root directory.
     * @param $rootDir File instance of the specified root directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return The list containing asset controller instances.
     */
    public static ArrayList<AssetCtrl> getAssetList(File $rootDir, JSONObject $modelsDataset) {
        ArrayList<AssetCtrl> list = new ArrayList<>();
        /* WorkDir
         *  +-rootDir
         *  |-+-subDir1(as key "assetId")
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
                    if (!AssetVerifier.isIntegralAsset(subDir, $modelsDataset)) {
                        //System.out.println("Not Integral: " + subDir.getName());
                        continue;
                    }
                    list.add(getAssetCtrl(subDir, $modelsDataset));
                }
            }
        }
        return list;
    }

    /** Sort the asset controller list.
     * @param $assetList The specified asset controller list.
     * @return The sorted list.
     */
    public static ArrayList<AssetCtrl> sortAssetList(ArrayList<AssetCtrl> $assetList) {
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

    /** Get a list of asset id in the given asset controller list.
     * @param $assetList The specified asset controller list.
     * @return The list containing asset ids.
     */
    public static ArrayList<String> getAssetIdList(ArrayList<AssetCtrl> $assetList) {
        ArrayList<String> result = new ArrayList<>();
        for (AssetCtrl asset : $assetList) {
            if (asset.assetId != null)
                result.add(asset.assetId);
        }
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
            if ($assetList.get(i).assetId != null &&
                    $assetList.get(i).assetId.equalsIgnoreCase(assetId)) {
                return i;
            }
        }
        return 0;
    }

    public static JSONObject getChecksum(File $assetDir, JSONObject $modelsDataset) {
        return $modelsDataset.getJSONObject($assetDir.getName()).getJSONObject("checksum");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetDir, assetId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssetCtrl) {
            return ((AssetCtrl)obj).assetDir.equals(assetDir) && ((AssetCtrl)obj).assetId.equals(assetId);
        }
        return false;
    }

    public static class AssetVerifier {
        /** VALID: The asset is valid in the dataset. */
        public static final int VALID = 0x001;
        /** INTEGRAL: The asset files are existed. */
        public static final int INTEGRAL = 0x010;
        /** CHECKED: The asset files are verified with checksums. */
        public static final int CHECKED = 0x100;
        
        protected final JSONObject data;

        public AssetVerifier(JSONObject $modelsDataset) {
            data = $modelsDataset;
        }

        public int verify(File $assetDir) {
            int result = 0;
            if (isValidAsset($assetDir, data)) {
                result |= VALID;
                if (isIntegralAsset($assetDir, data)) {
                    result |= INTEGRAL;
                    if (isCheckedAsset($assetDir, data)) {
                        result |= CHECKED;
                    }
                }
            }
            return result;
        }

        protected static boolean isValidAsset(File $assetDir, JSONObject $modelsDataset) {
            if ($assetDir == null || $modelsDataset == null)
                return false;
            if (!$modelsDataset.containsKey($assetDir.getName())) {
                Logger.warn("Verifier", "The asset directory " + $assetDir.getPath() + " is undefined.");
                return false;
            }
            if (!$assetDir.isDirectory()) {
                Logger.warn("Verifier", "The asset directory " + $assetDir.getPath() + " is missing.");
                return false;
            }
            return true;
        }

        protected static boolean isIntegralAsset(File $assetDir, JSONObject $modelsDataset) {
            try {
                if (getChecksum($assetDir, $modelsDataset) == null)
                    return true; // Skip data-emptied asset (marked as verified)
                List<String> assetFileList = Arrays.asList(Objects.requireNonNull($assetDir.list()));
                for (String ext : extensions) {
                    String path = getAssetFileName($assetDir, $modelsDataset, ext);
                    if (!assetFileList.contains(path)) {
                        Logger.warn("Verifier", "The asset file " + path + " is missing.");
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                Logger.warn("Verifier", "Failed to handle the asset " + $assetDir.getName());
                return false;
            }
        }

        protected static boolean isCheckedAsset(File $assetDir, JSONObject $modelsDataset) {
            try {
                JSONObject checksum;
                if ((checksum = getChecksum($assetDir, $modelsDataset)) == null)
                    return true; // Skip data-emptied asset (marked as verified)
                for (String ext : extensions) {
                    File file = new File(Objects.requireNonNull(getAssetFilePath($assetDir, $modelsDataset, ext)));
                    if (!checksum.getObject(ext, String.class)
                            .equals(IOUtils.FileUtil.getMD5(file))) {
                        Logger.warn("Verifier", "The md5 of file " + getAssetFilePath($assetDir, $modelsDataset, ext) +
                                " is: " + IOUtils.FileUtil.getMD5(file) +
                                " but in the dataset, it was recorded as: " + checksum.getObject(ext, String.class));
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
