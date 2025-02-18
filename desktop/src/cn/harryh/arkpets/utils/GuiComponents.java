/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import com.jfoenix.controls.*;
import javafx.animation.ScaleTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;


public class GuiComponents {
    /** A useful tool to initialize a {@link Slider} control.
     * @param <N> The value type of the Slider.
     */
    @SuppressWarnings("UnusedReturnValue")
    abstract public static class SliderSetup<N extends Number> {
        protected final Slider slider;
        protected Labeled display;
        protected final DoubleProperty proxy;
        protected final ChangeListener<? super Number> listener;
        protected ChangeListener<? super Number> listenerForDisplay;
        protected ChangeListener<? super Number> listenerForExternal;
        protected static final double initialValue = Double.MIN_VALUE;

        public SliderSetup(Slider slider) {
            this.slider = slider;
            this.slider.setValue(Double.NaN);
            // Initialize the property proxy.
            this.proxy = new DoublePropertyBase(initialValue) {
                @Override
                public Object getBean() {
                    return SliderSetup.this;
                }

                @Override
                public String getName() {
                    return "value";
                }
            };
            // Add a listener to the slider to bind the property proxy to it.
            listener = (observable, oldValue, newValue) -> {
                double validatedValue = getValidatedValue().doubleValue();
                if (validatedValue != getSliderValue())
                    setSliderValue(validatedValue);
                else
                    this.proxy.setValue(validatedValue);
            };
            slider.valueProperty().addListener(listener);
        }

        abstract protected N adjustValue(double rawValue);

        public final SliderSetup<N> setDisplay(Labeled display, String format, String tooltipText) {
            this.display = display;
            GuiPrefabs.addTooltip(display, tooltipText);
            // Add the listener to update the display's text.
            if (listenerForDisplay != null)
                proxy.removeListener(listenerForDisplay);
            listenerForDisplay = (observable, oldValue, newValue) ->
                    display.setText(String.format(format, getValidatedValue()));
            proxy.addListener(listenerForDisplay);
            return this;
        }

        public final SliderSetup<N> setOnChanged(ChangeListener<? super Number> handler) {
            if (listenerForExternal != null)
                proxy.removeListener(listenerForExternal);
            listenerForExternal = handler;
            if (listenerForExternal != null)
                proxy.addListener(listenerForExternal);
            return this;
        }

        public final SliderSetup<N> setRange(N min, N max) {
            slider.setMin(min.doubleValue());
            slider.setMax(max.doubleValue());
            return this;
        }

        public final SliderSetup<N> setTicks(N majorTickUnit, int minorTickCount) {
            slider.setMajorTickUnit(majorTickUnit.doubleValue());
            slider.setMinorTickCount(minorTickCount);
            return this;
        }

        public final SliderSetup<N> setSliderValue(double newValue) {
            slider.setValue(newValue);
            return this;
        }

        public final SliderSetup<N> setDisable(boolean disable) {
            slider.setDisable(disable);
            return this;
        }

        public final double getSliderValue() {
            return slider.getValue();
        }

        public final N getValidatedValue() {
            return adjustValue(getSliderValue());
        }
    }


    /** A useful tool to initialize a {@link ComboBox} control whose items are numeric values.
     * @param <N> The value type of the items.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class ComboBoxSetup<N extends Serializable> {
        protected final ComboBox<NamedItem<N>> comboBox;

        public ComboBoxSetup(ComboBox<NamedItem<N>> comboBox) {
            this.comboBox = comboBox;
        }

        @SafeVarargs
        public final ComboBoxSetup<N> setItems(NamedItem<N>... elements) {
            comboBox.getItems().setAll(elements);
            return this;
        }

        public final ComboBoxSetup<N> selectValue(N targetValue, String defaultName) {
            if (NamedItem.match(comboBox.getItems(), targetValue) == null)
                comboBox.getItems().add(new NamedItem<>(defaultName, targetValue));
            comboBox.getSelectionModel().select(NamedItem.match(comboBox.getItems(), targetValue));
            return this;
        }

        public final ComboBoxSetup<N> setOnNonNullValueUpdated(ChangeListener<NamedItem<N>> listener) {
            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null)
                    listener.changed(observable, oldValue, newValue);
            });
            return this;
        }
    }


    /** A useful tool to transform a {@link Canvas} into a dot picker control
     * that allows user to pick a dot on a 2D plane.
     * Note that the dot's x and y property ranges from {@code 0} (left/top) to {@code 1} (right/bottom),
     * which is called "the relative position".
     */
    public static class DotPickerSetup {
        public static final Color themeColor = GuiPrefabs.COLOR_INFO;
        public static final Color alarmColor = GuiPrefabs.COLOR_WARNING;
        public static final double metaLengthFactor = 0.025;
        public static final double referLineThreshold = 0.025;

