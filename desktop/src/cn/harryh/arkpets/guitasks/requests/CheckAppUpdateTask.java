/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks.requests;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.network.SourceStrategy;
import cn.harryh.arkpets.network.api.AppQueryVersion;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson2.JSONObject;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Objects;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.Const.appVersion;


public class CheckAppUpdateTask extends FetchAsDataTask {
    private final String sourceStr;

    public CheckAppUpdateTask(StackPane parent, GuiTaskStyle style, String sourceStr) {
        super(parent, style);
        this.sourceStr = sourceStr;
    }

    protected void onHasNewStableVersion(Version stableVersion) {
    }

    protected void onUpToDated(Version stableVersion) {
    }

    protected void onAPIFailed() {
    }

    @Override
    protected String getHeader() {
        return "正在下载软件版本信息...";
    }

    @Override
    protected URL getTargetURL() {
        return new StringUtils.URLStringBuilder(PathConfig.urlApi)
                .addQuery("type", "queryVersion")
                .addQuery("cliVer", appVersion.toString())
                .addQuery("source", sourceStr)
                .toURL();
    }

    @Override
    protected void onReceivedData(JSONObject json) {
        try {
            AppQueryVersion value = json.toJavaObject(AppQueryVersion.class);
            if (value.code == 0) {
                // On API succeeded:
                Version stableVersion = Objects.requireNonNull(value.getStableVersion());
                Logger.info("Checker", "Application version check finished, newest: " + stableVersion);
                setAppBackupSource(stableVersion);

                if (appVersion.lessThan(stableVersion)) {
                    Const.isUpdateAvailable = true;
                    onHasNewStableVersion(stableVersion);
                } else {
                    Const.isUpdateAvailable = false;
                    onUpToDated(stableVersion);
                }
            } else {
                // On API failed:
                Logger.warn("Checker", "Application version check failed (api failed)");
                onAPIFailed();
            }
        } catch (Exception e) {
            // On parsing failed:
            Logger.error("Checker", "Application version check failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();
        }
    }

    protected static void setAppBackupSource(Version version) {
        // Set update source according to the given version
        // Should be replaced with actual OS detection logic in higher version of ArkPets
        // Windows only
        SourceStrategy.getStrategy("AppDownload")
                .clearBackupSource()
                .addBackupSource("GitHub", "https://github.com/isHarryh/Ark-Pets/releases/download/v%s/ArkPets-v%s-Setup.exe".formatted(version, version))
                .addBackupSource("GHProxy", "https://ghproxy.harryh.cn/https://github.com/isHarryh/Ark-Pets/releases/download/v%s/ArkPets-v%s-Setup.exe".formatted(version, version));
    }
}
