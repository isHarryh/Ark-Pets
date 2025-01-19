package cn.harryh.arkpets.kt.database.tables

import cn.harryh.arkpets.kt.database.entity.CharInfo
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object CharInfos : Table<CharInfo>("char_info") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val assetId = varchar("asset_id").bindTo { it.assetId }
    val storePath = varchar("store_path").bindTo { it.storePath }
    val type = varchar("type").bindTo { it.type }
    val style = varchar("style").bindTo { it.style }
    val name = varchar("name").bindTo { it.name }
    val appellation = varchar("appellation").bindTo { it.appellation }
    val skinGroupId = varchar("skin_group_id").bindTo { it.skinGroupId }
    val skinGroupName = varchar("skin_group_name").bindTo { it.skinGroupName }
    val md5 = varchar("md5").bindTo { it.md5 }
}
