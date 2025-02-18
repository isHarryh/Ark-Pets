package cn.harryh.arkpets.envchecker;

import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NVAPIWrapper;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.File;

import static com.sun.jna.platform.win32.WinNT.*;
import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;


public class WinGraphicsEnvCheckTask extends EnvCheckTask {
    private String failureReason;
    private String failureDetail;
    private FixMode fix;

    private final String launcherPath;
    private final String javaBin;

    public static final String NVAPI_PROFILE_NAME = "ArkPets";

    private enum FixMode {
        WIN_SAV,     // Windows Power-saving
        WIN_PERF,
        NV,          // NVIDIA OpenGL GDI and Present method
        WIN_PERF_NV, // Windows Performance, NVIDIA OpenGL GDI and Present method
        FAIL         // Can't Fix
    }

    public WinGraphicsEnvCheckTask() {
        super();
        javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
        File launcher = new File("ArkPets.exe");
        if (launcher.exists()) launcherPath = launcher.getAbsolutePath().replaceAll("\"", "\"\"");
        else launcherPath = javaBin;
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String getFailureDetail() {
        return failureDetail;
    }

    @Override
    public boolean tryFix() {
        try {
            switch (fix) {
                case NV -> setNvidiaGLSettings(launcherPath, javaBin);
                case WIN_SAV -> {
                    setWinGraphicsCard(launcherPath, false);
                    setWinGraphicsCard(javaBin, false);
                }
                case WIN_PERF -> {
                    setWinGraphicsCard(launcherPath, true);
                    setWinGraphicsCard(javaBin, true);
                }
                case WIN_PERF_NV -> {
                    setWinGraphicsCard(launcherPath, true);
                    setWinGraphicsCard(javaBin, true);
                    setNvidiaGLSettings(launcherPath, javaBin);
                }
            }
        } catch (Exception e) {
            Logger.error("System", "Failed to modify graphics settings", e);
            failureDetail = "自动设置显卡失败";
            failureReason = "尝试设置显卡时失败，请查看“常见问题解答”中的方法进行设置。";
            return false;
        }
        return true;
    }

    @Override
    public boolean canFix() {
        return fix != FixMode.FAIL;
    }

    @Override
    public boolean run() {
        String cards = wmicCheck();
        if (cards != null) {
            try {
                if (cards.contains("Intel") && cards.contains("NVIDIA")) {
                    // I+N Hybrid
                    boolean card = checkWinGraphicsCard(launcherPath, false) && checkWinGraphicsCard(javaBin, false);
                    if (!card) {
                        fix = FixMode.WIN_SAV;
                        return false;
                    }
                    return true;
                } else if (cards.contains("AMD") && cards.contains("NVIDIA")) {
                    // A+N Hybrid
                    boolean card = checkWinGraphicsCard(launcherPath, true) && checkWinGraphicsCard(javaBin, true);
                    boolean nv = checkNvidiaGLSettings();
                    if (card && nv) {
                        return true;
                    }
                    if (!card && !nv) fix = FixMode.WIN_PERF_NV;
                    else if (!card) fix = FixMode.WIN_PERF;
                    else fix = FixMode.NV;
                    return false;
                } else if (cards.contains("AMD")) {
                    // A+A Hybrid or AMD single,Fail temporary
                    fix = FixMode.FAIL;
                    failureReason = "AMD 显卡警告";
                    failureDetail = "检测到正在使用 AMD 显卡，ArkPets 尚未对 AMD 显卡进行充分测试。\n你仍可以强制运行，但桌宠背景可能会不透明。";
                    return false;
                } else if (cards.contains("NVIDIA")) {
                    // NVIDIA only
                    boolean status = checkNvidiaGLSettings();
                    if (!status) {
                        fix = FixMode.NV;
                        return false;
                    }
                    return true;
                } else if (cards.contains("Intel")) {
                    // Intel only
                    return true;
                } else {
                    // Other card (Virtual,Software,Non-mainstream...)
                    failureReason = "未知显卡警告";
                    failureDetail = "当前可能正在使用特殊显卡（虚拟显卡、软件渲染等），ArkPets 尚未对这类显卡进行测试。\n你仍可以强制运行，但可能会产生未知的问题。";
                    fix = FixMode.FAIL;
                    return false;
                }
            } catch (Exception e) {
                failureReason = "获取显卡信息失败";
                failureDetail = "当前无法获取显卡信息，请参考“常见问题解答”对显卡进行设置。";
                fix = FixMode.FAIL;
                return false;
            }
        } else {
            failureReason = "获取显卡信息失败";
            failureDetail = "当前无法获取显卡信息，请参考“常见问题解答”对显卡进行设置。";
            fix = FixMode.FAIL;
            return false;
        }
    }

    private static String wmicCheck() {
        try {
            String result = IOUtils.CommandUtil.runCommand("wmic path win32_VideoController get Name", null, null);
            if (result != null) {
                return result;
            } else {
                Logger.warn("EnvCheck", "Failed to get graphics card info");
                return null;
            }
        } catch (Exception e) {
            Logger.warn("EnvCheck", "Failed to get graphics card info");
            return null;
        }
    }

    public boolean checkNvidiaGLSettings() {
        boolean status = false;
        NVAPIWrapper.NvAPI_Initialize();
        PointerByReference sess = new PointerByReference();
        NVAPIWrapper.NvAPI_DRS_CreateSession(sess);
        NVAPIWrapper.NvAPI_DRS_LoadSettings(sess.getValue());
        PointerByReference pro = new PointerByReference();
        try {
            NVAPIWrapper.NvAPI_DRS_FindProfileByName(sess.getValue(), new WString(NVAPI_PROFILE_NAME), pro);
            status = true;
        } catch (Exception ignore) {
        }
        NVAPIWrapper.NvAPI_DRS_DestroySession(sess.getValue());
        NVAPIWrapper.NvAPI_Unload();
        return status;
    }

    public void setNvidiaGLSettings(String... path) {
        NVAPIWrapper.NvAPI_Initialize();
        PointerByReference sess = new PointerByReference();
        NVAPIWrapper.NvAPI_DRS_CreateSession(sess);
        NVAPIWrapper.NvAPI_DRS_LoadSettings(sess.getValue());
        PointerByReference prof = new PointerByReference();
        NVAPIWrapper.NVDRS_PROFILE.ByReference profile = new NVAPIWrapper.NVDRS_PROFILE.ByReference();
        NVAPIWrapper.writeStringToShortArray(NVAPI_PROFILE_NAME, profile.profileName);
        NVAPIWrapper.NvAPI_DRS_CreateProfile(sess.getValue(), profile, prof);
        for (String p : path) {
            NVAPIWrapper.NVDRS_APPLICATION.ByReference app = new NVAPIWrapper.NVDRS_APPLICATION.ByReference();
            NVAPIWrapper.writeStringToShortArray(p, app.appName);
            NVAPIWrapper.writeStringToShortArray(p, app.userFriendlyName);
            NVAPIWrapper.NvAPI_DRS_CreateApplication(sess.getValue(), prof.getValue(), app);
        }
        NVAPIWrapper.NVDRS_SETTING.ByReference glSetting = new NVAPIWrapper.NVDRS_SETTING.ByReference();
        glSetting.settingId = new NativeLong(0x2072C5A3);
        glSetting.settingType = 0;
        glSetting.currentValue.u32 = new NativeLong(1);
        //NVAPIWrapper.NVDRS_SETTING.ByReference dxgiSetting = new NVAPIWrapper.NVDRS_SETTING.ByReference();
        // todo dxgiSetting.settingId= new NativeLong(0x20D690F8);
        // todo dxgiSetting.currentValue.u32 =
        //dxgiSetting.settingType = 0;
        NVAPIWrapper.NVDRS_SETTING.ByReference optimusSetting = new NVAPIWrapper.NVDRS_SETTING.ByReference();
        optimusSetting.settingId = new NativeLong(0x10F9DC81);
        optimusSetting.currentValue.u32 = new NativeLong(1);
        optimusSetting.settingType = 0;
        NVAPIWrapper.NvAPI_DRS_SetSetting(sess.getValue(), prof.getValue(), glSetting);
        //NVAPIWrapper.NvAPI_DRS_SetSetting(sess.getValue(),prof.getValue(),dxgiSetting);
        NVAPIWrapper.NvAPI_DRS_SetSetting(sess.getValue(), prof.getValue(), optimusSetting);
        NVAPIWrapper.NvAPI_DRS_SaveSettings(sess.getValue());
        NVAPIWrapper.NvAPI_DRS_DestroySession(sess.getValue());
        NVAPIWrapper.NvAPI_Unload();
        Logger.info("EnvCheck", "Success write NVIDIA GPU settings");
    }

    public boolean checkWinGraphicsCard(String path, boolean performance) {
        WinReg.HKEYByReference outKey = new WinReg.HKEYByReference();
        int winstatus = Advapi32.INSTANCE.RegOpenKeyEx(HKEY_CURRENT_USER,
                "Software\\Microsoft\\DirectX\\UserGpuPreferences", 0, KEY_READ, outKey);
        if (winstatus != 0) throw new Win32Exception(winstatus);
        char[] data = new char[1024];
        winstatus = Advapi32.INSTANCE.RegQueryValueEx(outKey.getValue(), path, 0,
                new IntByReference(REG_SZ), data, new IntByReference(1024));
        if (winstatus != 0) {
            if (winstatus == 2) return false; // not found, uncertain card.
            throw new Win32Exception(winstatus);
        }
        String value = Native.toString(data);
        Advapi32.INSTANCE.RegCloseKey(outKey.getValue());
        if (value.contains("GpuPreference=0;")) return false; // uncertain card.
        if (value.contains("GpuPreference=1;") && !performance) return true;
        return value.contains("GpuPreference=2;") && performance;
    }

    public void setWinGraphicsCard(String path, boolean performance) {
        WinReg.HKEYByReference outKey = new WinReg.HKEYByReference();
        int winstatus = Advapi32.INSTANCE.RegOpenKeyEx(HKEY_CURRENT_USER,
                "Software\\Microsoft\\DirectX\\UserGpuPreferences", 0, KEY_WRITE, outKey);
        if (winstatus != 0) throw new Win32Exception(winstatus);
        String value = performance ? "GpuPreference=2;" : "GpuPreference=1;";
        Advapi32Util.registrySetStringValue(outKey.getValue(), path, value);
        Advapi32.INSTANCE.RegCloseKey(outKey.getValue());
        Logger.info("EnvCheck", "Success set GPU to " + (performance ? "performance" : "power-saving") + "mode");
    }

    public void removeNvidiaSettings() {
        try {
            String cards = wmicCheck();
            if (cards != null && cards.contains("NVIDIA")) {
                NVAPIWrapper.NvAPI_Initialize();
                PointerByReference sess = new PointerByReference();
                NVAPIWrapper.NvAPI_DRS_CreateSession(sess);
                NVAPIWrapper.NvAPI_DRS_LoadSettings(sess.getValue());
                PointerByReference pro = new PointerByReference();
                NVAPIWrapper.NvAPI_DRS_FindProfileByName(sess.getValue(), new WString(NVAPI_PROFILE_NAME), pro);
                NVAPIWrapper.NvAPI_DRS_DeleteProfile(sess.getValue(), pro.getValue());
                NVAPIWrapper.NvAPI_DRS_SaveSettings(sess.getValue());
                NVAPIWrapper.NvAPI_DRS_DestroySession(sess.getValue());
                NVAPIWrapper.NvAPI_Unload();
                Logger.info("EnvCheck", "Success remove NVIDIA GPU settings");
            }
        } catch (Exception ignore) {
        }
    }
}