        public final Canvas canvas;
        public final GraphicsContext gc;

        protected EventHandler<ActionEvent> listener;
        protected boolean picked = false;
        protected boolean pending = false;
        protected double pickedRelX = 0;
        protected double pickedRelY = 0;
        protected double pendingRelX = 0;
        protected double pendingRelY = 0;

        public DotPickerSetup(Canvas canvas) {
            this.canvas = canvas;
            this.gc = canvas.getGraphicsContext2D();
            canvas.setCursor(Cursor.CROSSHAIR);
            canvas.setOnMouseClicked(e -> {
                setRelXY(e.getX() / canvas.getWidth(), e.getY() / canvas.getHeight());
                if (listener != null)
                    listener.handle(new ActionEvent(this, canvas));
            });
            canvas.setOnMouseDragged(e -> setPendingRelXY(e.getX() / canvas.getWidth(), e.getY() / canvas.getHeight()));
            canvas.setOnMouseMoved(e -> setPendingRelXY(e.getX() / canvas.getWidth(), e.getY() / canvas.getHeight()));
            canvas.setOnMouseExited(e -> {
                pending = false;
                repaint();
            });
        }

        public final double getRelX() {
            return pickedRelX;
        }

        public final double getRelY() {
            return pickedRelY;
        }

        public final void setOnDotPicked(EventHandler<ActionEvent> listener) {
            this.listener = listener;
        }

        public final void setRelXY(double x, double y) {
            picked = true;
            pickedRelX = x;
            pickedRelY = y;
            repaint();
        }

        protected final void setPendingRelXY(double x, double y) {
            pending = true;
            pendingRelX = x;
            pendingRelY = y;
            repaint();
        }

        protected void repaint() {
            double maxSide = Math.max(canvas.getWidth(), canvas.getHeight());
            double lineSize = maxSide * metaLengthFactor / 2;
            double dotSize = maxSide * metaLengthFactor * 2;
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            // Rectangle
            gc.setFill(Color.WHITE);
            gc.fillRoundRect(lineSize / 2, lineSize / 2,
                    canvas.getWidth() - lineSize, canvas.getHeight() - lineSize, dotSize, dotSize);
            gc.setLineWidth(lineSize);
            gc.setStroke(themeColor.deriveColor(0, 1, 1, 0.7));
            gc.strokeRoundRect(lineSize / 2, lineSize / 2,
                    canvas.getWidth() - lineSize, canvas.getHeight() - lineSize, dotSize, dotSize);
            // Reference lines
            if (picked) {
                pickedRelX = Math.abs(pickedRelX - 0.5) <= referLineThreshold ? 0.5 : pickedRelX;
                pickedRelY = Math.abs(pickedRelY - 0.5) <= referLineThreshold ? 0.5 : pickedRelY;
            }
            if (pending) {
                gc.setFill(alarmColor.deriveColor(0, 1, 1, 0.5));
                if (Math.abs(pendingRelX - 0.5) <= referLineThreshold) {
                    pendingRelX = 0.5;
                    gc.fillRect(canvas.getWidth() / 2 - lineSize / 2, lineSize,
                            lineSize, canvas.getHeight() - lineSize * 2);
                }
                if (Math.abs(pendingRelY - 0.5) <= referLineThreshold) {
                    pendingRelY = 0.5;
                    gc.fillRect(lineSize, canvas.getHeight() / 2 - lineSize / 2,
                            canvas.getWidth() - lineSize * 2, lineSize);
                }
            }
            // Dots
            if (picked) {
                gc.setFill(themeColor);
                gc.fillRoundRect(pickedRelX * canvas.getWidth() - dotSize / 2,
                        pickedRelY * canvas.getHeight() - dotSize / 2,
                        dotSize, dotSize, dotSize, dotSize);
            }
            if (pending) {
                gc.setFill(themeColor.deriveColor(0, 1, 1, 0.3));
                gc.fillRoundRect(pendingRelX * canvas.getWidth() - dotSize / 2,
                        pendingRelY * canvas.getHeight() - dotSize / 2,
                        dotSize, dotSize, dotSize, dotSize);
            }
        }
    }


