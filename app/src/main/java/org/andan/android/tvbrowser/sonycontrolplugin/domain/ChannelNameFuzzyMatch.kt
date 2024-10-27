package org.andan.android.tvbrowser.sonycontrolplugin.domain

import me.xdrop.fuzzywuzzy.Applicable
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.ToStringFunction
import me.xdrop.fuzzywuzzy.algorithms.TokenSet

object ChannelNameFuzzyMatch {
  private val tokenSet: Applicable = TokenSet()
  private val toStringFunction: ToStringFunction<String> = NormalizeString()

  fun matchTop(
      channelName: String,
      channelTitleList: List<String>,
      ntop: Int,
      regExMatch: Boolean
  ): Set<Int> {
    val topChannelNameToChannelNameListMatchIndexSet: MutableSet<Int> = LinkedHashSet()
    val cs = channelName.lowercase()
    val whiteSpaceIndex = channelName.indexOf(" ")
    var cs1: String? = null
    if (whiteSpaceIndex > 0) {
      cs1 = channelName.lowercase().substring(0, whiteSpaceIndex)
    }
    var numberMatches = 0
    if (regExMatch) {
      for (i in channelTitleList.indices) {
        val ps = channelTitleList[i].lowercase()
        if (ps.matches("$cs\\b.*".toRegex())) {
          topChannelNameToChannelNameListMatchIndexSet.add(i)
          numberMatches++
          if (numberMatches == ntop) break
        }
      }
      if (numberMatches < ntop &&
          cs1 != null &&
          topChannelNameToChannelNameListMatchIndexSet.size == 0) {
        for (i in channelTitleList.indices) {
          val ps = channelTitleList[i].lowercase()
          if (ps.matches("$cs1\\b.*".toRegex())) {
            topChannelNameToChannelNameListMatchIndexSet.add(i)
            numberMatches++
            if (numberMatches == ntop) break
          }
        }
      }
    }
    if (numberMatches < ntop) {
      val matches =
          FuzzySearch.extractTop(channelName, channelTitleList, toStringFunction, tokenSet, ntop)
      for (match in matches) {
        val index = match.index
        if (index >= 0) {
          topChannelNameToChannelNameListMatchIndexSet.add(index)
          numberMatches++
          if (numberMatches == ntop) break
        }
      }
    }
    return topChannelNameToChannelNameListMatchIndexSet
  }

  fun matchAll(
      channelMap: Map<String, SonyChannel?>,
      channelTitleList: List<String>,
      channelList: List<SonyChannel>,
      regExMatch: Boolean
  ): LinkedHashMap<String, String> {
    val channelMatchResult: LinkedHashMap<String, String> = LinkedHashMap()
    channelMap.keys.forEach {
      val index1 = matchOne(it, channelTitleList, regExMatch)
      if (index1 >= 0) {
        channelMatchResult[it] = channelList[index1].uri
      }
    }
    return channelMatchResult
  }

  private fun matchOne(
      channelName: String,
      channelTitleList: List<String>,
      regExMatch: Boolean
  ): Int {
    var index = -1
    val topChannelNameToChannelNAmeListMatchIndexSet =
        matchTop(channelName, channelTitleList, 1, regExMatch)
    val iter: Iterator<*> = topChannelNameToChannelNAmeListMatchIndexSet.iterator()
    if (iter.hasNext()) index = iter.next() as Int
    return index
  }

  internal class NormalizeString : ToStringFunction<String> {
    override fun apply(s: String): String {
      return s.replace("_", "").lowercase()
    }
  }
}
