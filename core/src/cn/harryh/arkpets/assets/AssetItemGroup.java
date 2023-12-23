/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.assets;

import cn.harryh.arkpets.assets.AssetItem.PropertyExtractor;

import java.util.*;
import java.util.function.Predicate;


/** The class implements the Collection of {@link AssetItem}.
 * <hr>
 * The structure of the root directory may be like what is shown below.
 * Each {@code SubDir} represents an {@code AssetItem}.
 * <blockquote><pre>
 * +-RootDir
 * |-+-SubDir1 (whose name is the model name of xxx)
 * | |---xxx.atlas
 * | |---xxx.png
 * | |---xxx.skel
 * |---SubDir2
 * |---SubDir3
 * |---...</pre>
 * </blockquote>
 * @since ArkPets 2.4
 */
public class AssetItemGroup implements Collection<AssetItem> {
    protected final ArrayList<AssetItem> assetItemList;

    public AssetItemGroup(Collection<AssetItem> assetItemList) {
        this.assetItemList = new ArrayList<>(assetItemList);
    }

    public AssetItemGroup() {
        this(new ArrayList<>());
    }

    public AssetItemGroup searchByKeyWords(String keyWords) {
        if (keyWords == null || keyWords.isEmpty())
            return this;
        String[] wordList = keyWords.split(" ");
        AssetItemGroup result = new AssetItemGroup();
        for (AssetItem asset : this) {
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

    public AssetItem searchByRelPath(String relPath) {
        if (relPath == null || relPath.isEmpty())
            return null;
        for (AssetItem asset : this)
            if (asset.getLocation().equalsIgnoreCase(relPath))
                return asset;
        return null;
    }

    public <T> Set<T> extract(PropertyExtractor<T> property) {
        HashSet<T> result = new HashSet<>();
        for (AssetItem item : this)
            result.addAll(property.apply(item));
        return result;
    }

    public AssetItemGroup filter(Predicate<AssetItem> predicate) {
        return new AssetItemGroup(assetItemList.stream().filter(predicate).toList());
    }

    public <T> AssetItemGroup filter(PropertyExtractor<T> property, Set<T> filterValues, int mode) {
        final boolean TRUE = (mode & FilterMode.MATCH_REVERSE) == 0;
        return filter(assetItem -> {
            Set<T> itemValues = property.apply(assetItem);
            if ((mode & FilterMode.MATCH_ANY) != 0) {
                for (T value : itemValues)
                    if (filterValues.contains(value))
                        return TRUE;
            } else {
                if (itemValues.containsAll(filterValues))
                    return TRUE;
            }
            return !TRUE;
        });
    }

    public <T> AssetItemGroup filter(PropertyExtractor<T> property, Set<T> filterValues) {
        return filter(property, filterValues, 0);
    }

    public void sort() {
        assetItemList.sort(Comparator.comparing(asset -> asset.assetDir, Comparator.naturalOrder()));
    }

    @Deprecated
    public void removeDuplicated() {
        ArrayList<AssetItem> newList = new ArrayList<>();
        for (AssetItem i : assetItemList) {
            boolean flag = true;
            for (AssetItem j : newList) {
                if (j.equals(i)) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                newList.add(i);
        }
        assetItemList.clear();
        assetItemList.addAll(newList);
    }

    public static class FilterMode {
        public static final int MATCH_ANY           = 0b1;
        public static final int MATCH_REVERSE       = 0b10;
    }

    @Override
    public Iterator<AssetItem> iterator() {
        return assetItemList.iterator();
    }

    @Override
    public boolean add(AssetItem assetItem) {
        return assetItemList.add(assetItem);
    }

    @Override
    public boolean addAll(Collection<? extends AssetItem> c) {
        return assetItemList.addAll(c);
    }

    @Override
    public boolean contains(Object o) {
        return assetItemList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return assetItemList.containsAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return assetItemList.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return assetItemList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return assetItemList.retainAll(c);
    }

    @Override
    public void clear() {
        assetItemList.clear();
    }

    @Override
    public boolean isEmpty() {
        return assetItemList.isEmpty();
    }

    @Override
    public int size() {
        return assetItemList.size();
    }

    @Override
    public Object[] toArray() {
        return assetItemList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return assetItemList.toArray(a);
    }
}
