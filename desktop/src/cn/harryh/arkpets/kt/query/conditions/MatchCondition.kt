package cn.harryh.arkpets.kt.query.conditions

import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.schema.ColumnDeclaring

open class MatchCondition(
    property: ColumnDeclaring<String>,
    value: String,
    private val type: MatchConditionType = MatchConditionType.PRECISE_MATCH
) : Condition<String>(property, value) {
    override fun applyCondition(): ColumnDeclaring<Boolean> {
        return when (type) {
            MatchConditionType.PRECISE_MATCH -> {
                property eq value
            }

            MatchConditionType.FUZZY_MATCH -> {
                property like "%$value%"
            }

            MatchConditionType.FULL_FUZZY_MATCH -> {
                val pattern = buildString {
                    append("%")
                    value.forEach {
                        append(it).append("%")
                    }
                }
                property like pattern
            }
        }
    }
}
