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

    public AssetCtrl() {
    }

    /** Get the asset file path with the given extension.
     * @param $fileExt The specified file extension.
     * @return The file path.
     */
    public String getAssetFilePath(String $fileExt) {
        return assetDir + separator + assetId + $fileExt;
    }

    /** Get the asset file name with the given extension.
     * @param $fileExt The specified file extension.
     * @return The file name.
     */
    public String getAssetFileName(String $fileExt) {
        return assetId + $fileExt;
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

    /** Judge whether the given asset directory is valid in the models' dataset.
     * @param $assetDir File instance of the specified asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return Judgment result.
     */
    public static boolean isValidAsset(File $assetDir, JSONObject $modelsDataset) {
        if ($assetDir == null || $modelsDataset == null)
            return false;
        return $modelsDataset.containsKey($assetDir.getName()) && $assetDir.isDirectory();
    }

    /** Judge whether the given asset directory has integral files.
     * More strict than <code>isValidAsset()</code>.
     * @param $assetDir File instance of the specified asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return Judgment result.
     */
    public static boolean isIntegralAsset(File $assetDir, JSONObject $modelsDataset) {
        if (!isValidAsset($assetDir, $modelsDataset))
            return false;
        try {
            List<String> assetFileList = Arrays.asList(Objects.requireNonNull($assetDir.list()));
            for (String ext : extensions) {
                if (!assetFileList.contains(getAssetFileName($assetDir, $modelsDataset, ext)))
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Judge whether the given asset directory has integral files and all of them are verified with hash.
     * More strict than <code>isIntegralAsset()</code>.
     * @param $assetDir File instance of the specified asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return Judgment result.
     */
    public static boolean isVerifiedAsset(File $assetDir, JSONObject $modelsDataset) {
        if (!isIntegralAsset($assetDir, $modelsDataset))
            return false;
        if (!$modelsDataset.getJSONObject($assetDir.getName()).containsKey("checksum"))
            return false;
        try {
            JSONObject checksum = $modelsDataset.getJSONObject($assetDir.getName()).getJSONObject("checksum");
            if (checksum == null)
                return false;
            String assetId = $modelsDataset.getJSONObject($assetDir.getName()).getObject("assetId", String.class);
            if (assetId == null)
                return false;
            for (String ext : extensions)
                if (!checksum.getObject(ext, String.class)
                        .equals(IOUtils.FileUtil.getMD5(new File(Objects.requireNonNull(getAssetFilePath($assetDir, $modelsDataset, ext)))))) {
                    Logger.warn("Verify", "The md5 of file " + getAssetFilePath($assetDir, $modelsDataset, ext) +
                            " is: " + IOUtils.FileUtil.getMD5(new File(Objects.requireNonNull(getAssetFilePath($assetDir, $modelsDataset, ext)))) +
                            " but in the dataset, it is recorded as: " + checksum.getObject(ext, String.class));
                    return false;
                }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Get a single asset controller instance using the given asset directory.
     * @param $assetDir File instance of the specified asset directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return The asset controller instance.
     */
    public static AssetCtrl getAssetCtrl(File $assetDir, JSONObject $modelsDataset) {
        if (isValidAsset($assetDir, $modelsDataset)) {
            AssetCtrl assetCtrl = $modelsDataset.getObject($assetDir.getName(), AssetCtrl.class);
            assetCtrl.assetDir = $assetDir;
            return assetCtrl;
        }
        return new AssetCtrl();
    }

    /** Get a list of asset controller instances using the given root directory.
     * @param $rootDir File instance of the specified root directory.
     * @param $modelsDataset JSONObject of the models' dataset.
     * @return The array containing asset controller instances.
     */
    public static AssetCtrl[] getAssetList(File $rootDir, JSONObject $modelsDataset) {
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
                return list.toArray(new AssetCtrl[0]);
            for (File subDir : subDirs) {
                if ($modelsDataset.containsKey(subDir.getName())) {
                    if (!isIntegralAsset(subDir, $modelsDataset)) {
                        //System.out.println("Not Integral: " + subDir.getName());
                        continue;
                    }
                    list.add(getAssetCtrl(subDir, $modelsDataset));
                }
            }
        }
        return list.toArray(new AssetCtrl[0]);
    }

    /** Sort the asset controller list.
     * @param $assetList The specified asset controller list.
     * @return The sorted list.
     */
    public static AssetCtrl[] sortAssetList(AssetCtrl[] $assetList) {
        ArrayList<AssetCtrl> list = new ArrayList<>(List.of($assetList));
        ArrayList<AssetCtrl> newList = new ArrayList<>();
        for (AssetCtrl i : list) {
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
        return newList.toArray(new AssetCtrl[0]);
    }

    /** Get a list of asset id in the given asset controller list.
     * @param $assetList The specified asset controller list.
     * @return The array containing asset ids.
     */
    public static String[] getAssetIdList(AssetCtrl[] $assetList) {
        ArrayList<String> result = new ArrayList<>();
        for (AssetCtrl asset : $assetList) {
            if (asset.assetId != null)
                result.add(asset.assetId);
        }
        return result.toArray(new String[0]);
    }

    /** Search assets by keywords in the given asset controller list.
     * @param $keywords The keywords.
     * @param $assetList The specified asset controller list.
     * @return The array containing asset controller instances that matches the keywords.
     */
    public static AssetCtrl[] searchByKeyWords(String $keywords, AssetCtrl[] $assetList) {
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
        return result.toArray(new AssetCtrl[0]);
    }

    /** Search index by relative asset path in the given asset controller list.
     * @param $assetRelPath The relative asset path (without ext), like {@code "models\xxx_xxx"}.
     * @param $assetList The specified asset controller list.
     * @return The index of the 1st matched asset, otherwise {@code 0} will be return by default.
     */
    public static int searchByAssetRelPath(String $assetRelPath, AssetCtrl[] $assetList) {
        if ($assetRelPath.isEmpty())
            return 0;
        String assetId = new File($assetRelPath).getName();
        for (int i = 0; i < $assetList.length; i++) {
            if ($assetList[i].assetId != null &&
                    $assetList[i].assetId.equalsIgnoreCase(assetId)) {
                return i;
            }
        }
        return 0;
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
}
