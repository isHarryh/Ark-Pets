package cn.harryh.arkpets.kt.database.buffer

import cn.harryh.arkpets.kt.database.entity.ModelTag
import cn.harryh.arkpets.kt.database.tables.ModelTags
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelTagSqlBuffer(database: Database) : SqlBuffer<ModelTag>(database) {
    override fun pendingInserts() {
        database.batchInsert(ModelTags) {
            pendingInserts.forEach { info ->
                item {
                    set(it.modelId, info.charInfo.assetId)
                    set(it.tag, info.tag)
                }
            }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelTags) {
            pendingUpdates.forEach { info ->
                item {
                    set(it.modelId, info.charInfo.assetId)
                    set(it.tag, info.tag)
                    where { it.id eq info.id }
                }
            }
        }
    }
}
