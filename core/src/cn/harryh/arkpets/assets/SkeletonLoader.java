/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.assets;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.harryh.arkpets.Const.PathConfig.tempDirPath;


public class SkeletonLoader {
    protected final FileHandle file;
    protected final boolean isJson;
    protected final long headerSize;

    public final String hash;
    public final String version;
    public final float x, y, width, height;
    public final boolean nonEssential;
    public final float fps;
    public final String images_path, audio_path;
    public final List<String> strings;

    protected static final int MAX_SKELETON_FILE_SIZE = 64 << 20;

    /** Initializes a Spine skeleton loader from a file handle.
     * @param file The file handle of a skeleton file which can be either JSON format or binary format.
     * @throws IOException If I/O error occurs.
     */
    public SkeletonLoader(FileHandle file) throws IOException {
        this.file = file;
        if (file.length() > MAX_SKELETON_FILE_SIZE) {
            throw new IOException("Skeleton file is to large");
        }
        isJson = isJson(file);

        try (InputStream is = file.read()) {
            if (isJson) {
                // As JSON:
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonData = Objects.requireNonNull(JSON.parseObject(content),
                        "Cannot load skel json");
                JSONObject skelData = Objects.requireNonNull(jsonData.getJSONObject("skeleton"),
                        "Cannot find skeleton data in skel json");
                hash = skelData.getString("hash");
                version = skelData.getString("spine");
                x = skelData.getFloatValue("x");
                y = skelData.getFloatValue("y");
                width = skelData.getFloatValue("width");
                height = skelData.getFloatValue("height");
                nonEssential = false;
                fps = skelData.containsKey("fps") ? skelData.getFloatValue("fps") : 30.0f;
                images_path = skelData.getString("images");
                audio_path = skelData.getString("audio");
                strings = Collections.emptyList();
                headerSize = 0;
            } else {
                // As binary:
                long[] bytesRead = {0};
                hash = readString(is, bytesRead);
                version = readString(is, bytesRead);
                x = readFloat(is, bytesRead);
                y = readFloat(is, bytesRead);
                width = readFloat(is, bytesRead);
                height = readFloat(is, bytesRead);
                nonEssential = readBoolean(is, bytesRead);
                float fps;
                String imagesPath, audioPath;
                if (nonEssential) {
                    fps = readFloat(is, bytesRead);
                    imagesPath = readString(is, bytesRead);
                    if (imagesPath.isEmpty())
                        imagesPath = null;
                    audioPath = readString(is, bytesRead);
                    if (audioPath.isEmpty())
                        audioPath = null;
                } else {
                    fps = 30.0f;
                    imagesPath = null;
                    audioPath = null;
                }
                int stringCount = readVarInt(is, bytesRead);
                ArrayList<String> strings = new ArrayList<>(stringCount);
                for (int i = 0; i < stringCount; i++) {
                    strings.add(readString(is, bytesRead));
                }
                this.strings = Collections.unmodifiableList(strings);
                this.fps = fps;
                this.images_path = imagesPath;
                this.audio_path = audioPath;
                headerSize = bytesRead[0];
            }
        }
    }

    /** Gets whether the skeleton file is in JSON format.
     * @return True if the skeleton file is in JSON format; false if in binary format.
     */
    public boolean isJson() {
        return isJson;
    }

    /** Checks if the skeleton file needs to be fixed.
     * @return True if it needs to be fixed; false otherwise.
     * @see <a href="https://github.com/isHarryh/Ark-Pets/issues/150">ArkPets Issue #150</a>
     */
    public boolean needFix() {
        if (isJson) {
            return false;
        }
        for (String s : strings) {
            if (s.matches(".*\\s")) {
                return true;
            }
        }
        return false;
    }

