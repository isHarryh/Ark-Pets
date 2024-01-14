/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;


abstract public class GuiTask {
    public final StackPane root;
    public final GuiTaskStyle style;
    protected final Task<Boolean> task;
    protected final JFXDialog dialog;
    private static int threadNumber;

    public enum GuiTaskStyle {
        /** Indicates that this GuiTask is a cancellable dialog task */
        COMMON,
        /** Indicates that this GuiTask is a not-cancellable dialog task */
        STRICT,
        /** Indicates that this GuiTask is to be running in background */
        HIDDEN
    }

    /** Initializes a task wrapper that can execute a certain task in the JavaFX GUI.
     * @param root The root node of the UI.
     * @param style The {@link GuiTaskStyle} of this instance.
     * @since ArkPets 2.4
     */
    public GuiTask(StackPane root, GuiTaskStyle style) {
        this.root = root;
        this.style = style;

        // Generate related instances
        task = getTask();
        dialog = style != GuiTaskStyle.HIDDEN ? getDialog(root, task, style != GuiTaskStyle.STRICT) : null;

        // Handle all task events
        task.setOnCancelled(e -> {
            Logger.info("Task", this + " was cancelled.");
            this.onCancelled();
            GuiPrefabs.DialogUtil.disposeDialog(dialog, root);
        });
        task.setOnFailed(e -> {
            Logger.error("Task", this + " failed, details see below.", task.getException());
            this.onFailed(task.getException());
            GuiPrefabs.DialogUtil.disposeDialog(dialog, root);
        });
        task.setOnSucceeded(e -> {
            Logger.info("Task", this + " completed.");
            this.onSucceeded(task.getValue());
            GuiPrefabs.DialogUtil.disposeDialog(dialog, root);
        });
        task.setOnRunning(e -> Logger.debug("Task", this + " running."));
        task.setOnScheduled(e -> Logger.debug("Task", this + " scheduled."));
    }

    /** Starts the task defined in this wrapper.
     */
    public final void start() {
        if (task == null)
            throw new NullPointerException("The task to be started was null.");
        if (task.isDone())
            throw new IllegalStateException("The task was already done.");
        if (task.isRunning())
            throw new IllegalStateException("The task was already running.");
        if (dialog != null && style != GuiTaskStyle.HIDDEN)
            dialog.show();
        new Thread(task, "GuiTask-" + threadNumber++).start();
    }

    /** Generates a JavaFX {@code Task} instance which will be controlled by this {@code GuiTask} instance.
     * @return The JavaFX Task to be controlled.
     * @implNote The task should use a boolean as its result value.
     *           The task should not have any event handlers, instead,
     *           please override the "{@code onEvent}" methods in this {@code GuiTask} class.
     * @see javafx.concurrent.Task
     */
    abstract protected Task<Boolean> getTask();

    /** Generates the header of the dialog.
     * @return The header string.
     */
    abstract protected String getHeader();

    /** Generates the initial dialog content.
     * @return The content string.
     */
    protected String getInitialContent() {
        return "";
    }

    /** On task cancelled, this method would be invoked.
     */
    protected void onCancelled() {
    }

    /** On task failed, this method would be invoked.
     * @param e The exception that cause the failure.
     */
    protected void onFailed(Throwable e) {
    }

    /** On task succeeded, this method would be invoked.
     * @param result The result value of the task.
     */
    protected void onSucceeded(boolean result) {
    }

    private JFXDialog getDialog(StackPane root, Task<Boolean> boundTask, boolean cancelable) {
        // Initialize the dialog framework
        JFXDialog dialog = GuiPrefabs.DialogUtil.createCenteredDialog(root, false);
        ProgressBar bar = new ProgressBar(-1);
        bar.setPrefSize(root.getWidth() * 0.6, 10);

        // Add components to the dialog
        VBox content = new VBox();
        Label h2 = (Label) GuiPrefabs.DialogUtil.getPrefabsH2(getHeader());
        Label h3 = (Label) GuiPrefabs.DialogUtil.getPrefabsH3(getInitialContent());
        content.setSpacing(5);
        content.getChildren().add(h2);
        content.getChildren().add(new Separator());
        content.getChildren().add(h3);

        // Set the layout of the dialog
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(bar);
        layout.setBody(content);
        layout.setActions(GuiPrefabs.DialogUtil.getOkayButton(dialog, root));
        dialog.setContent(layout);
        if (cancelable) {
            JFXButton cancel = GuiPrefabs.DialogUtil.getCancelButton(dialog, root);
            cancel.setOnAction(e -> boundTask.cancel());
            layout.setActions(cancel);
        } else {
            layout.setActions(List.of());
        }

        // Handle changeable content
        final double[] cachedProgress = {-1};
        boundTask.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (Math.abs((double)newValue - cachedProgress[0]) >= 0.001) {
                cachedProgress[0] = (double)newValue;
                bar.setProgress((double)newValue);
            }
        });
        boundTask.messageProperty().addListener(((observable, oldValue, newValue) -> h3.setText(newValue)));

        return dialog;
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        return name.isEmpty() ? getClass().getSuperclass().getSimpleName() : name;
    }
}
