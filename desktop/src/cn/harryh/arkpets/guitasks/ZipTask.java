/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.Map;


public class ZipTask extends GuiTask {
    protected final String zipPath;
    protected final Map<String, String> contents;

    public ZipTask(StackPane root, GuiTaskStyle style, String zipPath, Map<String, String> contents) {
        super(root, style);
        this.zipPath = zipPath;
        this.contents = contents;
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
            GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
    }
}