    public static final class SimpleIntegerSliderSetup extends SliderSetup<Integer> {
        public SimpleIntegerSliderSetup(Slider slider) {
            super(slider);
        }

        @Override
        protected Integer adjustValue(double rawValue) {
            return Math.toIntExact(Math.round(rawValue));
        }
    }


    public static final class SimpleMultipleIntegerSliderSetup extends SliderSetup<Integer> {
        private final float commonMultiple;

        public SimpleMultipleIntegerSliderSetup(Slider slider, float commonMultiple) {
            super(slider);
            this.commonMultiple = commonMultiple;
        }

        @Override
        protected Integer adjustValue(double rawValue) {
            return Math.toIntExact(Math.round(Math.round(rawValue / commonMultiple) * commonMultiple));
        }
    }


    public record NamedItem<N extends Serializable>(String name, N value) {
        @Override
        public String toString() {
            return name;
        }

        public static <N extends Serializable> NamedItem<N> match(List<NamedItem<N>> candidateOptions, N targetValue) {
            for (NamedItem<N> i : candidateOptions)
                if (targetValue.equals(i.value))
                    return i;
            return null;
        }
    }


    abstract public static class NoticeBar {
        protected static final double borderRadius = 8;
        protected static final double internalSpacing = 8;
        protected static final double iconScale = 0.75;
        protected static final double inserts = 4;
        protected static final double widthScale = 0.85;
        protected final Pane container;
        protected Pane noticeBar;

        public NoticeBar(Pane root) {
            container = root;
        }

        public final void refresh() {
            if (isToActivate())
                activate();
            else
                suppress();
        }

        public final void activate() {
            if (noticeBar == null) {
                noticeBar = getNoticeBar(getWidth(), getHeight());
                container.getChildren().add(noticeBar);
                ScaleTransition transition = new ScaleTransition(Const.durationFast, noticeBar);
                transition.setFromY(0.1);
                transition.setToY(1);
                transition.play();
            }
        }

        public final void suppress() {
            if (noticeBar != null) {
                final Pane finalNoticeBar = noticeBar;
                noticeBar = null;
                ScaleTransition transition = new ScaleTransition(Const.durationFast, finalNoticeBar);
                transition.setFromY(1);
                transition.setToY(0.1);
                transition.setOnFinished(e -> container.getChildren().remove(finalNoticeBar));
                transition.play();
            }
        }

        abstract protected Color getColor();

        abstract protected String getIconSVGPath();

        abstract protected String getText();

        protected boolean isToActivate() {
            throw new UnsupportedOperationException("Unimplemented method invoked");
        }

        protected double getHeight() {
            return Font.getDefault().getSize() * 2;
        }

        protected double getWidth() {
            Region region = (Region)container.getParent();
            double regionWidth = region.getWidth() - region.getInsets().getLeft() - region.getInsets().getRight();
            return regionWidth * widthScale;
        }

        protected Pane getNoticeBar(double width, double height) {
            // Colors
            Color color = getColor();
            BackgroundFill bgFill = new BackgroundFill(
                    color.deriveColor(0, 0.5, 1.5, 0.25),
                    new CornerRadii(borderRadius),
                    new Insets(inserts)
            );
            // Layouts
            HBox bar = new HBox(internalSpacing);
            bar.setBackground(new Background(bgFill));
            bar.setMinSize(width, height);
            bar.setAlignment(Pos.CENTER_LEFT);
            SVGPath icon = new SVGPath();
            icon.setContent(getIconSVGPath());
            icon.setFill(color);
            icon.setScaleX(iconScale);
            icon.setScaleY(iconScale);
            icon.setTranslateX(inserts);
            Label label = new Label(getText());
            label.setMinWidth(width * widthScale * widthScale);
            label.setPadding(new Insets(inserts));
            label.setTextFill(color);
            label.setWrapText(true);
            bar.getChildren().addAll(icon, label);
            // Click event
            try {
                Method onClick = getClass().getDeclaredMethod("onClick", MouseEvent.class);
                if (!NoticeBar.class.equals(onClick.getDeclaringClass())) {
                    // If the method "onClick" has been overridden:
                    bar.setCursor(Cursor.HAND);
                    bar.setOnMouseClicked(this::onClick);
                }
            } catch (Exception ignored) {
            }
            return bar;
        }

