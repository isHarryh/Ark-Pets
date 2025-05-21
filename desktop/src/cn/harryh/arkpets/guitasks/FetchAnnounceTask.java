/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.controllers.AnnounceDialog;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONObject;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

import static cn.harryh.arkpets.Const.PathConfig;


public class FetchAnnounceTask extends FetchRemoteTask {
    protected ObservableList<AnnounceDialog.AnnounceItem> acceptor;

    public FetchAnnounceTask(StackPane parent, GuiTaskStyle style, ObservableList<AnnounceDialog.AnnounceItem> acceptor) {
        super(parent,
                style,
                PathConfig.urlApi + "?type=queryAnnouncement",
                PathConfig.tempQueryAnnounceCachePath,
                Const.isHttpsTrustAll);
        this.acceptor = acceptor;

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return "正在获取公告数据...";
    }

    @Override
    protected void onSucceeded(boolean result) {
        // When finished downloading the latest app ver-info:
        try {
            // Try to parse the latest app ver-info
            JSONObject queryAnnounceResult = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readByte(new File(PathConfig.tempQueryAnnounceCachePath))));
            if (queryAnnounceResult.getString("msg").equals("success")) {
                // If the response status is "success":
                ArrayList<AnnounceDialog.AnnounceItem> arrayList = new ArrayList<>();
                queryAnnounceResult.getJSONObject("data").getJSONArray("contents")
                        .forEach(o -> arrayList.add(((JSONObject) o).toJavaObject(AnnounceDialog.AnnounceItem.class)));
                acceptor.setAll(arrayList);
            } else {
                // On API failed:
                Logger.warn("Announce", "Announcement fetching failed (api failed)");
                if (style != GuiTaskStyle.HIDDEN)
                    GuiPrefabs.Dialogs.createCommonDialog(parent,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DANGER, GuiPrefabs.COLOR_DANGER),
                            "获取公告失败",
                            "服务器返回了无效的消息。",
                            "可能是兼容性问题或服务器不可用。",
                            null).show();
            }
        } catch (Exception e) {
            // On parsing failed:
            Logger.error("Announce", "Announcement fetching failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
        }
    }
}
