package cn.harryh.arkpets.naitves;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.WinNT;


public class IPersistFile extends Unknown {
    IPersistFile(Pointer ptr) {
        super(ptr);
    }

    public void Save(String path) {
        int res = this._invokeNativeInt(6, new Object[]{this.getPointer(), new WString(path), true});
        COMUtils.checkRC(new WinNT.HRESULT(res));
    }
}