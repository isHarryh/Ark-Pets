package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

interface ModelVoice : Entity<ModelVoice> {
    var id: Int
    var charInfo: CharInfo
    var language: String
    var duration: Double
    var md5: String
    var exist: Boolean
    var vcs: Int

    companion object : Entity.Factory<ModelVoice>()
}
