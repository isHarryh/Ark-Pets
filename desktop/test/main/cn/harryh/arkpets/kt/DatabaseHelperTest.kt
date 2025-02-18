package cn.harryh.arkpets.kt

import cn.harryh.arkpets.LoggerExtension
import cn.harryh.arkpets.kt.database.DatabaseHelper
import cn.harryh.arkpets.kt.extension.metadata
import cn.harryh.arkpets.kt.utils.Logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ktorm.entity.forEach

@ExtendWith(LoggerExtension::class)
class DatabaseHelperTest {
    private val logger = Logger(this)

    @Test
    fun getDatabase() {
        val database = DatabaseHelper.getDatabase()
        database.metadata.forEach { logger.debug(it.toString()) }
    }
}
