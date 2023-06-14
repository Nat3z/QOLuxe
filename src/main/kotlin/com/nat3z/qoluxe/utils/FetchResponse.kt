package com.nat3z.qoluxe.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class FetchResponse(internal var res: String) {

    fun asString(): String {
        return this.res
    }

    fun asJson(): JsonObject {
        val gson = Gson()
        return gson.fromJson(res, JsonObject::class.java)
    }

    fun asJsonArray(): JsonArray {
        val gson = Gson()
        return gson.fromJson(res, JsonArray::class.java)
    }
}
