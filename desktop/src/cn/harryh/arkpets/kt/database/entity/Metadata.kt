package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

interface Metadata : Entity<Metadata> {
    var id: Int
    var repo: String
    var group: String
    var key: String
    var value: String

    companion object : Entity.Factory<Metadata>()
}
