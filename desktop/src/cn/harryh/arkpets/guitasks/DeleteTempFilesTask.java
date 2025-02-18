/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const.PathConfig;
import cn.harryh.arkpets.utils.IOUtils.FileUtil;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;
import java.util.regex.Pattern;


public abstract class DeleteTempFilesTask extends GuiTask {
    protected final Pattern regex;
    protected final long timestamp;

    public DeleteTempFilesTask(StackPane parent, GuiTaskStyle style, String matchRegex, long beforeMillis) {
        super(parent, style);
        this.regex = Pattern.compile(matchRegex);
        this.timestamp = System.currentTimeMillis() - beforeMillis;
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                File temp = new File(PathConfig.tempDirPath);
                if (!temp.isDirectory()) {
                    return true;
                }

                List<Path> filesToDelete;
                try (Stream<Path> pathStream = Files.walk(temp.toPath())) {
                    filesToDelete = pathStream
                            .filter(path -> {
                                File file = path.toFile();
                                return file.isFile()
                                        && regex.matcher(file.getName()).matches()
                                        && file.lastModified() < timestamp;
                            })
                            .toList();
                }

                int total = filesToDelete.size();
                if (total == 0) {
                    return true;
                }

                this.updateProgress(0, total);
                for (int i = 0; i < filesToDelete.size(); i++) {
                    if (isCancelled()) {
                        break;
                    }
                    FileUtil.delete(filesToDelete.get(i), true);
                    updateProgress(i + 1, total);
                }

                return !isCancelled();
            }
        };
    }

    @Override
    protected String getHeader() {
        return "正在清理临时文件...";
    }
}
