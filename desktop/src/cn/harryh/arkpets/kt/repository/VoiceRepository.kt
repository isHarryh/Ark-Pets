package cn.harryh.arkpets.kt.repository
//
// import cn.harryh.arkpets.Const.RepositoryConfig.VoiceRepository
// import cn.harryh.arkpets.kt.database.buffer.ModelVoiceRClipSqlBuffer
// import cn.harryh.arkpets.kt.database.buffer.ModelVoiceSqlBuffer

// object VoiceRepository : Repository() {
//
//    private val modelVoiceRepository: ModelVoiceSqlBuffer = ModelVoiceSqlBuffer(database)
//    private val modelVoiceRClipRepository: ModelVoiceRClipSqlBuffer = ModelVoiceRClipSqlBuffer(database)
//
//    init {
//        repoName = VoiceRepository.repoName
//        localPath = VoiceRepository.repoPath
//    }
//
//    override fun initRepository() {
// //        TODO("Not yet implemented")
//    }
//
//    private fun executePendingOperations() {
//        database.useTransaction {
//            metadataRepository.executePendingOperations()
//            modelVoiceRepository.executePendingOperations()
//            modelVoiceRClipRepository.executePendingOperations()
//        }
//    }
// }
