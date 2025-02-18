/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.transitions.EasingFunction;
import cn.harryh.arkpets.utils.GuiComponents.*;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import com.jfoenix.controls.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import static cn.harryh.arkpets.Const.durationFast;


public final class BehaviorModule implements Controller<ArkHomeFX> {
    @FXML
    private JFXCheckBox configBehaviorAllowWalk;
    @FXML
    private JFXCheckBox configBehaviorAllowSit;
    @FXML
    private JFXCheckBox configBehaviorAllowSleep;
    @FXML
    private JFXCheckBox configBehaviorAllowSpecial;
    @FXML
    private JFXSlider configBehaviorAiActivation;
    @FXML
    private Label configBehaviorAiActivationValue;
    @FXML
    private JFXCheckBox configBehaviorAllowInteract;
    @FXML
    private JFXCheckBox configBehaviorDoPeerRepulsion;
    @FXML
    private JFXCheckBox configDeployMultiMonitors;
    @FXML
    private Label configDeployMultiMonitorsStatus;
    @FXML
    private JFXSlider configDeployMarginBottom;
    @FXML
    private Label configDeployMarginBottomValue;
    @FXML
    private JFXButton toggleConfigDeployPosition;
    @FXML
    private HBox wrapperConfigDeployPosition;
    @FXML
    private Canvas configDeployPosition;

    @FXML
    private Label configTransitionAnimationLabel;
    @FXML
    private JFXComboBox<NamedItem<Float>> configTransitionAnimation;
    @FXML
    private JFXButton configTransitionAnimationHelp;
    @FXML
    private Label configTransitionDurationLabel;
    @FXML
    private JFXComboBox<NamedItem<Float>> configTransitionDuration;
    @FXML
    private JFXButton configTransitionDurationHelp;
    @FXML
    private Label configTransitionFunctionLabel;
    @FXML
    private JFXComboBox<NamedItem<String>> configTransitionFunction;
    @FXML
    private JFXButton configTransitionFunctionHelp;

