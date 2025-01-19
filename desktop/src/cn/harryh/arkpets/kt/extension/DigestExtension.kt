package cn.harryh.arkpets.kt.extension

import cn.harryh.arkpets.kt.json.ModelData
import com.alibaba.fastjson2.JSON
import java.security.MessageDigest

fun String.md5(): String = MessageDigest
    .getInstance("MD5")
    .digest(this.toByteArray())
    .joinToString("") {
        "%02x".format(it)
    }

fun ModelData.md5(): String = JSON.toJSONString(this).md5()
fun ModelData.md5(addons: String): String = "${JSON.toJSONString(this)}.$addons".md5()
