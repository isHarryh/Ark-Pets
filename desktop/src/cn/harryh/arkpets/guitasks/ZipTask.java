/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ZipTask extends GuiTask {
    protected final String zipPath;
    protected final Map<String, String> contents;

    public ZipTask(StackPane parent, GuiTaskStyle style, String zipPath, Map<String, String> contents) {
        super(parent, style);
        this.zipPath = zipPath;
        this.contents = contents;
    }

    public ZipTask(StackPane parent, GuiTaskStyle style, String zipPath, List<String> contents) {
        super(parent, style);
        this.zipPath = zipPath;
        this.contents = contents.stream()
                .collect(Collectors.toMap(
                        path -> path,
                        path -> Paths.get(path).getFileName().toString()
                ));
    }

    @Override
    protected String getHeader() {
        return "正在创建压缩文件...";
    }

    @Override
    protected String getInitialContent() {
        return "这可能需要一些时间";
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Logger.info("Zip", "Zipping " + contents.size() + " entries into " + zipPath);
                IOUtils.FileUtil.delete(new File(zipPath), false);
                IOUtils.ZipUtil.zip(zipPath, contents, false);
                Logger.info("Zip", "Zipped into " + zipPath + " , finished");
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
    }

    @Override
    protected void onSucceeded(boolean result) {
        GuiPrefabs.Dialogs.createCommonDialog(parent,
                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                "导出",
                "已成功导出为压缩包",
                "压缩包文件已被保存到：\n" + new File(zipPath).getAbsolutePath(),
                null).show();
    }
}
