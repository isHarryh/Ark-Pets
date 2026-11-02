package cn.harryh.arkpets.naitves;

import cn.harryh.arkpets.natives.ObjCHelper;
import com.sun.javafx.tk.TKStage;
import com.sun.jna.Pointer;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.lang.reflect.Field;


public class MacUIHelper {
    public static void adjustNativeWindow(Stage stage) {
        long nswindow;
        try {
            Field peerF = Window.class.getDeclaredField("peer");
            peerF.setAccessible(true);
            TKStage tkStage = (TKStage) peerF.get(stage);
            nswindow = tkStage.getRawHandle();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ObjCHelper.msgSend.invoke(new Object[]{
                nswindow,
                ObjCHelper.sel("setStyleMask:"),
                32775
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                nswindow,
                ObjCHelper.sel("setTitleVisibility:"),
                1
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                nswindow,
                ObjCHelper.sel("setTitlebarAppearsTransparent:"),
                1
        });
        Pointer clearColor = ObjCHelper.msgSend.invokePointer(new Object[]{
                ObjCHelper.cls("NSColor"),
                ObjCHelper.sel("redColor")
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                nswindow,
                ObjCHelper.sel("setBackgroundColor:"),
                clearColor
        });
        /*ObjCHelper.msgSend.invoke(new Object[]{
                nswindow,
                ObjCHelper.sel("setOpaque:"),
                0
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                nswindow,
                ObjCHelper.sel("setHasShadow:"),
                0
        });*/
        //System.err.println(mask);
        processTitle(nswindow);
    }
    private static void processTitle(long nswindow) {
        Pointer buttonZoom = ObjCHelper.msgSend.invokePointer(new Object[]{
                nswindow,
                ObjCHelper.sel("standardWindowButton:"),
                2
        });
        Pointer buttonMini = ObjCHelper.msgSend.invokePointer(new Object[]{
                nswindow,
                ObjCHelper.sel("standardWindowButton:"),
                1
        });
        Pointer buttonClose = ObjCHelper.msgSend.invokePointer(new Object[]{
                nswindow,
                ObjCHelper.sel("standardWindowButton:"),
                0
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                buttonZoom,
                ObjCHelper.sel("setHidden:"),
                1
        });
        Pointer superView = ObjCHelper.msgSend.invokePointer(new Object[]{
                buttonZoom,
                ObjCHelper.sel("superview")
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                buttonClose,
                ObjCHelper.sel("setTranslatesAutoresizingMaskIntoConstraints:"),
                0
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                buttonMini,
                ObjCHelper.sel("setTranslatesAutoresizingMaskIntoConstraints:"),
                0
        });
        createConstraint(buttonClose,1,superView,1,15);
        createConstraint(buttonMini,1,buttonClose,1,20);
    }

    private static void createConstraint(Pointer item,long attr,Pointer toItem,long toAttr,double constant) {
        Pointer con = ObjCHelper.msgSend.invokePointer(new Object[]{
                ObjCHelper.cls("NSLayoutConstraint"),
                ObjCHelper.sel("constraintWithItem:attribute:relatedBy:toItem:attribute:multiplier:constant:"),
                item,
                attr,
                0,
                toItem,
                toAttr,
                1.0,
                constant
        });
        ObjCHelper.msgSend.invoke(new Object[]{
                con,
                ObjCHelper.sel("setActive:"),
                1
        });
    }
}
