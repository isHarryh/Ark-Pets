/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.utils.StringUtils;
import com.alibaba.fastjson2.JSONObject;
import javafx.scene.layout.StackPane;

import java.net.URL;

import static cn.harryh.arkpets.Const.PathConfig.urlMirrorChyan;
import static cn.harryh.arkpets.Const.mirrorChyanAID;
import static cn.harryh.arkpets.Const.mirrorChyanRID;


public class McCheckAppUpdateTask extends FetchAsDataTask {
    private final String cdk;
    private final String os;

    public McCheckAppUpdateTask(StackPane parent, GuiTaskStyle style, String cdk) {
        super(parent, style, new int[]{400, 403});
        this.cdk = cdk;
        this.os = "win"; // Should be replaced with actual OS detection logic in higher version of ArkPets
    }

    @Override
    protected String getHeader() {
        return "正在与 Mirror 酱建立联系";
    }

    @Override
    protected URL getTargetURL() {
        return new StringUtils.URLStringBuilder(urlMirrorChyan)
                .addPath("api")
                .addPath("resources")
                .addPath(mirrorChyanRID)
                .addPath("latest")
                .addQuery("cdk", cdk)
                .addQuery("user_agent", mirrorChyanAID)
                .addQuery("os", os)
                .toURL();
    }

    @Override
    protected void onReceivedData(JSONObject json) {
    }
}