    @FXML
    private JFXSlider configPhysicGravity;
    @FXML
    private Label configPhysicGravityValue;
    @FXML
    private JFXSlider configPhysicAirFriction;
    @FXML
    private Label configPhysicAirFrictionValue;
    @FXML
    private JFXSlider configPhysicStaticFriction;
    @FXML
    private Label configPhysicStaticFrictionValue;
    @FXML
    private JFXSlider configPhysicSpeedLimitX;
    @FXML
    private Label configPhysicSpeedLimitXValue;
    @FXML
    private JFXSlider configPhysicSpeedLimitY;
    @FXML
    private Label configPhysicSpeedLimitYValue;
    @FXML
    private Label configPhysicRestore;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initConfigBehavior();
        initScheduledListener();
    }

    private void initConfigBehavior() {
        configBehaviorAllowWalk.setSelected(app.config.behavior_allow_walk);
        configBehaviorAllowWalk.setOnAction(e -> {
            app.config.behavior_allow_walk = configBehaviorAllowWalk.isSelected();
            app.config.save();
        });
        configBehaviorAllowSit.setSelected(app.config.behavior_allow_sit);
        configBehaviorAllowSit.setOnAction(e -> {
            app.config.behavior_allow_sit = configBehaviorAllowSit.isSelected();
            app.config.save();
        });
        configBehaviorAllowSleep.setSelected(app.config.behavior_allow_sleep);
        configBehaviorAllowSleep.setOnAction(e -> {
            app.config.behavior_allow_sleep = configBehaviorAllowSleep.isSelected();
            app.config.save();
        });
        configBehaviorAllowSpecial.setSelected(app.config.behavior_allow_special);
        configBehaviorAllowSpecial.setOnAction(e -> {
            app.config.behavior_allow_special = configBehaviorAllowSpecial.isSelected();
            app.config.save();
        });

        SliderSetup<Integer> setupBehaviorAiActivation = new SimpleIntegerSliderSetup(configBehaviorAiActivation);
        setupBehaviorAiActivation
                .setDisplay(configBehaviorAiActivationValue, "%d 级", "活跃级别 (activation level)")
                .setRange(0, 8)
                .setTicks(1, 0)
                .setSliderValue(app.config.behavior_ai_activation)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.behavior_ai_activation = setupBehaviorAiActivation.getValidatedValue();
                    app.config.save();
                });

        configBehaviorAllowInteract.setSelected(app.config.behavior_allow_interact);
        configBehaviorAllowInteract.setOnAction(e -> {
            app.config.behavior_allow_interact = configBehaviorAllowInteract.isSelected();
            app.config.save();
        });
        configBehaviorDoPeerRepulsion.setSelected(app.config.behavior_do_peer_repulsion);
        configBehaviorDoPeerRepulsion.setOnAction(e -> {
            app.config.behavior_do_peer_repulsion = configBehaviorDoPeerRepulsion.isSelected();
            app.config.save();
        });

        configDeployMultiMonitors.setSelected(app.config.display_multi_monitors);
        configDeployMultiMonitors.setOnAction(e -> {
            app.config.display_multi_monitors = configDeployMultiMonitors.isSelected();
            app.config.save();
        });

        SliderSetup<Integer> setupDeployMarginBottom = new SimpleIntegerSliderSetup(configDeployMarginBottom);
        setupDeployMarginBottom
                .setDisplay(configDeployMarginBottomValue, "%d px", "像素 (pixel)")
                .setRange(0, 120)
                .setTicks(10, 10)
                .setSliderValue(app.config.display_margin_bottom)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.display_margin_bottom = setupDeployMarginBottom.getValidatedValue();
                    app.config.save();
                });

        GuiPrefabs.bindToggleAndWrapper(toggleConfigDeployPosition, wrapperConfigDeployPosition, durationFast);
        DotPickerSetup setupDeployPosition = new DotPickerSetup(configDeployPosition);
        setupDeployPosition.setRelXY(app.config.initial_position_x, app.config.initial_position_y);
        setupDeployPosition.setOnDotPicked(e -> {
            float x = (float)setupDeployPosition.getRelX();
            float y = (float)setupDeployPosition.getRelY();
            Logger.debug("Config", "Specified deploy position to " + x + ", " + y);
            app.config.initial_position_x = x;
            app.config.initial_position_y = y;
            app.config.save();
        });

        new ComboBoxSetup<>(configTransitionAnimation).setItems(new NamedItem<>("禁用", 0f),
                        new NamedItem<>("快速", 0.1f),
                        new NamedItem<>("标准", 0.3f),
                        new NamedItem<>("慢速", 0.6f))
                .selectValue(app.config.render_animation_mixture, app.config.render_animation_mixture + "s（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_animation_mixture = newValue.value();
                    app.config.save();
                });
        new HelpHandbookEntrance(app.body, configTransitionAnimationHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook(configTransitionAnimationLabel) {
                    @Override
                    public String getContent() {
                        return "此选项控制的是动画间切换的过渡速度，越慢的过渡会使得动画间切换越平滑。" +
                                "如果禁用过渡，那么动画间切换将会立即完成，而不会进行交叉过渡。";
                    }
                };
            }
        };
        new ComboBoxSetup<>(configTransitionDuration).setItems(new NamedItem<>("禁用", 0f),
                        new NamedItem<>("快速", 0.1f),
                        new NamedItem<>("标准", 0.3f),
                        new NamedItem<>("慢速", 0.6f))
                .selectValue(app.config.transition_duration, app.config.transition_duration + "s（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.transition_duration = newValue.value();
                    app.config.save();
                });
        new HelpHandbookEntrance(app.body, configTransitionDurationHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook(configTransitionDurationLabel) {
                    @Override
                    public String getContent() {
                        return "此选项控制角色的位置、透明度、水平翻转、高亮描边等属性的过渡速度，越慢的过渡会使得这些属性变化得越平滑。" +
                                "如果禁用过渡，这些属性的变化可能会表现得不自然。";
                    }
                };
            }
        };
        new ComboBoxSetup<>(configTransitionFunction).setItems(new NamedItem<>("线性（Linear）", EasingFunction.LINEAR.name()),
                        new NamedItem<>("正弦缓出（EaseOutSine）", EasingFunction.EASE_OUT_SINE.name()),
                        new NamedItem<>("三次方缓出（EaseOutCubic）", EasingFunction.EASE_OUT_CUBIC.name()),
                        new NamedItem<>("五次方缓出（EaseOutQuint）", EasingFunction.EASE_OUT_QUINT.name()))
                .selectValue(app.config.transition_type, app.config.transition_type)
                .setOnNonNullValueUpdated((observableValue, oldValue, newValue) -> {
                    app.config.transition_type = newValue.value();
                    app.config.save();
                });
        new HelpHandbookEntrance(app.body, configTransitionFunctionHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook(configTransitionFunctionLabel) {
                    @Override
                    public String getContent() {
                        return "缓动函数决定了属性随时间变化的快慢。" +
                                "线性函数使得属性按照固定的速度变化；缓入/缓出函数会在变化开始/结束时降低速度，从而使变化更加自然。";
                    }
                };
            }
        };

        SliderSetup<Integer> setupPhysicGravity = new SimpleMultipleIntegerSliderSetup(configPhysicGravity, 10);
        setupPhysicGravity
                .setDisplay(configPhysicGravityValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 2000)
                .setTicks(200, 10)
                .setSliderValue(app.config.physic_gravity_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_gravity_acc = setupPhysicGravity.getValidatedValue();
                    app.config.save();
                });
        SliderSetup<Integer> setupPhysicAirFriction = new SimpleMultipleIntegerSliderSetup(configPhysicAirFriction, 10);
        setupPhysicAirFriction
                .setDisplay(configPhysicAirFrictionValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 2000)
                .setTicks(200, 10)
                .setSliderValue(app.config.physic_air_friction_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_air_friction_acc = setupPhysicAirFriction.getValidatedValue();
                    app.config.save();
                });
        SliderSetup<Integer> setupPhysicStaticFriction = new SimpleMultipleIntegerSliderSetup(configPhysicStaticFriction, 10);
        setupPhysicStaticFriction
                .setDisplay(configPhysicStaticFrictionValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 2000)
                .setTicks(200, 10)
                .setSliderValue(app.config.physic_static_friction_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_static_friction_acc = setupPhysicStaticFriction.getValidatedValue();
                    app.config.save();
                });
        SliderSetup<Integer> setupPhysicSpeedLimitX = new SimpleMultipleIntegerSliderSetup(configPhysicSpeedLimitX, 10);
        setupPhysicSpeedLimitX
                .setDisplay(configPhysicSpeedLimitXValue, "%d px/s", "像素每秒 (pixel/s)")
                .setRange(0, 2000)
                .setTicks(200, 10)
                .setSliderValue(app.config.physic_speed_limit_x)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_speed_limit_x = setupPhysicSpeedLimitX.getValidatedValue();
                    app.config.save();
                });
        SliderSetup<Integer> setupPhysicSpeedLimitY = new SimpleMultipleIntegerSliderSetup(configPhysicSpeedLimitY, 10);
        setupPhysicSpeedLimitY
                .setDisplay(configPhysicSpeedLimitYValue, "%d px/s", "像素每秒 (pixel/s)")
                .setRange(0, 2000)
                .setTicks(200, 10)
                .setSliderValue(app.config.physic_speed_limit_y)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_speed_limit_y = setupPhysicSpeedLimitY.getValidatedValue();
                    app.config.save();
                });

        EventHandler<MouseEvent> configPhysicRestoreEvent = e -> {
            ArkConfig defaults = ArkConfig.getDefaultConfig();
            if (defaults != null) {
                setupPhysicGravity.setSliderValue(defaults.physic_gravity_acc);
                setupPhysicAirFriction.setSliderValue(defaults.physic_air_friction_acc);
                setupPhysicStaticFriction.setSliderValue(defaults.physic_static_friction_acc);
                setupPhysicSpeedLimitX.setSliderValue(defaults.physic_speed_limit_x);
                setupPhysicSpeedLimitY.setSliderValue(defaults.physic_speed_limit_y);
                Logger.info("Config", "Physic params restored");
            }
        };
        configPhysicRestore.setOnMouseClicked(e -> {
            configPhysicRestoreEvent.handle(e);
            app.rootModule.moduleWrapperComposer.activate(1);
        });
    }

    private void initScheduledListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return true;
                    }
                };
                task.setOnSucceeded(e ->
                        configDeployMultiMonitorsStatus.setText("检测到 " + ArkConfig.Monitor.getMonitors().length + " 个显示屏"));
                return task;
            }
        };
        ss.setDelay(new Duration(2500));
        ss.setPeriod(new Duration(5000));
        ss.setRestartOnFailure(true);
        ss.start();
    }
}
