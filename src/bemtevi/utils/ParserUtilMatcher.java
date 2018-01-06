package bemtevi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtilMatcher {
	private List<Matcher> matchers = new ArrayList<Matcher>();
	private List<String> groups = new ArrayList<String>();
	private int pos = 0;
	private String documentText;
	
	public ParserUtilMatcher(List<Pattern> patterns, String documentText) {
		this.documentText = documentText;
		for (Pattern pattern : patterns) {
			matchers.add(pattern.matcher(documentText));
		}
	}
	
	public boolean find() {
		return find(false);
	}
	
	public boolean find(boolean debug) {
		if (debug) {
			System.out.println(documentText.replace("\n", "\\n").replace("\r", "\\r"));
		}
		for (Matcher matcher : matchers) {
			System.out.println("PATTERN: " + matcher.pattern());
			if (!matcher.find()) {
				if (debug) {
					System.out.println("FAILED PATTERN: " + matcher.pattern());
					int i1 = indexOfLastMatch(matcher, documentText);
					int i0 = Math.max(i1-30, 0);
					int i2 = Math.min(i1+30, documentText.length());
					String prefix = "";
					if (i1-i0 > 0) {
						prefix = String.format("%" + (i1-i0) + "s", " ");
					}
					System.out.println(documentText.substring(i0, i2).replace("\n", "\\n").replace("\r", "\\r"));
					System.out.println(prefix + "^");
				}
				return false;
			}
			for (int i=1; i<=matcher.groupCount(); i++) {
				groups.add(matcher.group(i));
			}
		}
		pos = 0;
		return true;
	}

	public String group(int i) {
		return groups.get(i);
	}
	
	public String nextGroup() {
		return group(pos++);
	}

    private static int indexOfLastMatch(Matcher matcher, String input) {
        for (int i = input.length(); i > 0; --i) {
            Matcher region = matcher.region(0, i);
            if (region.matches() || region.hitEnd()) {
                return i;
            }
        }
        return 0;
    }
}
