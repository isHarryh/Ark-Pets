package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

interface ModelTag : Entity<ModelTag> {
    var id: Int
    var charInfo: CharInfo
    var tag: String

    companion object : Entity.Factory<ModelTag>()
}
