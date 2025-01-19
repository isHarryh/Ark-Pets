package cn.harryh.arkpets.kt.database.buffer

import cn.harryh.arkpets.kt.database.entity.ModelVoiceClip
import cn.harryh.arkpets.kt.database.tables.ModelVoiceClips
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelVoiceRClipSqlBuffer(database: Database) : SqlBuffer<ModelVoiceClip>(database) {
    override fun pendingInserts() {
        database.batchInsert(ModelVoiceClips) {
            pendingInserts.forEach { info ->
                item {
                    set(it.modelVoiceId, info.modelVoice.id)
                    set(it.duration, info.duration)
                    set(it.start, info.start)
                }
            }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelVoiceClips) {
            pendingUpdates.forEach { info ->
                item {
                    set(it.modelVoiceId, info.modelVoice.id)
                    set(it.duration, info.duration)
                    set(it.start, info.start)
                    where { it.id eq info.id }
                }
            }
        }
    }
}
