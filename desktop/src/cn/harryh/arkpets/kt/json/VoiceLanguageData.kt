package cn.harryh.arkpets.kt.json

data class VoiceLanguageData(
    val size: Int,
    val duration: Double,
    val clips: List<VoiceChip>
)
