package cn.harryh.arkpets.kt.database.tables

import cn.harryh.arkpets.kt.database.entity.ModelVoiceClip
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ModelVoiceClips : Table<ModelVoiceClip>("model_voice_clips") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val modelVoiceId = varchar("model_voice_id").references(ModelVoices) { it.modelVoice }
    val start = double("start").bindTo { it.start }
    val duration = double("duration").bindTo { it.duration }
}
