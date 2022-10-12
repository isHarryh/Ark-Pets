/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.isharryh.arkpets.utils.JavaProcess;
import com.isharryh.arkpets.utils.SkinLoader;
import com.isharryh.arkpets.behaviors.*;
import com.isharryh.arkpets.utils.AssetCtrl;
import java.io.IOException;
import java.util.ArrayList;


public class ArkHome extends ApplicationAdapter {
    // Sence2d.ui
    private Stage mainStage;
    private Skin skin;
    private ArkChar preview;

    public int WD_W = 500;
    public int WD_H = 310;
    public int status = 1;
    public final String APP_TITLE;
    public HWND HWND_MINE;
    public ArkConfig config;

    /** Create the gdx app ArkHome.
     * @param $title The window title for the app.
     */
    public ArkHome(String $title) {
        APP_TITLE = $title;
    }

    @Override
    public void create() {
        // When the APP was created
		Gdx.app.log("event", "AH:Create");
        skin = SkinLoader.loadSkin(Gdx.files.internal("newmetalui/metal-ui.json"));
        config = ArkConfig.init();
        hideArkHome(false);
		Gdx.app.log("event", "AH:Render");
        mainStage = stageMainPage();
    }

    @Override
	public void render() {
        if (status == 0) {
            Gdx.app.log("event", "AH:Hide");
            hideArkHome(true);
            startArkPets();
            Gdx.app.exit();
        }
		ScreenUtils.clear(.70f, .78f, .86f, 1f);
        if (preview != null)
            preview.next();
		mainStage.act(Gdx.graphics.getDeltaTime());
		mainStage.draw();
	}

    @Override
    public void resize(int newWidth, int newHeight) {
    }

    @Override
	public void dispose() {
		Gdx.app.log("event", "AH:Dispose");
	}


