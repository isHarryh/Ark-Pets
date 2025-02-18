package cn.harryh.arkpets.kt.query.builders

import cn.harryh.arkpets.kt.database.DatabaseHelper
import org.ktorm.dsl.Query

abstract class QueryBuilder {
    protected val database = DatabaseHelper.getDatabase()
    abstract fun build(): Query
    abstract fun buildAndGetResult(): Any
}
