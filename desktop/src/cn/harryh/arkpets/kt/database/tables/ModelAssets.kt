package cn.harryh.arkpets.kt.database.tables

import cn.harryh.arkpets.kt.database.entity.ModelAsset
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ModelAssets : Table<ModelAsset>("model_assets") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelId = int("model_id").references(CharInfos) { it.charInfo }
    val type = varchar("type").bindTo { it.type }
    val filename = varchar("filename").bindTo { it.filename }
    val md5 = varchar("md5").bindTo { it.md5 }
    val exist = boolean("exist").bindTo { it.exist }
    val vcs = int("vcs").bindTo { it.vcs }
}
