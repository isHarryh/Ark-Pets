package cn.harryh.arkpets

import cn.harryh.arkpets.Const.LogConfig
import cn.harryh.arkpets.utils.Logger
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LoggerExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        Logger.initialize(LogConfig.logDesktopPath, LogConfig.logDesktopMaxKeep)
        Logger.setLevel(Logger.DEBUG)
    }
}
