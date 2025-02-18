package cn.harryh.arkpets.kt.database.buffer

import cn.harryh.arkpets.kt.database.entity.CharInfo
import cn.harryh.arkpets.kt.database.tables.CharInfos
import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq

class CharInfoSqlBuffer(database: Database) : SqlBuffer<CharInfo>(database) {
    override fun pendingInserts() {
        database.batchInsert(CharInfos) {
            pendingInserts.forEach { info -> item { setData(it, info) } }
        }
    }

    override fun pendingUpdates() {
        database.batchUpdate(CharInfos) {
            pendingUpdates.forEach { info ->
                item {
                    setData(it, info)
                    where { it.id eq info.id }
                }
            }
        }
    }

    private fun AssignmentsBuilder.setData(it: CharInfos, info: CharInfo) {
        set(it.assetId, info.assetId)
        set(it.storePath, info.storePath)
        set(it.type, info.type)
        set(it.style, info.style)
        set(it.name, info.name)
        set(it.appellation, info.appellation)
        set(it.skinGroupId, info.skinGroupId)
        set(it.skinGroupName, info.skinGroupName)
        set(it.syllable, info.syllable)
        set(it.md5, info.md5)
    }
}
