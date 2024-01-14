/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const.PathConfig;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class PostUnzipModelTask extends GuiTask {
    private final String rootPath;
    private final String modelsDataPath;

    public PostUnzipModelTask(StackPane root, GuiTaskStyle style) {
        super(root, style);
        this.rootPath = PathConfig.tempModelsUnzipDirPath;
        this.modelsDataPath = PathConfig.fileModelsDataPath;
    }

    @Override
    protected String getHeader() {
        return "正在应用模型更新...";
    }

    @Override
    protected String getInitialContent() {
        return "即将完成";
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (!new File(rootPath).isDirectory())
                    throw new FileNotFoundException("The directory " + rootPath + " not found.");
                Path root = new File(rootPath).toPath();
                int rootPathCount = root.getNameCount();
                final boolean[] hasDataset = {false};

                Logger.info("Task", "Moving required files from unzipped files");
                Files.walkFileTree(root, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getNameCount() == (rootPathCount + 2) && file.getName(rootPathCount + 1).toString().equals(modelsDataPath)) {
                            Files.move(file, file.getFileName(), StandardCopyOption.REPLACE_EXISTING);
                            hasDataset[0] = true;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (dir.getNameCount() == (rootPathCount + 2)) {
                            if (Files.exists(dir.getFileName()))
                                IOUtils.FileUtil.delete(dir.getFileName(), false);
                            Files.move(dir, dir.getFileName(), StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (!hasDataset[0])
                    throw new FileNotFoundException("The file " + modelsDataPath + " not found.");
                try {
                    IOUtils.FileUtil.delete(new File(PathConfig.tempModelsUnzipDirPath).toPath(), false);
                } catch (IOException e) {
                    Logger.warn("Task", "The unzipped cache cannot be deleted, because " + e.getMessage());
                }
                Logger.info("Task", "Moved required files from unzipped files, finished");
                return true;
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
    }
}
