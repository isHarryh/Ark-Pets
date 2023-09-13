/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;


public class PopupUtils {
    public static final String COLOR_INFO       = "#37B";
    public static final String COLOR_SUCCESS    = "#5B5";
    public static final String COLOR_WARNING    = "#E93";
    public static final String COLOR_DANGER     = "#F54";
    public static final String COLOR_WHITE      = "#FFF";
    public static final String COLOR_BLACK      = "#000";
    public static final String COLOR_DARK_GRAY  = "#222";
    public static final String COLOR_GRAY       = "#444";
    public static final String COLOR_LIGHT_GRAY = "#666";

    public static class IconUtil {
        public static final String ICON_INFO        = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z";
        public static final String ICON_INFO_ALT    = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z";
        public static final String ICON_HELP        = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm1.25 17c0 .69-.559 1.25-1.25 1.25-.689 0-1.25-.56-1.25-1.25s.561-1.25 1.25-1.25c.691 0 1.25.56 1.25 1.25zm1.393-9.998c-.608-.616-1.515-.955-2.551-.955-2.18 0-3.59 1.55-3.59 3.95h2.011c0-1.486.829-2.013 1.538-2.013.634 0 1.307.421 1.364 1.226.062.847-.39 1.277-.962 1.821-1.412 1.343-1.438 1.993-1.432 3.468h2.005c-.013-.664.03-1.203.935-2.178.677-.73 1.519-1.638 1.536-3.022.011-.924-.284-1.719-.854-2.297z";
        public static final String ICON_HELP_ALT    = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm0 18.25c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25c.691 0 1.25.56 1.25 1.25s-.559 1.25-1.25 1.25zm1.961-5.928c-.904.975-.947 1.514-.935 2.178h-2.005c-.007-1.475.02-2.125 1.431-3.468.573-.544 1.025-.975.962-1.821-.058-.805-.73-1.226-1.365-1.226-.709 0-1.538.527-1.538 2.013h-2.01c0-2.4 1.409-3.95 3.59-3.95 1.036 0 1.942.339 2.55.955.57.578.865 1.372.854 2.298-.016 1.383-.857 2.291-1.534 3.021z";
        public static final String ICON_SUCCESS     = "m12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm4.393 7.5l-5.643 5.784-2.644-2.506-1.856 1.858 4.5 4.364 7.5-7.643-1.857-1.857z";
        public static final String ICON_SUCCESS_ALT = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-1.25 17.292l-4.5-4.364 1.857-1.858 2.643 2.506 5.643-5.784 1.857 1.857-7.5 7.643z";
        public static final String ICON_WARNING     = "m12 5.177l8.631 15.823h-17.262l8.631-15.823zm0-4.177l-12 22h24l-12-22zm-1 9h2v6h-2v-6zm1 9.75c-.689 0-1.25-.56-1.25-1.25s.561-1.25 1.25-1.25 1.25.56 1.25 1.25-.561 1.25-1.25 1.25z";
        public static final String ICON_WARNING_ALT = "m12 1l-12 22h24l-12-22zm-1 8h2v7h-2v-7zm1 11.25c-.69 0-1.25-.56-1.25-1.25s.56-1.25 1.25-1.25 1.25.56 1.25 1.25-.56 1.25-1.25 1.25z";
        public static final String ICON_DANGER      = "m16.142 2l5.858 5.858v8.284l-5.858 5.858h-8.284l-5.858-5.858v-8.284l5.858-5.858h8.284zm.829-2h-9.942l-7.029 7.029v9.941l7.029 7.03h9.941l7.03-7.029v-9.942l-7.029-7.029zm-8.482 16.992l3.518-3.568 3.554 3.521 1.431-1.43-3.566-3.523 3.535-3.568-1.431-1.432-3.539 3.583-3.581-3.457-1.418 1.418 3.585 3.473-3.507 3.566 1.419 1.417z";
        public static final String ICON_DANGER_ALT  = "m16.971 0h-9.942l-7.029 7.029v9.941l7.029 7.03h9.941l7.03-7.029v-9.942l-7.029-7.029zm-1.402 16.945l-3.554-3.521-3.518 3.568-1.418-1.418 3.507-3.566-3.586-3.472 1.418-1.417 3.581 3.458 3.539-3.583 1.431 1.431-3.535 3.568 3.566 3.522-1.431 1.43z";
        public static final String ICON_UPDATE      = "m12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm1 15.889v-2.223s-3.78-.114-7 3.333c1.513-6.587 7-7.778 7-7.778v-2.221l5 4.425-5 4.464z";

