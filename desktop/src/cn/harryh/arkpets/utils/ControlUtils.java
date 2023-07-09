/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Labeled;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;


public class ControlUtils {
    public static final String tooltipStyle = "-fx-text-fill:#FFF;-fx-font-size:10px;-fx-font-weight:normal;";


    public static class SliderUtil {
        abstract public static class SliderSetup<N extends Number> {
            protected Slider slider;
            protected Labeled display;
            protected DoubleProperty proxy;
            protected ChangeListener<? super Number> listener;
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
                tooltip.setStyle(tooltipStyle);
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
}
