/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import javafx.animation.ScaleTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

import java.lang.reflect.Method;


public class GuiComponents {
    abstract public static class NoticeBar {
        protected static final double borderRadius = 8;
        protected static final double internalSpacing = 8;
        protected static final double iconScale = 0.75;
        protected static final double margin = 4;
        protected static final double widthScale = 0.9;
        protected final Pane container;
        protected Pane noticeBar;

        public NoticeBar(Pane root) {
            container = root;
        }

        public final void refresh() {
            if (getActivated() && noticeBar == null) {
                noticeBar = getNoticeBar(getWidth(), getHeight());
                container.getChildren().add(noticeBar);
                ScaleTransition transition = new ScaleTransition(Const.durationFast, noticeBar);
                transition.setFromY(0.1);
                transition.setToY(1);
                transition.play();
            } else if (!getActivated() && noticeBar != null) {
                final Pane finalNoticeBar = noticeBar;
                noticeBar = null;
                ScaleTransition transition = new ScaleTransition(Const.durationFast, finalNoticeBar);
                transition.setFromY(1);
                transition.setToY(0.1);
                transition.setOnFinished(e -> container.getChildren().remove(finalNoticeBar));
                transition.play();
            }
        }

        abstract protected boolean getActivated();

        abstract protected String getColorString();

        abstract protected String getIconSVGPath();

        abstract protected String getText();

        protected double getHeight() {
            return Font.getDefault().getSize() * 3;
        }

        protected double getWidth() {
            Region region = (Region)container.getParent();
            double regionWidth = region.getWidth() - region.getInsets().getLeft() - region.getInsets().getRight();
            return regionWidth * widthScale;
        }

        protected Pane getNoticeBar(double width, double height) {
            // Colors
            Color color = Color.valueOf(getColorString());
            BackgroundFill bgFill = new BackgroundFill(
                    color.deriveColor(0, 0.62, 1.62, 0.38),
                    new CornerRadii(borderRadius),
                    new Insets(margin)
            );
            // Layouts
            HBox bar = new HBox(internalSpacing);
            bar.setBackground(new Background(bgFill));
            bar.setMaxSize(width, height);
            bar.setAlignment(Pos.CENTER_LEFT);
            SVGPath icon = new SVGPath();
            icon.setContent(getIconSVGPath());
            icon.setFill(color);
            icon.setScaleX(iconScale);
            icon.setScaleY(iconScale);
            icon.setTranslateX(margin);
            Label label = new Label(getText());
            label.setTextFill(color);
            label.setMinWidth(width * widthScale * widthScale);
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
            // Initialize the tooltip for the display node.
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle(GuiPrefabs.tooltipStyle);
            display.setTooltip(tooltip);
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

        public final double getSliderValue() {
            return slider.getValue();
        }

        public final N getValidatedValue() {
            return adjustValue(getSliderValue());
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
}
