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
package com.catlogging.util.grok;

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import com.catlogging.fields.FieldsMap;
import com.catlogging.util.grok.Grok.TypeConverter;

/**
 * Grok matcher with foreseen predicate support but without implementation.
 * 
 * @author Tester
 * 
 */
public final class GrokMatcher implements MatchResult {
	private final Matcher regexMatcher;
	private final Grok grok;

	protected GrokMatcher(final Grok grok, final Matcher regexMatcher) {
		this.grok = grok;
		this.regexMatcher = regexMatcher;
	}

	/**
	 * Attempts to match the entire region against the pattern including Grok
	 * predicates.
	 * 
	 * @return <tt>true</tt> if, and only if, the entire region sequence matches
	 *         this matcher's pattern and the Grok predicates
	 */
	public boolean matches() {
		return regexMatcher.matches();// && matchesPredicates();
	}

	/**
	 * Attempts to find the next subsequence of the input sequence that matches
	 * the pattern including Grok predicates.
	 * 
	 * @return <tt>true</tt> if, and only if, a subsequence of the input
	 *         sequence matches this matcher's pattern including the Grok
	 *         predicates.
	 */
	public boolean find() {
		return regexMatcher.find();
		// while (regexMatcher.find()) {
		// if (matchesPredicates()) {
		// return true;
		// }
		// }
		// return false;
	}

	// private boolean matchesPredicates() {
	// Map<Integer, GrokPredicate> predicates = grok.getGroupPredicates();
	// for (Integer g : predicates.keySet()) {
	// if (!predicates.get(g).matches(regexMatcher.group(g))) {
	// return false;
	// }
	// }
	// return true;
	// }

	public String group(final String groupName) {
		return regexMatcher.group(grok.getGroupNames().get(groupName));
	}

	public void setToField(final String groupName, final FieldsMap fields) {
		final int groupIndex = grok.getGroupNames().get(groupName);
		final String strValue = regexMatcher.group(groupIndex);
		if (strValue != null) {
			final TypeConverter<Object> typeConverter = grok.getTypeConverters().get(groupIndex);
			if (typeConverter != null) {
				final Object targetValue = typeConverter.convert(strValue);
				if (targetValue != null) {
					fields.put(groupName, targetValue);
				}
			} else {
				fields.put(groupName, strValue);
			}
		} else {
			fields.put(groupName, null);
		}
	}

	public void setMatchingGroupsToFields(final FieldsMap fields, final boolean setAlsoNullValues) {
		final Set<Entry<String, Integer>> entrySet = grok.getGroupNames().entrySet();
		for (final Entry<String, Integer> group : entrySet) {
			final int groupIndex = group.getValue();
			final String groupName = group.getKey();
			final String strValue = regexMatcher.group(groupIndex);
			if (strValue != null) {
				final TypeConverter<Object> typeConverter = grok.getTypeConverters().get(groupIndex);
				if (typeConverter != null) {
					final Object targetValue = typeConverter.convert(strValue);
					if (targetValue != null) {
						fields.put(groupName, targetValue);
					}
				} else {
					fields.put(groupName, strValue);
				}
			} else if (setAlsoNullValues) {
				fields.put(groupName, null);
			}
		}
	}

	@Override
	public int end() {
		return regexMatcher.end();
	}

	@Override
	public int end(final int arg0) {
		return regexMatcher.end(arg0);
	}

	@Override
	public String group() {
		return regexMatcher.group();
	}

	@Override
	public String group(final int arg0) {
		return regexMatcher.group(arg0);
	}

	@Override
	public int groupCount() {
		return regexMatcher.groupCount();
	}

	@Override
	public int start() {
		return regexMatcher.start();
	}

	@Override
	public int start(final int arg0) {
		return regexMatcher.start(arg0);
	}

}
