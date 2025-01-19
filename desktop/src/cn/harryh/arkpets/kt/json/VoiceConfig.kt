package cn.harryh.arkpets.kt.json

data class VoiceConfig(
    val localizations: Map<String, Map<String, String>>,
    val storageDirectory: Map<String, String>,
    val gameDataVersionDescription: String,
    val gameDataServerRegion: String,
    val data: Map<String, VoiceData>,
    val audioTypes: Map<String, String>,
    val audioFormat: String,
    val arkPetsCompatibility: List<Int>
)
