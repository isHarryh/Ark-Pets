package cn.harryh.arkpets.utils;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;


public class NVAPIWrapper {
    public static void NvAPI_Initialize() {
        checkStatus(getFunction(0x150E828).invokeInt(new Object[]{}));
    }

    public static void NvAPI_DRS_CreateSession(PointerByReference phSession) {
        checkStatus(getFunction(0x694D52E).invokeInt(new Object[]{phSession}));
    }

    public static void NvAPI_DRS_DestroySession(Pointer hSession) {
        checkStatus(getFunction(0xDAD9CFF8).invokeInt(new Object[]{hSession}));
    }

    public static void NvAPI_DRS_LoadSettings(Pointer hSession) {
        checkStatus(getFunction(0x375DBD6B).invokeInt(new Object[]{hSession}));
    }

    public static void NvAPI_DRS_SaveSettings(Pointer hSession) {
        checkStatus(getFunction(0xFCBC7E14).invokeInt(new Object[]{hSession}));
    }

    public static void NvAPI_DRS_FindProfileByName(Pointer hSession, WString profileName, PointerByReference phProfile) {
        checkStatus(getFunction(0x7E4A9A0B).invokeInt(new Object[]{hSession, profileName, phProfile}));
    }

    public static void NvAPI_Unload() {
        checkStatus(getFunction(0xD22BDD7E).invokeInt(new Object[]{}));
    }

    public static void NvAPI_DRS_CreateApplication(Pointer hSession, Pointer hProfile, NVDRS_APPLICATION.ByReference pApplication) {
        checkStatus(getFunction(0x4347A9DE).invokeInt(new Object[]{hSession, hProfile, pApplication}));
    }

    public static void NvAPI_DRS_CreateProfile(Pointer hSession, NVDRS_PROFILE.ByReference pProfileInfo, PointerByReference phProfile) {
        checkStatus(getFunction(0xCC176068).invokeInt(new Object[]{hSession, pProfileInfo, phProfile}));
    }

    public static void NvAPI_DRS_SetSetting(Pointer hSession, Pointer hProfile, NVDRS_SETTING.ByReference setting) {
        checkStatus(getFunction(0x577DD202).invokeInt(new Object[]{hSession, hProfile, setting}));
    }

    public static void NvAPI_DRS_DeleteProfile(Pointer hSession, Pointer hProfile) {
        checkStatus(getFunction(0x17093206).invokeInt(new Object[]{hSession, hProfile}));
    }


    @Structure.FieldOrder({"version", "settingName", "settingId", "settingType", "settingLocation", "isCurrentPredefined", "isPredefinedValid", "predefinedValue", "currentValue"})
    public static class NVDRS_SETTING extends Structure {
        public NativeLong version;
        public short[] settingName = new short[2048];
        public NativeLong settingId;
        public int settingType;
        public int settingLocation;
        public NativeLong isCurrentPredefined;
        public NativeLong isPredefinedValid;
        public DrsSettingValue predefinedValue;
        public DrsSettingValue currentValue;

        public NVDRS_SETTING() {
            super();
            version = new NativeLong((size() | ((1) << 16)));
        }

        @Override
        public void read() {
            super.read();
            if (settingType == 0) {
                predefinedValue.setType(NativeLong.class);
                currentValue.setType(NativeLong.class);
            } else if (settingType == 1) {
                predefinedValue.setType(NVDRS_BINARY_SETTING.class);
                currentValue.setType(NVDRS_BINARY_SETTING.class);
            } else if (settingType == 3 || settingType == 4) {
                predefinedValue.setType(Short[].class);
                currentValue.setType(Short[].class);
            }
            predefinedValue.read();
            currentValue.read();
        }

        @Override
        public void write() {
            super.write();
            if (settingType == 0) {
                predefinedValue.setType(NativeLong.class);
                currentValue.setType(NativeLong.class);
            } else if (settingType == 1) {
                predefinedValue.setType(NVDRS_BINARY_SETTING.class);
                currentValue.setType(NVDRS_BINARY_SETTING.class);
            } else if (settingType == 3 || settingType == 4) {
                predefinedValue.setType(Short.class);
                currentValue.setType(Short.class);
            }
            predefinedValue.write();
            currentValue.write();
        }

        public static class ByReference extends NVDRS_SETTING implements Structure.ByReference {
        }
    }

    public static class DrsSettingValue extends Union {
        public NativeLong u32;
        public NVDRS_BINARY_SETTING binary;
        public short[] wsz = new short[2048];
    }

    @Structure.FieldOrder({"valueLength", "valueData"})
    public static class NVDRS_BINARY_SETTING extends Structure {
        public NativeLong valueLength;
        public byte[] valueData = new byte[4096];

        public static class ByReference extends NVDRS_BINARY_SETTING implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder({"version", "isPredefined", "appName", "userFriendlyName", "launcher"})
    public static class NVDRS_APPLICATION extends Structure {
        public NativeLong version;
        public NativeLong isPredefined;
        public short[] appName = new short[2048];
        public short[] userFriendlyName = new short[2048];
        public short[] launcher = new short[2048];

        public NVDRS_APPLICATION() {
            super();
            version = new NativeLong((size() | ((1) << 16)));
        }

        public static class ByReference extends NVDRS_APPLICATION implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder({"version", "profileName", "gpuSupport", "isPredefined", "numOfApps", "numOfSettings"})
    public static class NVDRS_PROFILE extends Structure {
        public NativeLong version;
        public short[] profileName = new short[2048];
        public NativeLong gpuSupport;

        public NativeLong isPredefined;
        public NativeLong numOfApps;
        public NativeLong numOfSettings;

        public NVDRS_PROFILE() {
            super();
            version = new NativeLong((size() | ((1) << 16)));
        }

        public static class ByReference extends NVDRS_PROFILE implements Structure.ByReference {
        }
    }

    private static Function getFunction(int id) {
        return Function.getFunction(NVAPI.INSTANCE.nvapi_QueryInterface(id));
    }

    private static void checkStatus(int status) {
        if (status != 0) {
            byte[] errmsg = new byte[64];
            getFunction(0x6C2D048C).invokeInt(new Object[]{status, errmsg});
            throw new RuntimeException("Failed to execute NVAPI, message: " + Native.toString(errmsg));
        }
    }

    private interface NVAPI extends Library {
        NVAPI INSTANCE = Native.load("nvapi64", NVAPI.class);

        Pointer nvapi_QueryInterface(int id);
    }

    public static String shortArrayToString(short[] array) {
        StringBuilder sb = new StringBuilder();
        for (short value : array) {
            if (value == 0) {
                break;
            }
            sb.append((char) value);
        }
        return sb.toString();
    }

    public static void writeStringToShortArray(String str, short[] target) {
        char[] strarr = str.toCharArray();
        for (int i = 0; i < strarr.length; i++) {
            target[i] = (short) strarr[i];
        }
    }
}
