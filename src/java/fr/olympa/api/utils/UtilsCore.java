package fr.olympa.api.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.md_5.bungee.api.chat.TextComponent;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class UtilsCore {

	public static Collection<String> similarWords(String word, Set<String> allWords) {
		Map<Double, String> similarWordList = new TreeMap<>();
		StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
		for (String currentWord : allWords) {
			double score = service.score(word, currentWord);
			if (score > 0.80)
				similarWordList.put(score, currentWord);
		}
		return similarWordList.values();
	}

	public static void toTextComponent(TextComponent msg, List<TextComponent> list, String separator, String end) {
		if (list.size() == 0)
			return;
		int i = 1;
		for (TextComponent targetAccountsText : list) {
			msg.addExtra(targetAccountsText);
			if (i == list.size())
				targetAccountsText.addExtra(end);
			else
				targetAccountsText.addExtra(separator);
			i++;
		}
	}
}
