/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.catlogging.reader.support;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to parse date strings based on {@link SimpleDateFormat} patterns. The
 * implementation was inspired by <a href=
 * "http://sourceforge.net/p/logsaw/code/ci/master/tree/net.sf.logsaw.dialect.pattern/src/net/sf/logsaw/dialect/pattern/RegexUtils.java"
 * >LogSaw's implementation</a>.
 * 
 * @author Tester
 * 
 */
public class DateFormatUtils {
	private static Pattern simpleQuotesPattern = Pattern.compile("'([^']+)'");

	private static Pattern doubleQuotesPattern = Pattern.compile("''");

	/**
	 * Pattern context.
	 * 
	 * @author Tester
	 * 
	 */
	private static class PatternContext {
		private final StringBuffer pattern;

		private final ArrayList<String> replacedParts = new ArrayList<String>();

		private PatternContext(final String pattern) {
			this.pattern = new StringBuffer(pattern);
			unquote();
		}

		private void replace(final Pattern searchPattern, final String[]... alternatives) {
			final Matcher matcher = searchPattern.matcher(pattern.toString());
			if (matcher.matches()) {
				final StringBuilder rplStr = new StringBuilder("(");
				for (final String[] pool : alternatives) {
					for (final String s : pool) {
						if (rplStr.length() > 1) {
							rplStr.append("|");
						}
						rplStr.append(Pattern.quote(s));
					}
				}
				rplStr.append(")");
				int dif = 0;
				matcher.reset();
				while (matcher.find()) {
					dif += -(pattern.length() - pattern
							.replace(matcher.start() + dif, matcher.end() + dif, "@" + replacedParts.size() + "@")
							.length());
					replacedParts.add(rplStr.toString());
				}
			}
		}

		private void replace(final Pattern searchPattern, final String rplStr) {
			final Matcher matcher = searchPattern.matcher(pattern.toString());
			int dif = 0;
			while (matcher.find()) {
				dif += -(pattern.length()
						- pattern.replace(matcher.start() + dif, matcher.end() + dif, "@" + replacedParts.size() + "@")
								.length());
				replacedParts.add(rplStr);
			}
		}

		private void unquote() {
			// Replace single quotes with nested content, which is quoted by
			// Pattern.quote(...)
			final Matcher simpleQuotesMatcher = simpleQuotesPattern.matcher(pattern.toString());
			int dif = 0;
			while (simpleQuotesMatcher.find()) {
				final String quotedContent = Pattern.quote(simpleQuotesMatcher.group(1));
				// Quote content
				final int contentDif = -(pattern.length() - pattern.replace(simpleQuotesMatcher.start(1) - dif,
						simpleQuotesMatcher.end(1) - dif, "@" + replacedParts.size() + "@").length());
				replacedParts.add(quotedContent);
				// Remove quotes
				pattern.deleteCharAt(simpleQuotesMatcher.end() - 1 - dif + contentDif);
				pattern.deleteCharAt(simpleQuotesMatcher.start() - dif);
				dif += 2 - contentDif;
			}

			// Replace double quotes by a single quote
			final Matcher doubleQuotesMatcher = doubleQuotesPattern.matcher(pattern.toString());
			dif = 0;
			while (doubleQuotesMatcher.find()) {
				pattern.deleteCharAt(doubleQuotesMatcher.start() - dif);
				dif++;
			}
		}

		@Override
		public String toString() {
			final StringBuffer txt = new StringBuffer(pattern);
			for (int i = 0; i < replacedParts.size(); i++) {
				final String placeholder = "@" + i + "@";
				final int pos = txt.indexOf(placeholder);
				txt.replace(pos, pos + placeholder.length(), replacedParts.get(i));
			}
			return txt.toString();
		}

	}

	public static String getRegexPattern(final SimpleDateFormat dateFormat) {
		final PatternContext pattern = new PatternContext(dateFormat.toPattern());

		// G Era designator
		pattern.replace(Pattern.compile("G+"), dateFormat.getDateFormatSymbols().getEras());

		// y Year
		pattern.replace(Pattern.compile("[y]{3,}"), "\\d{4}");
		pattern.replace(Pattern.compile("[y]{2}"), "\\d{2}");
		pattern.replace(Pattern.compile("y"), "\\d{4}");

		// M Month in year
		pattern.replace(Pattern.compile("[M]{3,}"), dateFormat.getDateFormatSymbols().getMonths(),
				dateFormat.getDateFormatSymbols().getShortMonths());
		pattern.replace(Pattern.compile("[M]{2}"), "\\d{2}");
		pattern.replace(Pattern.compile("M"), "\\d{1,2}");

		// w Week in year
		pattern.replace(Pattern.compile("w+"), "\\d{1,2}");

		// W Week in month
		pattern.replace(Pattern.compile("W+"), "\\d");

		// D Day in year
		pattern.replace(Pattern.compile("D+"), "\\d{1,3}");

		// d Day in month
		pattern.replace(Pattern.compile("d+"), "\\d{1,2}");

		// F Day of week in month
		pattern.replace(Pattern.compile("F+"), "\\d");

		// E Day in week
		pattern.replace(Pattern.compile("E+"), dateFormat.getDateFormatSymbols().getWeekdays(),
				dateFormat.getDateFormatSymbols().getShortWeekdays());

		// a Am/pm marker
		pattern.replace(Pattern.compile("a+"), dateFormat.getDateFormatSymbols().getAmPmStrings());

		// H Hour in day (0-23)
		pattern.replace(Pattern.compile("H+"), "\\d{1,2}");

		// k Hour in day (1-24)
		pattern.replace(Pattern.compile("k+"), "\\d{1,2}");

		// K Hour in am/pm (0-11)
		pattern.replace(Pattern.compile("K+"), "\\d{1,2}");

		// h Hour in am/pm (1-12)
		pattern.replace(Pattern.compile("h+"), "\\d{1,2}");

		// m Minute in hour
		pattern.replace(Pattern.compile("m+"), "\\d{1,2}");

		// s Second in minute
		pattern.replace(Pattern.compile("s+"), "\\d{1,2}");

		// S Millisecond
		pattern.replace(Pattern.compile("S+"), "\\d{1,3}");

		// z Time zone
		pattern.replace(Pattern.compile("z+"), "[a-zA-Z-+:0-9]*");

		// Z Time zone
		pattern.replace(Pattern.compile("Z+"), "[-+]\\d{4}");

		return pattern.toString();
	}

}
