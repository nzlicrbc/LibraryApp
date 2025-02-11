package com.example.libraryapp.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromList(value: List<String>) = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String) = try {
        Gson().fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type)
    } catch (e: Exception) {
        emptyList<String>()
    }
}