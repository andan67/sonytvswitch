package org.andan.av.sony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.TokenSet;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;

import static java.lang.Math.min;

public class ProgramFuzzyMatch {

    private static final Applicable tokenSet = new TokenSet();

    private static final ToStringFunction toStringFunction = new NormalizeString();

    public static Set<Integer> matchTop(String channelName, List<String> programTitleList, int ntop, boolean regExMatch) {
        Set<Integer> topChannelNameToProgramTitleMatchIndexSet = new LinkedHashSet<>();

        String cs = channelName.toLowerCase();
        int whiteSpaceIndex = channelName.indexOf(" ");
        String cs1 = null;
        if (whiteSpaceIndex > 0) {
            cs1 = channelName.toLowerCase().substring(0, whiteSpaceIndex);
        }

        int numberMatches = 0;

        if (regExMatch) {
            for (int i = 0; i < programTitleList.size(); i++) {
                String ps = programTitleList.get(i).toLowerCase();
                if (ps.matches(cs + "\\b.*")) {
                    topChannelNameToProgramTitleMatchIndexSet.add(i);
                    numberMatches++;
                    if(numberMatches==ntop) break;
                }
            }
            if (numberMatches<ntop && cs1 != null && topChannelNameToProgramTitleMatchIndexSet.size() == 0) {
                for (int i = 0; i < programTitleList.size(); i++) {
                    String ps = programTitleList.get(i).toLowerCase();
                    if (ps.matches(cs1 + "\\b.*")) {
                        topChannelNameToProgramTitleMatchIndexSet.add(i);
                        numberMatches++;
                        if(numberMatches==ntop) break;
                    }
                }
            }
        }

        if (numberMatches<ntop) {
            List<BoundExtractedResult> matches = FuzzySearch.extractTop(channelName, programTitleList, toStringFunction, tokenSet, ntop);
            for (BoundExtractedResult match : matches) {
                int index = match.getIndex();
                if (index >= 0) {
                    topChannelNameToProgramTitleMatchIndexSet.add(index);
                    numberMatches++;
                    if(numberMatches==ntop) break;
                }
            }
        }
        return topChannelNameToProgramTitleMatchIndexSet;
    }

    public static int matchOne(String channelName, List<String> programTitleList, boolean regExMatch) {
        int index = -1;
        Set<Integer> topChannelNameToProgramTitleMatchIndexSet = matchTop(channelName, programTitleList, 1, regExMatch);
        Iterator iter = topChannelNameToProgramTitleMatchIndexSet.iterator();
        if(iter.hasNext()) index = (int) iter.next();
        return index;
    }


    static class NormalizeString implements ToStringFunction<String> {
        public String apply(String s) {
            return s.replace("_", "").toLowerCase();
        }
    }


    static List<BoundExtractedResult> mergeResults(List<BoundExtractedResult> matchList1, List<BoundExtractedResult> matchList2, int size) {
        List<BoundExtractedResult> mergedList = new ArrayList<>(matchList1);
        mergedList.addAll(matchList2);
        Collections.sort(mergedList, Collections.reverseOrder());
        HashSet<Integer> indexSet = new HashSet<>();
        Iterator<BoundExtractedResult> it = mergedList.iterator();
        while (it.hasNext()) {
            BoundExtractedResult match = it.next();
            int index = match.getIndex();
            if (indexSet.contains(index)) {
                it.remove();
            } else {
                indexSet.add(index);
            }
        }
        return mergedList.subList(0, min(mergedList.size(), size));
    }

}