        /** Gets an SVGPath Node using the given path string and color.
         * @param svg The SVG path string.
         * @param color The specified color string, e.g.#FFFFFF.
         * @return JavaFX SVGPath Node.
         */
        public static SVGPath getIcon(String svg, String color) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(svg);
            svgPath.setFill(Paint.valueOf(color));
            return svgPath;
        }
    }


    public static class DialogUtil {
        public static void disposeDialog(JFXDialog dialog, Pane root) {
            dialog.close();
            dialog.getDialogContainer().getChildren().remove(dialog);
            root.getChildren().remove(dialog.getDialogContainer());
            root.requestFocus();
        }

        public static JFXDialog createCenteredDialog(Pane root, boolean overlayClose) {
            StackPane container = new StackPane();
            container.setPrefSize(root.getWidth(), root.getHeight());
            root.getChildren().add(container);
            JFXDialog dialog = new JFXDialog();
            dialog.setDialogContainer(container);
            dialog.setOverlayClose(overlayClose);
            dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            return dialog;
        }

        public static Node getHeading(Node graphic, String text, String color) {
            Label label = new Label(text);
            label.setGraphic(graphic);
            label.setGraphicTextGap(5);
            label.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + color);
            return label;
        }

        public static Node getPrefabsH2(String text) {
            Label h2 = new Label(text);
            h2.setStyle("-fx-font-size:16px;-fx-text-fill:" + COLOR_DARK_GRAY);
            h2.setWrapText(true);
            return h2;
        }

        public static Node getPrefabsH3(String text) {
            Label h3 = new Label(text);
            h3.setStyle("-fx-font-size:12px;-fx-min-height:38px;-fx-wrap-text:true;-fx-text-fill:" + COLOR_LIGHT_GRAY);
            h3.setWrapText(true);
            return h3;
        }

        public static JFXButton getCancelButton(JFXDialog dialog, Pane root) {
            JFXButton button = new JFXButton();
            button.setText("取 消");
            button.setTextFill(Paint.valueOf(COLOR_WHITE));
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + COLOR_WHITE + ";-fx-background-color:" + COLOR_INFO);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getOkayButton(JFXDialog dialog, Pane root) {
            JFXButton button = new JFXButton();
            button.setText("确 认");
            button.setTextFill(Paint.valueOf(COLOR_WHITE));
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + COLOR_WHITE + ";-fx-background-color:" + COLOR_INFO);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }

        public static JFXButton getTrustButton(JFXDialog dialog, Pane root) {
            JFXButton button = new JFXButton();
            button.setText("信 任");
            button.setStyle("-fx-font-size:13px;-fx-text-fill:" + COLOR_WHITE + ";-fx-background-color:" + COLOR_WARNING);
            button.setOnAction(e -> disposeDialog(dialog, root));
            return button;
        }
    }


    abstract public static class Handbook {
        public boolean hasShown = false;

        public Handbook() {
        }

        abstract public String getTitle();

        abstract public String getHeader();

        abstract public String getContent();

        public SVGPath getIcon() {
            return IconUtil.getIcon(IconUtil.ICON_HELP_ALT, COLOR_INFO);
        }

        public boolean hasShown() {
            return hasShown;
        }

        public void setShown() {
            hasShown = true;
        }
    }
}
