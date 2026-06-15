/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.utils.StringUtils;
import com.alibaba.fastjson2.JSONObject;
import javafx.scene.layout.StackPane;

import java.net.URL;

import static cn.harryh.arkpets.Const.appName;


public class McCheckModelsUpdateTask extends FetchAsDataTask {
    private final String cdk;

    public McCheckModelsUpdateTask(StackPane parent, GuiTaskStyle style, String cdk) {
        super(parent, style, new int[]{400, 403});
        this.cdk = cdk;
    }

    @Override
    protected String getHeader() {
        return "正在与 Mirror 酱建立联系";
    }

    @Override
    protected URL getTargetURL() {
        return new StringUtils.URLStringBuilder("https://mirrorchyan.com/api/resources/ArkModelsRepo/latest")
                .addQuery("cdk", cdk)
                .addQuery("user_agent", appName + "Gui")
                .toURL();
    }

    @Override
    protected void onReceivedData(JSONObject json) {
    }
}
