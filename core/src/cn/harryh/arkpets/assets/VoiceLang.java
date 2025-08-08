package cn.harryh.arkpets.assets;

public enum VoiceLang {
    CN("普通话"),
    JP("日语"),
    KR("韩语"),
    EN("英语"),
    OFF("关闭"),
    CUSTOM("个性化");

    private final String langName;

    public String getLangName() {
        return langName;
    }

    public int getIconY() {
        return 64 * this.ordinal();
    }

    VoiceLang(String langName) {
        this.langName = langName;
    }
}
