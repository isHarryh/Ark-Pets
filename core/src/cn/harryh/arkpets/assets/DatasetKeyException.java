package cn.harryh.arkpets.assets;

public class DatasetKeyException extends IllegalArgumentException {
    public DatasetKeyException(String keyName) {
        super("The key \"" + keyName + "\" not found or invalid.");
    }
}