    /* User Interface Area */
    /** Main page.
     * @return Stage object.
     */
    private Stage stageMainPage() {
        // Elements' containers
        HorizontalGroup[] hGroups;
        SelectBox[] sBoxs;
        CheckBox[] cBoxs;
        Stage stage = new Stage();
        Table table = new Table();
        table.setFillParent(true);

        // Actor lists
        hGroups = new HorizontalGroup[10];
        sBoxs = new SelectBox[5];
        cBoxs = new CheckBox[5];

        // Text buttons
        TextButton tButton1 = new TextButton("启动", skin);
        tButton1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent iEvent, float a, float b) {
                if (config.character_recent != "")
                    status = 0;
                else
                    mainStage = stageWarning("当前未选择任何模型");
            }
        });

        // Select Model
        sBoxs[0] = new SelectBox<String>(skin);
        config.display_monitor_info = MonitorConfig.getDefaultMonitorInfo();
        AssetCtrl[] assets = initAssetCtrls(Gdx.files.local("models"));
        if (assets == null || assets.length <= 0) {
            sBoxs[0].setItems("未找到模型");
            sBoxs[0].setSelectedIndex(0);
            config.character_recent = "";
        } else {
            sBoxs[0].setItems(assets);
            sBoxs[0].setSelectedIndex(0);
            for (int i = 0; i < assets.length; i++)
                if (assets[i].PATH.equals(config.character_recent)) {
                    sBoxs[0].setSelectedIndex(i);
                    initPreview();
                    break;
                }
            config.character_recent = assets[sBoxs[0].getSelectedIndex()].PATH;
        }
        
        config.saveConfig();
        sBoxs[0].addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
                if (sBoxs[0].getSelected().getClass() == String.class)
                    config.character_recent = "";
                else {
                    config.character_recent = assets[sBoxs[0].getSelectedIndex()].PATH;
                    initPreview();
                }
                config.saveConfig();
			}
		});
        
        hGroups[0] = new HorizontalGroup();
        hGroups[0].addActor(new Label("选择模型 ", skin));
        hGroups[0].addActor(sBoxs[0]);

        // Set Scale
        sBoxs[1] = new SelectBox<String>(skin);
        sBoxs[1].setItems(0.5f, 1.0f, 2.0f);
        for (int i = 0; i < sBoxs[1].getItems().size; i++) {
            if (ArkConfig.compare(sBoxs[1].getItems().get(i), config.display_scale))
                sBoxs[1].setSelectedIndex(i);
        }
        sBoxs[1].addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
                config.display_scale = Float.valueOf(sBoxs[1].getSelected().toString());
                config.saveConfig();
			}
		});
        
        hGroups[1] = new HorizontalGroup();
        hGroups[1].addActor(new Label("图像缩放 ", skin));
        hGroups[1].addActor(sBoxs[1]);

        // Set FPS
        sBoxs[3] = new SelectBox<String>(skin);
        sBoxs[3].setItems(20, 25, 30, 45, 60);
        for (int i = 0; i < sBoxs[3].getItems().size; i++) {
            if (config.compare(sBoxs[3].getItems().get(i), config.display_fps))
                sBoxs[3].setSelectedIndex(i);
        }
        sBoxs[3].addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
                config.display_fps = Integer.valueOf(sBoxs[3].getSelected().toString());
                config.saveConfig();
			}
		});
        
        hGroups[3] = new HorizontalGroup();
        hGroups[3].addActor(new Label("最大帧率 ", skin));
        hGroups[3].addActor(sBoxs[3]);

        // Set Walk
        cBoxs[0] = new CheckBox("允许行走", skin);
        cBoxs[0].setChecked(config.behavior_allow_walk);
        cBoxs[0].addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                config.behavior_allow_walk = cBoxs[0].isChecked();
                config.saveConfig();
            }
        });

        // Set Sit
        cBoxs[1] = new CheckBox("允许坐下", skin);
        cBoxs[1].setChecked(config.behavior_allow_sit);
        cBoxs[1].addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                config.behavior_allow_sit = cBoxs[1].isChecked();
                config.saveConfig();
            }
        });

        // Set Interact
        cBoxs[2] = new CheckBox("允许交互", skin);
        cBoxs[2].setChecked(config.behavior_allow_interact);
        cBoxs[2].addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                config.behavior_allow_interact = cBoxs[2].isChecked();
                config.saveConfig();
            }
        });

        // Set Margin
        Slider slider1 = new Slider(0, 120, 5, false, skin);
        Label slider1Lable1 = new Label("下边界距离", skin);
        Label slider1Lable2 = new Label(String.valueOf(config.display_margin_bottom), skin);
        slider1.setValue(config.display_margin_bottom);
        slider1.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                config.display_margin_bottom = (int)slider1.getValue();
                slider1Lable2.setText((int)slider1.getValue());
                config.saveConfig();
            }
        });

        // Help button
        TextButton tButton2 = new TextButton("使用手册", skin);
        tButton2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent iEvent, float a, float b) {
                Gdx.net.openURI("https://github.com/isHarryh/Ark-Pets#ark-pets");
            }
        });

        // Add actors
        table.addActor(hGroups[0]);
        table.addActor(hGroups[1]);
        table.addActor(hGroups[3]);
        table.addActor(cBoxs[0]);
        table.addActor(cBoxs[1]);
        table.addActor(cBoxs[2]);
        table.addActor(tButton1);
        table.addActor(tButton2);
        table.addActor(slider1);
        table.addActor(slider1Lable1);
        table.addActor(slider1Lable2);
        
        // Merge table & stage 
        ScrollPane sPane = new ScrollPane(table, skin);
        sPane.setFillParent(true);
        Gdx.input.setInputProcessor(stage);
        stage.addActor(sPane);
        initAssetCtrls(Gdx.files.local("./models"));

        // Set actors' position
        final float WD_W_CT = (float)(WD_W*0.5-tButton1.getWidth()/2);
        hGroups[0].setPosition(10, WD_H - 25);
        hGroups[1].setPosition(10, WD_H - 65);
        hGroups[3].setPosition(10, WD_H - 105);
        cBoxs[0].setPosition(10, WD_H - 165);
        cBoxs[1].setPosition(WD_W_CT, WD_H - 165);
        cBoxs[2].setPosition(10, WD_H - 195);
        tButton1.setPosition(WD_W_CT, 5);
        tButton2.setPosition(10, 5);
        slider1.setPosition(150, WD_H - 250);
        slider1Lable1.setPosition(10, WD_H - 250);
        slider1Lable2.setPosition(110, WD_H - 250);
        return stage;
    }

    /** Warning Page.
     * @param $msg Warning message.
     * @return Stage object.
     */
    private Stage stageWarning(String $msg) {
        // Elements' containers
        Stage stage = new Stage();
        Table table = new Table();
        TextButton close = new TextButton("确认", skin);
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent iEvent, float a, float b) {
                mainStage = stageMainPage();
            }
        });
        table.setFillParent(true);

        // Show them
        final float WD_W_CT = (float)(WD_W*0.5);
        Window window = new Window(" 警 告 ", skin);
        ScrollPane innerPane = new ScrollPane(new Label($msg, skin), skin);
        window.defaults().spaceBottom(10);
        window.setSize(WD_W, WD_H);
        window.setPosition(0, 0, Align.center);
        innerPane.setFadeScrollBars(true);
        innerPane.setSize((int)(WD_W * 0.95f), (int)(WD_H * 0.95f));
        innerPane.setPosition(WD_W_CT, WD_H, Align.top | Align.center);
        close.setPosition(WD_W - 5, 5, Align.bottomRight);
        
        // Add actors
        table.addActor(window);
        table.addActor(innerPane);
        table.addActor(close);
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
        return stage;
    }

    /* Functional Area */
    /** Initialize the preview of model.
     */
    private void initPreview() {
        try {
            preview = new ArkChar(config.character_recent+".atlas", config.character_recent+".skel", 0.36f);
            preview.setCanvas(WD_W, WD_H, 25, new Color(.70f, .78f, .86f, 1f));
            preview.setPositionTar(400, 10, 0);
            preview.setPositionCur(1);
            preview.setPositionTar(400, 10, -1);
            Behavior[] candidateBehaviors = {new BehaviorOperBuild2(config), new BehaviorOperBuild(config)};
            Behavior behavior = Behavior.selectBehavior(preview.anim_list, candidateBehaviors);
            if (behavior == null) {
                preview = null;
                mainStage = stageWarning("此类模型可能尚未被本应用支持");
                return;
            }
            preview.setAnimation(behavior.defaultAnim());
        } catch(Exception e) {
            preview = null;
            mainStage = stageWarning("加载模型预览时发生了错误\n"+e.toString());
            return;
        }
    }

    /** Run the EmbeddedLauncher to launch the ArkPets app.
     */
    private void startArkPets() {
        mainStage.dispose();
        skin.dispose();
        preview = null;
        System.gc();
        try {
            JavaProcess.exec(EmbeddedLauncher.class);
        } catch (IOException e) {
            Gdx.app.error("error", "CAUGHT: IOException");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Gdx.app.error("error", "CAUGHT: InterruptException");
            e.printStackTrace();
        }
    }

    /** Change the visibility of ArkHome app.
     * @param enable true=hide, false=show.
     * @return Success.
     */
    private boolean hideArkHome(boolean enable) {
        if (HWND_MINE == null)
            HWND_MINE = User32.INSTANCE.FindWindow(null, APP_TITLE);
            if (HWND_MINE == null)
                return false;
        User32.INSTANCE.ShowWindow(HWND_MINE, enable ? WinUser.SW_HIDE : WinUser.SW_SHOW);
        return true;
    }

    /** Get a array of AssetCtrls in the specific root dir.
     * @param $rootDir The root dir.
     * @return An array consists of AssetCtrls.
     */
    private AssetCtrl[] initAssetCtrls(FileHandle $rootDir) {
        // Verify root dir accessibility
        if (!$rootDir.exists() || !$rootDir.isDirectory())
            return null;
        FileHandle[] fHs = $rootDir.list();
        ArrayList<AssetCtrl> results = new ArrayList<AssetCtrl>();
        // Collect assets
        for (FileHandle cur: fHs) { // cur: dir in root dir
            if (!cur.exists() || !cur.isDirectory())
                continue;
            String curName = cur.nameWithoutExtension();
            FileHandle[] subFHs = cur.list();
            for (FileHandle subcur: subFHs) { // subcur: file in cur dir
                if (!subcur.isDirectory() && subcur.extension().equals("atlas")) {
                    String subcurName = subcur.nameWithoutExtension();
                    if (subcur.sibling(subcurName+".png").exists() && subcur.sibling(subcurName+".skel").exists()) {
                        results.add(new AssetCtrl(curName, subcur));
                        break;
                    }
                }
            }
        }
        // Convert ArrayList<AssetCtrl> to AssetCtrl[]
        AssetCtrl[] assetCtrls = new AssetCtrl[results.size()];
        for (int i = 0; i < results.size(); i++)
            assetCtrls[i] = results.get(i);
        return assetCtrls;
    }
}
