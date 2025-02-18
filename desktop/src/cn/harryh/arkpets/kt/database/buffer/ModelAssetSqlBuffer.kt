package cn.harryh.arkpets.kt.database.buffer

import cn.harryh.arkpets.kt.database.entity.ModelAsset
import cn.harryh.arkpets.kt.database.tables.ModelAssets
import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class ModelAssetSqlBuffer(database: Database) : SqlBuffer<ModelAsset>(database) {

    override fun pendingInserts() {
        database.batchInsert(ModelAssets) {
            pendingInserts.forEach { info -> item { setData(it, info) } }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(ModelAssets) {
            pendingUpdates.forEach { info ->
                item {
                    setData(it, info)
                    where { it.id eq info.id }
                }
            }
        }
    }

    private fun AssignmentsBuilder.setData(it: ModelAssets, info: ModelAsset) {
        set(it.modelId, info.charInfo.assetId)
        set(it.type, info.type)
        set(it.filename, info.filename)
        set(it.md5, info.md5)
        set(it.exist, info.exist)
        set(it.vcs, info.vcs)
    }
}
