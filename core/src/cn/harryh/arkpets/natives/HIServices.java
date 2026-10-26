package cn.harryh.arkpets.natives;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public interface HIServices extends Library {
    HIServices INSTANCE = Native.load("ApplicationServices", HIServices.class);

    Pointer AXUIElementCreateSystemWide();

    boolean AXIsProcessTrusted();
}
