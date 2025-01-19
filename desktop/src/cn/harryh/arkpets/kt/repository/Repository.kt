package cn.harryh.arkpets.kt.repository

import cn.harryh.arkpets.kt.database.DatabaseHelper
import cn.harryh.arkpets.kt.database.entity.Metadata
import cn.harryh.arkpets.kt.database.buffer.MetadataSqlBuffer

abstract class Repository {
    protected val database = DatabaseHelper.getDatabase()
    protected var repoName: String = this.javaClass.simpleName
    protected var localPath = "${this.javaClass.simpleName}/"
    protected val metadataRepository: MetadataSqlBuffer = MetadataSqlBuffer(database)

    fun addMetaData(group: String, key: String, value: String) {
        metadataRepository.addInsert(
            Metadata {
                this.repo = repoName
                this.group = group
                this.key = key
                this.value = value
            }
        )
    }

    fun updateMetaData(metadata: Metadata) {
        metadataRepository.addUpdate(metadata)
    }

    abstract fun initRepository()
}
