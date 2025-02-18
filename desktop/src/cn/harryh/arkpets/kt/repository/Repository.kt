package cn.harryh.arkpets.kt.repository

import cn.harryh.arkpets.kt.database.DatabaseHelper
import cn.harryh.arkpets.kt.database.buffer.MetadataSqlBuffer
import cn.harryh.arkpets.kt.database.entity.Metadata

abstract class Repository<T> {
    protected val database = DatabaseHelper.getDatabase()
    protected lateinit var config: RepositoryConfig
    protected val metadataRepository: MetadataSqlBuffer = MetadataSqlBuffer(database)

    fun addMetaData(group: String, key: String, value: String) {
        metadataRepository.addInsert(
            Metadata {
                this.repo = config.repositoryName
                this.group = group
                this.key = key
                this.value = value
            }
        )
    }

    fun updateMetaData(metadata: Metadata) {
        metadataRepository.addUpdate(metadata)
    }

    fun setRepositoryConfig(config: RepositoryConfig) {
        this.config = config
    }

    fun initRepository(config: RepositoryConfig) {
        setRepositoryConfig(config)
        initRepository()
        if (config.makeCache) {
            getAllEntities()
        }
    }

    protected fun isConfigInitialized() = ::config.isInitialized

    abstract fun getAllEntities(): List<T>

    abstract fun initRepository()
}
