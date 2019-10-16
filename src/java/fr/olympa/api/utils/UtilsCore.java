package fr.olympa.api.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class UtilsCore {
	public static Collection<String> similarWords(String word, Set<String> allWords) {
		Map<Double, String> similarWordList = new TreeMap<>();
		StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
		for (String currentWord : allWords) {
			double score = service.score(word, currentWord);
			if (score > 0.80) {
				similarWordList.put(score, currentWord);
			}
		}
		return similarWordList.values();
	}

}
