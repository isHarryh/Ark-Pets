package cn.harryh.arkpets.naitves;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;


public class IShellLink extends Unknown {
    private static final Guid.GUID CLSID_ShellLink = new Guid.GUID("{00021401-0000-0000-c000-000000000046}");
    private static final Guid.GUID IID_IShellLinkW = new Guid.GUID("{000214F9-0000-0000-c000-000000000046}");
    private static final Guid.GUID IID_IPersistFile = new Guid.GUID("{0000010B-0000-0000-c000-000000000046}");

    private IShellLink(Pointer ptr) {
        super(ptr);
    }

    public static IShellLink create() {
        PointerByReference p = new PointerByReference();
        WinNT.HRESULT hr = Ole32.INSTANCE.CoCreateInstance(CLSID_ShellLink, Pointer.NULL, WTypes.CLSCTX_INPROC_SERVER, IID_IShellLinkW, p);
        COMUtils.checkRC(hr);
        return new IShellLink(p.getValue());
    }

    public void SetPath(String path) {
        int res = this._invokeNativeInt(20, new Object[]{this.getPointer(), new WString(path)});
        COMUtils.checkRC(new WinNT.HRESULT(res));
    }

    public void SetWorkingDirectory(String path) {
        int res = this._invokeNativeInt(9, new Object[]{this.getPointer(), new WString(path)});
        COMUtils.checkRC(new WinNT.HRESULT(res));
    }

    public void SetArguments(String arg) {
        int res = this._invokeNativeInt(11, new Object[]{this.getPointer(), new WString(arg)});
        COMUtils.checkRC(new WinNT.HRESULT(res));
    }

    public IPersistFile getPF() {
        PointerByReference p = new PointerByReference();
        WinNT.HRESULT hr = this.QueryInterface(new Guid.REFIID(new Guid.IID(IID_IPersistFile)), p);
        COMUtils.checkRC(hr);
        return new IPersistFile(p.getValue());
    }
}