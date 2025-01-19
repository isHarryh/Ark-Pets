package cn.harryh.arkpets.kt.json

data class ModelConfig(
    val storageDirectory: Map<String, String>,
    val sortTags: Map<String, String>,
    val gameDataVersionDescription: String,
    val gameDataServerRegion: String,
    val data: Map<String, ModelData>,
    val arkPetsCompatibility: List<Int>
)
