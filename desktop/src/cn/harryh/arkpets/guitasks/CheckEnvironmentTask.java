package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.envchecker.EnvCheckTask;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

import java.util.List;


public class CheckEnvironmentTask extends GuiTask {
    private String failureReason;
    private String failureContent;
    private CheckStatus status;
    private final List<EnvCheckTask> list;
    private final StackPane parent;
    private Runnable success;

    private enum CheckStatus {
        UNKNOWN,
        SUCCESS,
        FAILED,
        NEEDCONFIRM,
        RUNNING
    }

    public CheckEnvironmentTask(StackPane parent, List<EnvCheckTask> list) {
        super(parent, GuiTaskStyle.STRICT);
        this.parent = parent;
        this.list = list;
    }

    public CheckEnvironmentTask(StackPane parent, List<EnvCheckTask> list, Runnable success) {
        this(parent, list);
        this.success = success;
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() {
                failureContent = null;
                failureReason = null;
                status = CheckStatus.RUNNING;
                while (!list.isEmpty()) {
                    EnvCheckTask task = list.get(0);
                    Logger.info("EnvCheck", "Running Check " + task);
                    boolean result = task.run();
                    if (!result) {
                        if (task.canFix()) {
                            if (task.needConfirmFix()) {
                                failureReason = task.getFixReason();
                                failureContent = task.getFixDetail();
                                status = CheckStatus.NEEDCONFIRM;
                                return true;
                            }
                            Logger.info("EnvCheck", "Running Fix " + task);
                            boolean fixResult = task.tryFix();
                            if (!fixResult) {
                                failureReason = task.getFailureReason();
                                failureContent = task.getFailureDetail();
                                status = CheckStatus.FAILED;
                                return true;
                            }
                        } else {
                            failureReason = task.getFailureReason();
                            failureContent = task.getFailureDetail();
                            status = CheckStatus.FAILED;
                            return true;
                        }
                    }
                    list.remove(0);
                }
                status = CheckStatus.SUCCESS;
                return true;
            }
        };
    }

    @Override
    protected String getHeader() {
        return "正在检查运行环境";
    }

    @Override
    protected String getInitialContent() {
        return "这可能需要数秒钟";
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.Dialogs.createErrorDialog(parent, e).show();

    }

    @Override
    protected void onSucceeded(boolean result) {
        if (style != GuiTaskStyle.HIDDEN) {
            if (status == CheckStatus.FAILED) {
                GuiPrefabs.Dialogs.createCommonDialog(parent,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "环境检查警告",
                        failureReason,
                        failureContent,
                        null).show();
            } else if (status == CheckStatus.NEEDCONFIRM) {
                GuiPrefabs.Dialogs.createConfirmDialog(parent,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_HELP_ALT, GuiPrefabs.COLOR_INFO),
                        "环境检查提示",
                        failureReason,
                        failureContent,
                        this::fixAndContinue).show();
            } else if (status == CheckStatus.SUCCESS && success != null) {
                success.run();
            }
        }
    }

    private void fixAndContinue() {
        EnvCheckTask fixtask = list.get(0);
        Logger.info("EnvCheck", "Running Fix " + fixtask);
        boolean fixResult = fixtask.tryFix();
        if (!fixResult) {
            GuiPrefabs.Dialogs.createCommonDialog(parent,
                    GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                    "环境检查警告",
                    fixtask.getFailureReason(),
                    fixtask.getFailureDetail(),
                    null).show();
            return;
        }
        list.remove(0);
        new CheckEnvironmentTask(parent, list).start();
    }
}
