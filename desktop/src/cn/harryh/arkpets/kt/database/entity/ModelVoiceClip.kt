package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

interface ModelVoiceClip : Entity<ModelVoiceClip> {
    var id: Int
    var modelVoice: ModelVoice
    var start: Double
    var duration: Double

    companion object : Entity.Factory<ModelVoiceClip>()
}