        protected void onClick(MouseEvent event) {
        }
    }


    abstract public static class Handbook {
        public boolean hasShown = false;

        public Handbook() {
        }

        abstract protected String getTitle();

        abstract protected String getHeader();

        abstract protected String getContent();

        abstract protected SVGPath getIcon();

        protected boolean hasShown() {
            return hasShown;
        }

        protected void setShown() {
            hasShown = true;
        }

        public void show(StackPane root) {
            GuiPrefabs.Dialogs.createCommonDialog(root,
                    getIcon(),
                    getTitle(),
                    getHeader(),
                    getContent(),
                    null
            ).show();
            setShown();
        }

        public void showIfNotShownBefore(StackPane root) {
            if (!hasShown())
                show(root);
        }
    }


    abstract public static class ControlDangerHandbook extends Handbook {
        public ControlDangerHandbook() {
            super();
        }

        @Override
        public String getTitle() {
            return "无效配置";
        }

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DANGER, GuiPrefabs.COLOR_DANGER);
        }
    }


    abstract public static class ControlWarningHandbook extends Handbook {
        public ControlWarningHandbook() {
            super();
        }

        @Override
        public String getTitle() {
            return "临界警告";
        }

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING);
        }
    }


    abstract public static class ControlHelpHandbook extends Handbook {
        private final Labeled control;

        public ControlHelpHandbook(Labeled control) {
            super();
            this.control = control;
        }

        @Override
        public String getTitle() {
            return "选项说明";
        }

        @Override
        public String getHeader() {
            return control.getText();
        }

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_HELP_ALT, GuiPrefabs.COLOR_INFO);
        }
    }


    abstract public static class HandbookEntrance {
        private static final double scale = 2.0 / 3;
        protected final StackPane root;
        protected final JFXButton target;
        protected final Handbook handbook;

        public HandbookEntrance(StackPane root, JFXButton target) {
            this.root = root;
            this.target = target;
            handbook = getHandbook();

            SVGPath graphic = getIcon();
            graphic.setScaleX(scale);
            graphic.setScaleY(scale);
            target.setText("");
            target.setGraphic(graphic);
            target.setRipplerFill(Color.GRAY);
            target.setOnAction(e -> getHandbook().show(root));
        }

        abstract protected SVGPath getIcon();

        abstract protected Handbook getHandbook();
    }


    abstract public static class DangerHandbookEntrance extends HandbookEntrance {
        public DangerHandbookEntrance(StackPane root, JFXButton target) {
            super(root, target);
            refresh();
        }

        public void refresh() {
            target.setVisible(getEntranceVisibleCondition());
        }

        public void refreshAndEnsureDisplayed() {
            if (getEntranceVisibleCondition())
                handbook.showIfNotShownBefore(root);
            target.setVisible(getEntranceVisibleCondition());
        }

        abstract protected boolean getEntranceVisibleCondition();

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DANGER, GuiPrefabs.COLOR_DANGER);
        }
    }


    abstract public static class WarningHandbookEntrance extends HandbookEntrance {
        public WarningHandbookEntrance(StackPane root, JFXButton target) {
            super(root, target);
            refresh();
        }

        public void refresh() {
            target.setVisible(getEntranceVisibleCondition());
        }

        public void refreshAndEnsureDisplayed() {
            if (getEntranceVisibleCondition())
                handbook.showIfNotShownBefore(root);
            target.setVisible(getEntranceVisibleCondition());
        }

        abstract protected boolean getEntranceVisibleCondition();

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING, GuiPrefabs.COLOR_WARNING);
        }
    }


    abstract public static class HelpHandbookEntrance extends HandbookEntrance {
        public HelpHandbookEntrance(StackPane root, JFXButton target) {
            super(root, target);
        }

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_HELP, GuiPrefabs.COLOR_INFO);
        }
    }
}
