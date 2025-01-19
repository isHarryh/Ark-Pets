package cn.harryh.arkpets.kt.extension

import cn.harryh.arkpets.kt.database.tables.*
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf

val Database.metadata get() = this.sequenceOf(MetadataList)
val Database.modelAssets get() = this.sequenceOf(ModelAssets)
val Database.charInfo get() = this.sequenceOf(CharInfos)
val Database.modelTags get() = this.sequenceOf(ModelTags)
val Database.modelVoices get() = this.sequenceOf(ModelVoices)
val Database.modelVoiceClips get() = this.sequenceOf(ModelVoiceClips)
