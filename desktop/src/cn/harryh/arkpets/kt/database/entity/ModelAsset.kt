package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

interface ModelAsset : Entity<ModelAsset> {
    var id: Int
    var charInfo: CharInfo
    var type: String
    var filename: String
    var md5: String
    var exist: Boolean
    var vcs: Int

    companion object : Entity.Factory<ModelAsset>()
}
