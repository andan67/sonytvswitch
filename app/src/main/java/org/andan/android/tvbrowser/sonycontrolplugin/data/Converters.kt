package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.TypeConverter

private const val KEY_VALUE_SEPARATOR = "->"
private const val ENTRY_SEPARATOR = "||"

class Converters {
  /** return key1->value1||key2->value2||key3->value3 */
  @TypeConverter
  fun mapToString(map: Map<String, String>): String {
    return map.entries.joinToString(separator = ENTRY_SEPARATOR) {
      "${it.key}$KEY_VALUE_SEPARATOR${it.value}"
    }
  }

  /** return map of String, String "key1": "value1" "key2": "value2" "key3": "value3" */
  @TypeConverter
  fun stringToMap(string: String): Map<String, String> {
    return if (string.isEmpty()) emptyMap()
    else
        string.split(ENTRY_SEPARATOR).associate {
          val (key, value) = it.split(KEY_VALUE_SEPARATOR)
          key to value
        }
  }

  /** return value1||value2||value3 */
  @TypeConverter
  fun listToString(list: List<String>): String {
    return list.joinToString(separator = ENTRY_SEPARATOR)
  }

  /** return list of String values */
  @TypeConverter
  fun stringToList(string: String): List<String> {
    return if (string.isEmpty()) emptyList() else string.split(ENTRY_SEPARATOR)
  }
}
