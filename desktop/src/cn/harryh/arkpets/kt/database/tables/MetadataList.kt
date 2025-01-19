package cn.harryh.arkpets.kt.database.tables

import cn.harryh.arkpets.kt.database.entity.Metadata
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object MetadataList : Table<Metadata>("metadata") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val repo = varchar("repo").bindTo { it.repo }
    val group = varchar("group").bindTo { it.group }
    val key = varchar("key").bindTo { it.key }
    val value = varchar("value").bindTo { it.value }
}
