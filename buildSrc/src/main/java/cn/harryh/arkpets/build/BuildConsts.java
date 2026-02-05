package cn.harryh.arkpets.build;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class BuildConsts {
    public static final String appName = "ArkPets";
    public static final String appAuthor = "Harry Huang";
    public static final String appYearBegin = "2022";
    public static final String appYearCurrent;
    public static final String appCopyright;

    public static final String mainClassName = "cn.harryh.arkpets.DesktopLauncher";
    public static final File assetsDir = new File("../assets");

    static {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
        appYearCurrent = sdf.format(new Date());

        appCopyright = "Copyright (c) %s-%s %s".formatted(appYearBegin, appYearCurrent, appAuthor);
    }

    public static class PackageConfig {
        public static final String issFileRel = "docs/scripts/ExePacking.iss";
        public static final String jlinkModuleList = "java.base,java.desktop,java.logging,java.management,java.scripting,jdk.crypto.ec,jdk.localedata,jdk.unsupported";
        public static final String jlinkLocalesList = "en-US,zh-CN";
    }
}
