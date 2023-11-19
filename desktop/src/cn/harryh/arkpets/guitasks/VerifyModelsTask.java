/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.AssetCtrl;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.PopupUtils;
import com.alibaba.fastjson.JSONObject;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.ArrayList;

import static cn.harryh.arkpets.utils.PopupUtils.COLOR_SUCCESS;
import static cn.harryh.arkpets.utils.PopupUtils.COLOR_WARNING;


public class VerifyModelsTask extends GuiTask {
    protected final JSONObject modelsDatasetFull;
    protected final Iterable<AssetCtrl> foundModelAssets;
    private final Node[] dialogGraphic = new Node[1];
    private final String[] dialogHeader = new String[1];
    private final String[] dialogContent = new String[1];

    public VerifyModelsTask(StackPane root, GuiTaskStyle style, JSONObject modelsDatasetFull, Iterable<AssetCtrl> foundModelAssets) {
        super(root, style);
        this.modelsDatasetFull = modelsDatasetFull;
        this.foundModelAssets = foundModelAssets;
    }

    @Override
    protected String getHeader() {
        return "正在验证模型资源完整性...";
    }

    @Override
    protected String getInitialContent() {
        return "这可能需要数秒钟";
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                ArrayList<File> pendingDirs = new ArrayList<>();
                JSONObject modelsDatasetData = modelsDatasetFull.getJSONObject("data");
                for (AssetCtrl assetCtrl : foundModelAssets)
                    pendingDirs.add(new File(assetCtrl.getLocation()));

                Thread.sleep(100);
                boolean flag = false;
                AssetCtrl.AssetVerifier assetVerifier = new AssetCtrl.AssetVerifier(modelsDatasetData);
                for (int i = 0; i < pendingDirs.size(); i++) {
                    this.updateProgress(i, pendingDirs.size());
                    File file = pendingDirs.get(i);
                    AssetCtrl.AssetStatus result = assetVerifier.verify(file);
                    if (result == AssetCtrl.AssetStatus.VALID) {
                        Logger.info("Checker", "Model repo check finished (not integral)");
                        dialogGraphic[0] = PopupUtils.IconUtil.getIcon(PopupUtils.IconUtil.ICON_WARNING_ALT, COLOR_WARNING);
                        dialogHeader[0] = "已发现问题，模型资源可能不完整";
                        dialogContent[0] = "资源 " + file.getPath() + " 可能不存在，重新下载模型文件可能解决此问题。";
                        flag = true;
                        break;
                    } else if (result == AssetCtrl.AssetStatus.EXISTED) {
                        Logger.info("Checker", "Model repo check finished (checksum mismatch)");
                        dialogGraphic[0] = PopupUtils.IconUtil.getIcon(PopupUtils.IconUtil.ICON_WARNING_ALT, COLOR_WARNING);
                        dialogHeader[0] = "已发现问题，模型资源可能不完整";
                        dialogContent[0] = "资源 " + file.getPath() + " 可能缺少部分文件，重新下载模型文件可能解决此问题。";
                        flag = true;
                        break;
                    } else if (this.isCancelled()) {
                        Logger.info("Checker", "Model repo check was cancelled in verification stage.");
                        return false;
                    }
                }

                if (!flag) {
                    Logger.info("Checker", "Model repo check finished (okay)");
                    dialogGraphic[0] = PopupUtils.IconUtil.getIcon(PopupUtils.IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS);
                    dialogHeader[0] = "模型资源是完整的。";
                    dialogContent[0] = "这只能说明本地的模型资源是完整的，但不一定是最新的。";
                }
                return true;
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            PopupUtils.DialogUtil.createErrorDialog(root, e).show();
    }

    @Override
    protected void onSucceeded(boolean result) {
        if (style != GuiTaskStyle.HIDDEN)
            PopupUtils.DialogUtil.createCommonDialog(root,
                    dialogGraphic[0],
                    "验证资源完整性",
                    dialogHeader[0],
                    dialogContent[0],
                    null).show();
    }
}
