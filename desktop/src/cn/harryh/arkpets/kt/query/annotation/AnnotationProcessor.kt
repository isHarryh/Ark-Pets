package cn.harryh.arkpets.kt.query.annotation

import cn.harryh.arkpets.kt.query.conditions.Condition
import org.ktorm.dsl.and
import org.ktorm.dsl.or
import org.ktorm.schema.ColumnDeclaring
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

object AnnotationProcessor {
    private val fieldCache = ConcurrentHashMap<Class<*>, List<Field>>()

    @JvmStatic
    fun <T : Any> processorAnnotation(instance: T): List<ColumnDeclaring<Boolean>> {
        val groupMap = mutableMapOf<Int, Pair<ConditionGroupType, MutableList<Condition<*>>>>()
        val clazz = instance::class.java
        fieldCache.getOrPut(clazz) {
            clazz.declaredFields.onEach { it.isAccessible = true }.toList()
        }.forEach {
            val annotation = it.getAnnotation(ConditionGroup::class.java) ?: return@forEach
            val value = it.get(instance) as? Condition<*> ?: return@forEach
            groupMap.computeIfAbsent(annotation.groupId) {
                Pair(annotation.groupType, mutableListOf())
            }.second.add(value)
        }
        return groupMap
            .toSortedMap()
            .values
            .mapNotNull { (type, conditions) ->
                when (type) {
                    ConditionGroupType.AND -> conditions.map { it.applyCondition() }
                        .reduceOrNull { acc, col -> acc and col }

                    ConditionGroupType.OR -> conditions.map { it.applyCondition() }
                        .reduceOrNull { acc, col -> acc or col }
                }
            }
            .toList()
    }
}
