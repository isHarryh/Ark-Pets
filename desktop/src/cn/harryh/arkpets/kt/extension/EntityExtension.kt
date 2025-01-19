package cn.harryh.arkpets.kt.extension

import cn.harryh.arkpets.kt.database.entity.CharInfo
import cn.harryh.arkpets.kt.json.ModelData

fun CharInfo.updateInfo(data: ModelData, md5: String, storePath: String): CharInfo {
    this.md5 = md5
    this.assetId = data.assetId
    this.type = data.type
    this.style = data.style
    this.name = data.name
    this.appellation = data.appellation
    this.skinGroupId = data.skinGroupId
    this.skinGroupName = data.skinGroupName
    this.storePath = storePath
    return this
}
