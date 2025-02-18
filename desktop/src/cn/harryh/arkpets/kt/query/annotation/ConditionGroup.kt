package cn.harryh.arkpets.kt.query.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConditionGroup(
    val groupId: Int,
    val groupType: ConditionGroupType = ConditionGroupType.AND
)
