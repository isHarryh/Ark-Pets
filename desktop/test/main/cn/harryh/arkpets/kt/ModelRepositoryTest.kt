package cn.harryh.arkpets.kt

import cn.harryh.arkpets.Const
import cn.harryh.arkpets.LoggerExtension
import cn.harryh.arkpets.kt.repository.ModelRepository
import cn.harryh.arkpets.kt.repository.RepositoryConfig
import cn.harryh.arkpets.kt.utils.Logger
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.system.measureNanoTime

@ExtendWith(LoggerExtension::class)
class ModelRepositoryTest {
    private val logger = Logger(this)

    @Test
    @Order(1)
    fun initRepository() {
        val modelRepositoryConfig = RepositoryConfig(
            Const.RepositoryConfig.ModelRepository.repoName,
            Const.RepositoryConfig.ModelRepository.localPath,
            Const.RepositoryConfig.ModelRepository.remotePath,
            Const.RepositoryConfig.ModelRepository.metadataFileName,
            Const.RepositoryConfig.ModelRepository.metadataFileName
        )
        val time = measureNanoTime {
            ModelRepository.initRepository(modelRepositoryConfig)
        }
        logger.debug("ModelRepository initRepository took ${time.toDouble() / 1e6} ms")
    }

    @Test
    @Order(2)
    fun getAllEntities() {
        val data = ModelRepository.getAllEntities()
        assert(data.isNotEmpty())
        logger.debug("Get all entities from the DB, got ${data.size} items")
    }
}
