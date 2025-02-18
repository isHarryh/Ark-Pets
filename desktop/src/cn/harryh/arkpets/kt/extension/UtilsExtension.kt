package cn.harryh.arkpets.kt.extension

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

typealias CheckCallback = (Pair<Boolean, Path>) -> Unit

fun String.checkFolderExist(callback: CheckCallback? = null): Path {
    val path = Paths.get(this)
    callback?.invoke(Pair(path.exists(), path))
    return path
}

fun String.checkFolderExist(createIfNotExists: Boolean = false): Path {
    return checkFolderExist {
        if (!it.first && createIfNotExists) {
            it.second.createDirectories()
        }
    }
}
