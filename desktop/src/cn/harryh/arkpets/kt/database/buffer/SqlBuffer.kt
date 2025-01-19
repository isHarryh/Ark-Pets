package cn.harryh.arkpets.kt.database.buffer

import cn.harryh.arkpets.utils.Logger
import org.ktorm.database.Database

abstract class SqlBuffer<T>(protected val database: Database) {
    protected val pendingInserts = mutableListOf<T>()
    protected val pendingUpdates = mutableListOf<T>()

    fun addInsert(item: T) {
        pendingInserts.add(item)
    }

    fun addUpdate(item: T) {
        pendingUpdates.add(item)
    }

    abstract fun pendingInserts()
    abstract fun pendingUpdates()

    fun executePendingOperations() {
        database.useTransaction {
            if (pendingInserts.isNotEmpty()) {
                pendingInserts()
            }
            if (pendingUpdates.isNotEmpty()) {
                pendingUpdates()
            }
            it.commit()
        }
        Logger.debug(
            this.javaClass.simpleName,
            "Executing pending operations, inserting ${pendingInserts.size} items, update ${pendingUpdates.size} items"
        )
        pendingUpdates.clear()
        pendingInserts.clear()
    }
}
