package cn.harryh.arkpets.kt

import cn.harryh.arkpets.LoggerExtension
import cn.harryh.arkpets.kt.query.builders.CharInfoQueryBuilder
import cn.harryh.arkpets.kt.query.builders.ModelAssetsQueryBuilder
import cn.harryh.arkpets.kt.query.conditions.MatchConditionType
import cn.harryh.arkpets.kt.utils.Logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(LoggerExtension::class)
class QueryBuilderTest {
    private val logger = Logger(this)

    @Test
    fun charInfoQuery() {
        var dataList = CharInfoQueryBuilder()
            .addSearchCondition("缪", MatchConditionType.FUZZY_MATCH)
            .addTagCondition("Operator", "Rarity_6")
            .addTypeCondition("Operator")
            .buildAndGetResult()
        dataList.forEach { logger.debug(it.toString()) }
        dataList = CharInfoQueryBuilder()
            .addSearchCondition("mou", MatchConditionType.FUZZY_MATCH)
            .addTagCondition("Operator", "Rarity_6")
            .addTypeCondition("Operator")
            .buildAndGetResult()
        dataList.forEach { logger.debug(it.toString()) }
    }

    @Test
    fun modelAssetsQuery() {
        val assetMap = ModelAssetsQueryBuilder("char_249_mlyss").buildAndGetResult()
        assetMap.forEach { logger.debug(it.toString()) }
    }
}
