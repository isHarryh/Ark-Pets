package cn.harryh.arkpets.kt.query.builders

import cn.harryh.arkpets.kt.database.tables.CharInfos
import cn.harryh.arkpets.kt.database.tables.ModelTags
import cn.harryh.arkpets.kt.extension.groupConcat
import cn.harryh.arkpets.kt.model.CharData
import cn.harryh.arkpets.kt.query.annotation.AnnotationProcessor
import cn.harryh.arkpets.kt.query.annotation.ConditionGroup
import cn.harryh.arkpets.kt.query.annotation.ConditionGroupType
import cn.harryh.arkpets.kt.query.conditions.Condition
import cn.harryh.arkpets.kt.query.conditions.MatchCondition
import cn.harryh.arkpets.kt.query.conditions.MatchConditionType
import org.ktorm.dsl.*
import org.ktorm.schema.ColumnDeclaring
import kotlin.system.measureNanoTime

class CharInfoQueryBuilder : QueryBuilder() {
    @ConditionGroup(1)
    private var idCondition: Condition<String>? = null

    @ConditionGroup(1)
    private var tagsCondition: TagsCondition? = null

    @ConditionGroup(1)
    private var skinGroupIdCondition: Condition<String>? = null

    @ConditionGroup(1)
    private var skinGroupNameCondition: Condition<String>? = null

    @ConditionGroup(1)
    private var typeCondition: Condition<String>? = null

    @ConditionGroup(2, ConditionGroupType.OR)
    private var nameCondition: MatchCondition? = null

    @ConditionGroup(2, ConditionGroupType.OR)
    private var appellationCondition: MatchCondition? = null

    @ConditionGroup(2, ConditionGroupType.OR)
    private var pinYinCondition: MatchCondition? = null

    fun addIdCondition(id: String): CharInfoQueryBuilder {
        idCondition = Condition(CharInfos.assetId, id)
        return this
    }

    fun addNameCondition(
        name: String,
        type: MatchConditionType = MatchConditionType.PRECISE_MATCH
    ): CharInfoQueryBuilder {
        nameCondition = MatchCondition(CharInfos.name, name, type)
        return this
    }

    fun addTagCondition(tags: List<String>): CharInfoQueryBuilder {
        tagsCondition = TagsCondition(tags)
        return this
    }

    fun addTagCondition(vararg tags: String): CharInfoQueryBuilder {
        tagsCondition = TagsCondition(tags.toList())
        return this
    }

    fun addAppellationCondition(
        appellation: String,
        type: MatchConditionType = MatchConditionType.PRECISE_MATCH
    ): CharInfoQueryBuilder {
        appellationCondition = MatchCondition(CharInfos.appellation, appellation, type)
        return this
    }

    fun addPinYinCondition(
        pinYin: String,
        type: MatchConditionType = MatchConditionType.PRECISE_MATCH
    ): CharInfoQueryBuilder {
        pinYinCondition = MatchCondition(CharInfos.syllable, pinYin, type)
        return this
    }

    fun addTypeCondition(type: String): CharInfoQueryBuilder {
        typeCondition = Condition(CharInfos.type, type)
        return this
    }

    fun addSkinGroupIdCondition(skinGroupId: String): CharInfoQueryBuilder {
        skinGroupIdCondition = Condition(CharInfos.skinGroupId, skinGroupId)
        return this
    }

    fun addSkinGroupNameCondition(skinGroupName: String): CharInfoQueryBuilder {
        skinGroupNameCondition = Condition(CharInfos.skinGroupName, skinGroupName)
        return this
    }

    fun addSearchCondition(
        query: String,
        type: MatchConditionType = MatchConditionType.PRECISE_MATCH
    ): CharInfoQueryBuilder {
        addNameCondition(query, type)
        addAppellationCondition(query, type)
        addPinYinCondition(query, type)
        return this
    }

    override fun build(): Query {
        val conditions: List<ColumnDeclaring<Boolean>>
        val time = measureNanoTime {
            conditions = AnnotationProcessor.processorAnnotation(this)
        }
        println("Annotation processing takes ${time.toDouble() / 1e6} ms")
        return database
            .from(CharInfos)
            .innerJoin(ModelTags, CharInfos.assetId eq ModelTags.modelId)
            .select(
                CharInfos.assetId,
                CharInfos.name,
                CharInfos.type,
                CharInfos.style,
                CharInfos.appellation,
                CharInfos.skinGroupId,
                CharInfos.skinGroupName,
                groupConcat(ModelTags.tag).aliased("tags")
            )
            .where {
                conditions.reduce { acc, column -> acc and column }
            }
            .groupBy(CharInfos.id)
    }

    override fun buildAndGetResult(): List<CharData> {
        val data: List<CharData>
        val time = measureNanoTime {
            data = getQueryResult(build())
        }
        println("build and get result takes ${time.toDouble() / 1e6} ms")
        return data
    }

    class TagsCondition(private val tags: List<String>) : Condition<String>(ModelTags.tag, "") {
        override fun applyCondition(): ColumnDeclaring<Boolean> = property inList tags
    }

    companion object {
        @JvmStatic
        fun getQueryResult(query: Query): List<CharData> {
            val result = mutableListOf<CharData>()
            for (entity in query) {
                result.add(
                    CharData(
                        entity[CharInfos.assetId]!!,
                        entity[CharInfos.type]!!,
                        entity[CharInfos.style],
                        entity[CharInfos.name]!!,
                        entity[CharInfos.appellation],
                        entity[CharInfos.skinGroupId]!!,
                        entity[CharInfos.skinGroupName]!!,
                        entity.getString("tags")!!.split(",")
                    )
                )
            }
            return result
        }
    }
}
