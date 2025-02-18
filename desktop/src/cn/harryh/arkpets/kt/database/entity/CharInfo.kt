package cn.harryh.arkpets.kt.database.entity

import org.ktorm.entity.Entity

@Suppress("ComplexInterface")
interface CharInfo : Entity<CharInfo> {
    var id: Int
    var assetId: String
    var storePath: String
    var type: String
    var style: String?
    var name: String
    var appellation: String?
    var skinGroupId: String
    var skinGroupName: String
    var syllable: String
    var md5: String

    companion object : Entity.Factory<CharInfo>()
}
