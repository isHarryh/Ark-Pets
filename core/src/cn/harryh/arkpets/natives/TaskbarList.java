package cn.harryh.arkpets.natives;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.ptr.PointerByReference;


/** The Windows taskbar controller that implements the ITaskbarList COM interface.
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-itaskbarlist">ITaskbarList</a>
 */
public class TaskbarList extends Unknown {
    private static final Guid.GUID CLSID = new Guid.GUID("{56FDF344-FD6D-11d0-958A-006097C9A090}");
    private static final Guid.GUID IID = new Guid.GUID("{56FDF342-FD6D-11d0-958A-006097C9A090}");
    private boolean initialized = false;

    private TaskbarList(Pointer ptr) {
        super(ptr);
    }

    public static TaskbarList create() {
        Ole32.INSTANCE.CoInitialize(null);
        PointerByReference p = new PointerByReference();
        WinNT.HRESULT hr = Ole32.INSTANCE.CoCreateInstance(CLSID, Pointer.NULL, WTypes.CLSCTX_INPROC_SERVER, IID, p);
        COMUtils.checkRC(hr);
        return new TaskbarList(p.getValue());
    }

    public static TaskbarList createAndInit() {
        TaskbarList taskbarList = create();
        taskbarList.HrInit();
        return taskbarList;
    }

    /** Initializes the taskbar list object.
     * This method must be called before any other ITaskbarList methods can be called.
     */
    public void HrInit() {
        int res = this._invokeNativeInt(3, new Object[]{this.getPointer()});
        COMUtils.checkRC(new WinNT.HRESULT(res));
        initialized = true;
    }

    /** Adds an item to the taskbar.
     * @param hwnd The handle of the window to be added.
     */
    public void AddTab(WinDef.HWND hwnd) {
        if (!initialized)
            throw new IllegalStateException("TaskbarList not initialized.");
        int res = this._invokeNativeInt(4, new Object[]{this.getPointer(), hwnd});
        COMUtils.checkRC(new WinNT.HRESULT(res));
    }

    /** Deletes an item from the taskbar.
     * @param hwnd The handle of the window to be deleted.
     */
    public void DeleteTab(WinDef.HWND hwnd) {
        if (!initialized)
            throw new IllegalStateException("TaskbarList not initialized.");
        int res = this._invokeNativeInt(5, new Object[]{this.getPointer(), hwnd});
        COMUtils.checkRC(new WinNT.HRESULT(res));
    }

    @Override
    public String toString() {
        return "TaskbarList{" + "pointer=" + this.getPointer() + '}';
    }
}
