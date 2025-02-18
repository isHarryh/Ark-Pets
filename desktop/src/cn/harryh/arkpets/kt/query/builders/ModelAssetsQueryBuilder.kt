package cn.harryh.arkpets.kt.query.builders

import cn.harryh.arkpets.kt.database.tables.ModelAssets
import cn.harryh.arkpets.kt.model.CharData
import cn.harryh.arkpets.kt.query.annotation.ConditionGroup
import cn.harryh.arkpets.kt.query.conditions.Condition
import org.ktorm.dsl.Query
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import kotlin.system.measureNanoTime

class ModelAssetsQueryBuilder(id: String) : QueryBuilder() {
    @ConditionGroup(1)
    private val idCondition: Condition<String> = Condition(ModelAssets.modelId, id)

    constructor(charData: CharData) : this(charData.assetId)

    override fun build(): Query {
        return database
            .from(ModelAssets)
            .select()
            .where {
                idCondition.applyCondition()
            }
    }

    override fun buildAndGetResult(): Map<String, List<String>> {
        val result: Map<String, List<String>>
        val time = measureNanoTime {
            result = getQueryResult(build())
        }
        println("build and get result takes ${time.toDouble() / 1e6} ms")
        return result
    }

    fun getQueryResult(query: Query): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        for (entity in query) {
            result.getOrPut(entity[ModelAssets.type]!!) {
                mutableListOf()
            }.add(entity[ModelAssets.filename]!!)
        }
        return result
    }
}
