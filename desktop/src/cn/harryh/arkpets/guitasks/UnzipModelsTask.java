/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import javafx.scene.layout.StackPane;

import static cn.harryh.arkpets.Const.PathConfig;


public class UnzipModelsTask extends UnzipTask {
    public UnzipModelsTask(StackPane root, GuiTaskStyle style, String zipPath) {
        super(root, style, zipPath, PathConfig.tempModelsUnzipDirPath);
    }

    @Override
    protected String getHeader() {
        return "正在解压模型资源文件...";
    }

    @Override
    protected String getInitialContent() {
        return "这可能需要十几秒";
    }
}
