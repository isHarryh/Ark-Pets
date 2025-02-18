package cn.harryh.arkpets.kt.model

data class CharData(
    val assetId: String,
    val type: String,
    val style: String?,
    val name: String,
    val appellation: String?,
    val skinGroupId: String,
    val skinGroupName: String,
    val sortTags: List<String>
)
