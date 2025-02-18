package cn.harryh.arkpets.kt.query.conditions

import org.ktorm.dsl.eq
import org.ktorm.schema.ColumnDeclaring

open class Condition<T : Any>(
    protected val property: ColumnDeclaring<T>,
    protected val value: T
) {
    open fun applyCondition(): ColumnDeclaring<Boolean> = property eq value
}
