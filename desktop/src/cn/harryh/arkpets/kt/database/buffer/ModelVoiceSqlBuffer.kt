package cn.harryh.arkpets.kt.database.buffer

import cn.harryh.arkpets.kt.database.entity.ModelVoice
import cn.harryh.arkpets.kt.database.tables.ModelVoices
import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelVoiceSqlBuffer(database: Database) : SqlBuffer<ModelVoice>(database) {
    override fun pendingInserts() {
        database.batchInsert(ModelVoices) {
            pendingInserts.forEach { info -> item { setData(it, info) } }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelVoices) {
            pendingUpdates.forEach { info ->
                item {
                    setData(it, info)
                    where { it.id eq info.id }
                }
            }
        }
    }

    private fun AssignmentsBuilder.setData(it: ModelVoices, info: ModelVoice) {
        set(it.modelId, info.charInfo.assetId)
        set(it.duration, info.duration)
        set(it.language, info.language)
        set(it.md5, info.md5)
        set(it.exist, info.exist)
        set(it.vcs, info.vcs)
    }
}