    /** Returns a skeleton loader instance with fixed skeleton.
     * @return A new skeleton loader instance with fixed skeleton, or this instance if no fix is needed.
     * @throws IOException If I/O error occurs.
     * @see #needFix()
     */
    public SkeletonLoader fixed() throws IOException {
        if (!needFix()) {
            return this;
        }
        File tempFile = File.createTempFile("fixed_", "_" + file.name(), new File(tempDirPath));
        tempFile.deleteOnExit();
        FileHandle tempHandle = new FileHandle(tempFile);
        try (OutputStream os = tempHandle.write(false)) {
            // Write header
            writeString(os, ""); // Empty hash
            writeString(os, version);
            writeFloat(os, x);
            writeFloat(os, y);
            writeFloat(os, width);
            writeFloat(os, height);
            writeBoolean(os, nonEssential);
            if (nonEssential) {
                writeFloat(os, fps);
                writeString(os, images_path != null ? images_path : "");
                writeString(os, audio_path != null ? audio_path : "");
            }
            // Write strings pool
            List<String> fixedStrings = strings.stream()
                    .map(s -> s.replaceAll("\\s+$", ""))
                    .toList();
            writeVarInt(os, fixedStrings.size());
            for (String s : fixedStrings) {
                writeString(os, s);
            }
            // Write the remaining data
            try (InputStream is = file.read()) {
                long toSkip = headerSize;
                while (toSkip > 0) {
                    long skipped = is.skip(toSkip);
                    if (skipped <= 0) {
                        throw new IOException("Failed to skip header");
                    }
                    toSkip -= skipped;
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }
        return new SkeletonLoader(tempHandle);
    }

    /** Loads the skeleton data with the given texture atlas.
     * @param atlas The texture atlas to be attached to the skeleton data.
     * @param scale The scale factor to be applied to the skeleton data.
     * @return A skeleton data instance.
     */
    public SkeletonData loadSkeletonDataWith(TextureAtlas atlas, float scale) {
        if (isJson) {
            SkeletonJson json = new SkeletonJson(atlas);
            json.setScale(scale);
            return json.readSkeletonData(file);
        } else {
            SkeletonBinary binary = new SkeletonBinary(atlas);
            binary.setScale(scale);
            return binary.readSkeletonData(file);
        }
    }

    /** Loads the skeleton data with the texture atlas from the given atlas file.
     * @param atlasFile The file handle of the texture atlas file.
     * @param scale The scale factor to be applied to the skeleton data.
     * @param forceMipmap Whether to force enable mipmapping for the atlas textures.
     * @return A skeleton data instance.
     */
    public SkeletonData loadSkeletonDataWith(FileHandle atlasFile, float scale, boolean forceMipmap) {
        TextureAtlasData atlasData = new TextureAtlasData(atlasFile, atlasFile.parent(), false);
        if (forceMipmap) {
            for (TextureAtlasData.Page page : atlasData.getPages()) {
                page.minFilter = TextureFilter.MipMapLinearLinear;
                page.useMipMaps = true;
            }
        }
        return loadSkeletonDataWith(new TextureAtlas(atlasData), scale);
    }

    protected static boolean isJson(FileHandle file) throws IOException {
        try (InputStream is = file.read()) {
            byte[] buffer = new byte[1024];
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                throw new IOException("Empty skeleton file");
            }
            for (int i = 0; i < bytesRead; i++) {
                char c = (char) buffer[i];
                if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                    return c == '{';
                }
            }
            throw new IOException("Cannot determine skeleton file format");
        }
    }

    protected static boolean readBoolean(InputStream is, long[] bytesRead) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new EOFException("Unexpected end of stream");
        }
        bytesRead[0] += 1;
        return b != 0;
    }

    protected static float readFloat(InputStream is, long[] bytesRead) throws IOException {
        byte[] bytes = new byte[4];
        int read = is.read(bytes);
        if (read != 4) {
            throw new EOFException("Unexpected end of stream");
        }
        bytesRead[0] += 4;
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    protected static int readVarInt(InputStream is, long[] bytesRead) throws IOException {
        int result = 0;
        for (int i = 0; i < 5; i++) {
            int b = is.read();
            if (b == -1) {
                throw new EOFException("Unexpected end of stream");
            }
            bytesRead[0] += 1;
            result |= (b & 0x7F) << (i * 7);
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return result;
    }

    protected static String readString(InputStream is, long[] bytesRead) throws IOException {
        int length = readVarInt(is, bytesRead);
        if (length <= 1) {
            return "";
        }
        byte[] b = new byte[length - 1];
        int read = is.read(b);
        if (read != length - 1) {
            throw new EOFException("Unexpected end of stream");
        }
        bytesRead[0] += length - 1;
        return new String(b, StandardCharsets.UTF_8);
    }

    protected static void writeBoolean(OutputStream os, boolean value) throws IOException {
        os.write(value ? 1 : 0);
    }

    protected static void writeFloat(OutputStream os, float value) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(value);
        os.write(bb.array());
    }

    protected static void writeVarInt(OutputStream os, int value) throws IOException {
        do {
            int b = value & 0x7F;
            value >>>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            os.write(b);
        } while (value != 0);
    }

    protected static void writeString(OutputStream os, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length + 1;
        writeVarInt(os, length);
        os.write(bytes);
    }
}
