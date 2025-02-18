package cn.harryh.arkpets.kt.extension

import cn.harryh.arkpets.kt.database.tables.*
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.FunctionExpression
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.TextSqlType

val Database.metadata get() = this.sequenceOf(MetadataList)
val Database.modelAssets get() = this.sequenceOf(ModelAssets)
val Database.charInfo get() = this.sequenceOf(CharInfos)
val Database.modelTags get() = this.sequenceOf(ModelTags)
val Database.modelVoices get() = this.sequenceOf(ModelVoices)
val Database.modelVoiceClips get() = this.sequenceOf(ModelVoiceClips)
fun groupConcat(column: ColumnDeclaring<*>) = FunctionExpression(
    "GROUP_CONCAT",
    listOf(column.asExpression()),
    TextSqlType,
    false
)
