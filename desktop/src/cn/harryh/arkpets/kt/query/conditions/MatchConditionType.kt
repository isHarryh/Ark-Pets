package cn.harryh.arkpets.kt.query.conditions

// condition match rules
enum class MatchConditionType {
    // precise matching
    // WHERE column_name = 'abc'
    PRECISE_MATCH,

    // fuzzy matching
    // WHERE column_name LIKE '%abc%'
    FUZZY_MATCH,

    // exact fuzzy matching
    // WHERE column_name LIKE '%a%b%c%'
    FULL_FUZZY_MATCH
}
