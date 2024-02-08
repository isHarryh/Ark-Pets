/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.utils.GuiComponents;
import cn.harryh.arkpets.utils.Logger;
import com.jfoenix.controls.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;


public final class BehaviorModule implements Controller<ArkHomeFX> {
    @FXML
    private JFXCheckBox configBehaviorAllowWalk;
    @FXML
    private JFXCheckBox configBehaviorAllowSit;
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
            app.config.saveConfig();
        });
        configBehaviorAllowSit.setSelected(app.config.behavior_allow_sit);
        configBehaviorAllowSit.setOnAction(e -> {
            app.config.behavior_allow_sit = configBehaviorAllowSit.isSelected();
            app.config.saveConfig();
        });

        GuiComponents.SliderSetup<Integer> setupBehaviorAiActivation = new GuiComponents.SimpleIntegerSliderSetup(configBehaviorAiActivation);
        setupBehaviorAiActivation
                .setDisplay(configBehaviorAiActivationValue, "%d 级", "活跃级别 (activation level)")
                .setRange(0, 8)
                .setTicks(1, 0)
                .setSliderValue(app.config.behavior_ai_activation)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.behavior_ai_activation = setupBehaviorAiActivation.getValidatedValue();
                    app.config.saveConfig();
                });

        configBehaviorAllowInteract.setSelected(app.config.behavior_allow_interact);
        configBehaviorAllowInteract.setOnAction(e -> {
            app.config.behavior_allow_interact = configBehaviorAllowInteract.isSelected();
            app.config.saveConfig();
        });
        configBehaviorDoPeerRepulsion.setSelected(app.config.behavior_do_peer_repulsion);
        configBehaviorDoPeerRepulsion.setOnAction(e -> {
            app.config.behavior_do_peer_repulsion = configBehaviorDoPeerRepulsion.isSelected();
            app.config.saveConfig();
        });

        configDeployMultiMonitors.setSelected(app.config.display_multi_monitors);
        configDeployMultiMonitors.setOnAction(e -> {
            app.config.display_multi_monitors = configDeployMultiMonitors.isSelected();
            app.config.saveConfig();
        });

        GuiComponents.SliderSetup<Integer> setupDeployMarginBottom = new GuiComponents.SimpleIntegerSliderSetup(configDeployMarginBottom);
        setupDeployMarginBottom
                .setDisplay(configDeployMarginBottomValue, "%d px", "像素 (pixel)")
                .setRange(0, 120)
                .setTicks(10, 10)
                .setSliderValue(app.config.display_margin_bottom)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.display_margin_bottom = setupDeployMarginBottom.getValidatedValue();
                    app.config.saveConfig();
                });
        GuiComponents.SliderSetup<Integer> setupPhysicGravity = new GuiComponents.SimpleMultipleIntegerSliderSetup(configPhysicGravity, 10);
        setupPhysicGravity
                .setDisplay(configPhysicGravityValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(app.config.physic_gravity_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_gravity_acc = setupPhysicGravity.getValidatedValue();
                    app.config.saveConfig();
                });
        GuiComponents.SliderSetup<Integer> setupPhysicAirFriction = new GuiComponents.SimpleMultipleIntegerSliderSetup(configPhysicAirFriction, 10);
        setupPhysicAirFriction
                .setDisplay(configPhysicAirFrictionValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(app.config.physic_air_friction_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_air_friction_acc = setupPhysicAirFriction.getValidatedValue();
                    app.config.saveConfig();
                });
        GuiComponents.SliderSetup<Integer> setupPhysicStaticFriction = new GuiComponents.SimpleMultipleIntegerSliderSetup(configPhysicStaticFriction, 10);
        setupPhysicStaticFriction
                .setDisplay(configPhysicStaticFrictionValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(app.config.physic_static_friction_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_static_friction_acc = setupPhysicStaticFriction.getValidatedValue();
                    app.config.saveConfig();
                });
        GuiComponents.SliderSetup<Integer> setupPhysicSpeedLimitX = new GuiComponents.SimpleMultipleIntegerSliderSetup(configPhysicSpeedLimitX, 10);
        setupPhysicSpeedLimitX
                .setDisplay(configPhysicSpeedLimitXValue, "%d px/s", "像素每秒 (pixel/s)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(app.config.physic_speed_limit_x)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_speed_limit_x = setupPhysicSpeedLimitX.getValidatedValue();
                    app.config.saveConfig();
                });
        GuiComponents.SliderSetup<Integer> setupPhysicSpeedLimitY = new GuiComponents.SimpleMultipleIntegerSliderSetup(configPhysicSpeedLimitY, 10);
        setupPhysicSpeedLimitY
                .setDisplay(configPhysicSpeedLimitYValue, "%d px/s", "像素每秒 (pixel/s)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(app.config.physic_speed_limit_y)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.physic_speed_limit_y = setupPhysicSpeedLimitY.getValidatedValue();
                    app.config.saveConfig();
                });
        EventHandler<MouseEvent> configPhysicRestoreEvent = e -> {
            ArkConfig defaults = ArkConfig.defaultConfig;
            setupPhysicGravity.setSliderValue(defaults.physic_gravity_acc);
            setupPhysicAirFriction.setSliderValue(defaults.physic_air_friction_acc);
            setupPhysicStaticFriction.setSliderValue(defaults.physic_static_friction_acc);
            setupPhysicSpeedLimitX.setSliderValue(defaults.physic_speed_limit_x);
            setupPhysicSpeedLimitY.setSliderValue(defaults.physic_speed_limit_y);
            Logger.info("Config", "Physic params restored");
        };
        configPhysicRestore.setOnMouseClicked(e -> {
            configPhysicRestoreEvent.handle(e);
            app.rootModule.moduleWrapperComposer.activate(1);
        });
        if (app.config.isAllPhysicConfigZeroed())
            configPhysicRestoreEvent.handle(null);
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
        ss.setDelay(new Duration(2000));
        ss.setPeriod(new Duration(2000));
        ss.setRestartOnFailure(true);
        ss.start();
    }
}
