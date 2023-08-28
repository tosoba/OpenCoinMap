package com.trm.opencoinmap.core.database.converter

import androidx.room.TypeConverter

class StringListConverter {
  @TypeConverter
  fun fromList(stringList: List<String>?): String? = stringList?.joinToString(separator = ",")

  @TypeConverter fun toList(string: String?): List<String>? = string?.split(",")?.map(String::trim)
}
