package com.nat3z.qoluxe.utils

import com.nat3z.qoluxe.QOLuxeConfig

object LithiumServerUtils {

    fun getLithiumRealmsLocation(uuid: Long): String {
        return QOLuxeConfig.lithiumRealmsURL.split(";").find { it.startsWith("$uuid:") }?.substringAfter("$uuid:") ?: ""
    }

    fun doesPackExist(uuid: Long): Boolean {
        val url = getLithiumRealmsLocation(uuid)
        println(url)
        var result = false
        WebUtils.fetch(url + "/doespackexist", "GET",
            { res ->
                result = res.asJson().get("success").asBoolean
            },
            { err ->
            }
        )
        return result
    }

    fun getResourcePackHash(uuid: Long): String {
        var result = ""
        WebUtils.fetch(getLithiumRealmsLocation(uuid) + "/gethash", "GET",
            { hash ->
                if (hash.asJson().get("success").asBoolean) {
                    result = hash.asJson().get("hash").asString
                }
            },
            { err ->
                result = "FAILED"
            }
        )
        return result
    }
}
