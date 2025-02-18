package cn.harryh.arkpets.kt.database.tables

import cn.harryh.arkpets.kt.database.entity.ModelVoice
import org.ktorm.schema.*

object ModelVoices : Table<ModelVoice>("model_voice") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelId = varchar("model_id").references(CharInfos) { it.charInfo }
    val language = varchar("language").bindTo { it.language }
    val duration = double("duration").bindTo { it.duration }
    val md5 = varchar("md5").bindTo { it.md5 }
    val exist = boolean("exist").bindTo { it.exist }
    val vcs = int("vcs").bindTo { it.vcs }
}
