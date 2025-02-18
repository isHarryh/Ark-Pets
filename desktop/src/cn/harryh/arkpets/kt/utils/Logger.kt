package cn.harryh.arkpets.kt.utils

import cn.harryh.arkpets.utils.Logger as LoggerImpl

class Logger(private val name: String) {
    constructor(instance: Any) : this(instance.javaClass)
    constructor(clazz: Class<*>) : this(clazz.simpleName)

    fun info(message: String) {
        LoggerImpl.info(name, message)
    }

    fun debug(message: String) {
        LoggerImpl.debug(name, message)
    }

    fun warning(message: String) {
        LoggerImpl.warn(name, message)
    }

    fun error(message: String) {
        LoggerImpl.error(name, message)
    }

    fun error(message: String, e: Throwable) {
        LoggerImpl.error(name, message, e)
    }
}
