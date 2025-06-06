/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.network.api.AppQueryAnnouncement;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONObject;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

import java.util.Objects;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.network.api.AppQueryAnnouncement.AnnounceItem;


public class FetchAnnounceTask extends FetchAsDataTask {
    private final ObservableList<AnnounceItem> acceptor;

    public FetchAnnounceTask(StackPane parent, GuiTaskStyle style, ObservableList<AnnounceItem> acceptor) {
        super(parent, style, 16 << 20); // 16 MB
        this.acceptor = acceptor;
    }

    @Override
    protected String getHeader() {
        return "正在获取公告数据...";
    }

    @Override
    protected String getRemotePath() {
        return PathConfig.urlApi + "?type=queryAnnouncement";
    }

    @Override
    protected void onReceivedData(JSONObject json) {
        // When finished downloading the latest app ver-info:
        try {
            AppQueryAnnouncement value = json.toJavaObject(AppQueryAnnouncement.class);
            if (value.code == 0) {
                acceptor.setAll(Objects.requireNonNull(value.data.contents));
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
