package bemtevi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtilPattern {
	public static final int SIMPLE_SEARCH = 0;
	public static final int MULTILINE_SEARCH = 1;
	
	public static final int FILL_SPACES = 0x0001;
	public static final int FULL_LINE = 0x0002;
	public static final int AFTER_SPACE = 0x0004;
	public static final int AFTER_ANY = 0x0008;
	
	private int searchFlags;
	private List<Search> searches = new ArrayList<Search>();
	private boolean dirty = false;
	private List<Pattern> patterns = new ArrayList<Pattern>();
	private boolean optional = false;
	
	private class Search {
		String pattern;
		int type;
		private int options;
		
		Search(String pattern, int type, int options) {
			this.options = options;
			if ((options & FILL_SPACES) != 0) {
				pattern = pattern.replaceAll("(.)", "$1\\\\s*");
			} 
			if ((options & FULL_LINE) != 0) {
				pattern += "\\s*$";
			}			
			if ((options & AFTER_SPACE) != 0) {
				pattern = "\\s*" + pattern;
			}
			if ((options & AFTER_ANY) != 0) {
				pattern = ".*?" + pattern;
			}
			this.pattern = pattern;
			this.type = type;
		}
	}

	public ParserUtilPattern(int searchFlags) {
		this.searchFlags = searchFlags;
	}

	public void search(String pattern) {
		search(pattern, 0);
	}
	public void search(String pattern, int options) {
		search(new Search(pattern, 0, options | AFTER_SPACE));
	}
	
	public void searchNext(String pattern) {
		searchNext(pattern, 0);
	}
	public void searchNext(String pattern, int options) {
		search(new Search(pattern, 0, options | AFTER_ANY));
	}
	
	public void searchFirst(String pattern) {
		searchFirst(pattern, 0);
	}
	public void searchFirst(String pattern, int options) {
		search(new Search(pattern, 1, options));
	}
	
	public void setOptional(boolean optional) {
		this.optional = optional;
	}	
	
	private void search(Search search) {
		searches.add(search);
		dirty = true;
	}

	private void compile() {
		patterns.clear();
		String regex = "";
		int patternFlags = 0;
		if (searchFlags == SIMPLE_SEARCH) {
			patternFlags = Pattern.DOTALL;
		} else if (searchFlags == MULTILINE_SEARCH) {
			patternFlags = Pattern.DOTALL | Pattern.MULTILINE;
		}
		for (Search search : searches) {
			switch (search.type) {
				case 1:
					addPattern(regex, patternFlags);
					regex = "";
					break;
			} 
			regex += search.pattern;
		}
		addPattern(regex, patternFlags);
	}
	
	private void addPattern(String regex, int patternFlags) {
		if (regex.length() > 0) {
			if (optional) {
				regex = "(?:" + regex + ")?";
			}
			/*regex = regex.replaceAll("[àáâã]", "\\[aàáâã\\]");
			regex = regex.replaceAll("[ÀÁÂÃ]", "\\[AÀÁÂÃ\\]");
			regex = regex.replaceAll("[èéêẽ]", "\\[eèéêẽ\\]");
			regex = regex.replaceAll("[ÈÉÊẼ]", "\\[EÈÉÊẼ\\]");*/
			Pattern pattern = Pattern.compile(regex, patternFlags);
			patterns.add(pattern);
		}
	}

	public ParserUtilMatcher matcher(String documentText) {
		if (dirty) {
			compile();
		}		
		if (searchFlags == SIMPLE_SEARCH) {
			documentText = ParserUtil.joinLines(documentText);
		}
		ParserUtilMatcher matcher = new ParserUtilMatcher(patterns, documentText);
		return matcher;
	}

}
