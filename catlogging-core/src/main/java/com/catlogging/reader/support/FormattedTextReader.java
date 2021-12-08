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

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.LogEntry;
import com.catlogging.reader.FormatException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reader based on patterns.
 * 
 * @author Tester
 * 
 */
@Slf4j
public abstract class FormattedTextReader extends AbstractPatternLineReader<Matcher> {
	public abstract static class Specifier implements Cloneable {
		private int minWidth;
		private int maxWidth;
		private String modifier;
		private final String specifierKey;
		private String fieldName;

		public Specifier(final String specifierKey) {
			super();
			this.specifierKey = specifierKey;
		}

		/**
		 * @return the minWidth
		 */
		public int getMinWidth() {
			return minWidth;
		}

		/**
		 * @param minWidth
		 *            the minWidth to set
		 */
		public void setMinWidth(final int minWidth) {
			this.minWidth = minWidth;
		}

		/**
		 * @return the maxWidth
		 */
		public int getMaxWidth() {
			return maxWidth;
		}

		/**
		 * @param maxWidth
		 *            the maxWidth to set
		 */
		public void setMaxWidth(final int maxWidth) {
			this.maxWidth = maxWidth;
		}

		/**
		 * @return the modifier
		 */
		public String getModifier() {
			return modifier;
		}

		/**
		 * @param modifier
		 *            the modifier to set
		 */
		public void setModifier(final String modifier) {
			this.modifier = modifier;
		}

		protected abstract String getRegex() throws FormatException;

		protected abstract void set(LogEntry entry, String match) throws FormatException;

		protected abstract FieldBaseTypes getFieldType();

		public final String getSpecifierKey() {
			return specifierKey;
		}

		/**
		 * @return the attributeName
		 */
		public String getFieldName() {
			return fieldName != null ? fieldName : specifierKey;
		}

		/**
		 * Adapts the lengthPattern by length modifiers and returns it. If
		 * length modifiers aren't specified the withoutLengthPattern is
		 * returned if given otherwise the unchanged lengthPattern.
		 * 
		 * @param lengthPattern
		 * @param withoutLengthPattern
		 * @return
		 */
		protected String adaptRegexByLength(final String lengthPattern, final String withoutLengthPattern) {
			if (maxWidth > 0 && maxWidth == minWidth) {
				return lengthPattern + "{" + maxWidth + "}";
			} else if (maxWidth > 0) {
				return lengthPattern + "{" + Math.max(0, minWidth) + "," + maxWidth + "}";
			} else if (minWidth > 0) {
				return lengthPattern + "{" + minWidth + ",}";
			}
			return withoutLengthPattern != null ? withoutLengthPattern : lengthPattern;
		}

		@Override
		public Specifier clone() throws CloneNotSupportedException {
			return (Specifier) super.clone();
		}
	}

	/**
	 * Specifier for arbitrary unknown specifiers.
	 * 
	 * @author Tester
	 * 
	 */
	public static class ArbitraryTextSpecifier extends Specifier {
		private final boolean greedy;

		public ArbitraryTextSpecifier(final String specifierKey, final boolean greedy) {
			super(specifierKey);
			this.greedy = greedy;
		}

		@Override
		protected String getRegex() {
			return adaptRegexByLength(".", ".*") + (greedy ? "" : "?");
		}

		@Override
		protected void set(final LogEntry entry, final String match) throws FormatException {
			entry.put(getFieldName(), match);
		}

		@Override
		protected FieldBaseTypes getFieldType() {
			return FieldBaseTypes.STRING;
		}
	}

	/**
	 * Specifier to ignore.
	 * 
	 * @author Tester
	 * 
	 */
	public static final class IgnoreSpecifier extends Specifier {
		public IgnoreSpecifier(final String specifierKey) {
			super(specifierKey);
		}

		@Override
		protected String getRegex() {
			return null;
		}

		@Override
		protected void set(final LogEntry entry, final String match) throws FormatException {
			// NOP
		}

		@Override
		protected FieldBaseTypes getFieldType() {
			return null;
		}
	}

	private static final Pattern SPECIFIER_PATTERN = Pattern
			.compile("%(-?(\\d+))?(\\.(\\d+))?([a-zA-Z])(\\{([^\\}]+)\\})?");

	@JsonProperty
	@NotNull
	private String formatPattern;

	@JsonProperty
	private Map<String, String> specifiersFieldMapping = new HashMap<String, String>();

	protected Specifier[] parsingSpecifiers;
	protected Pattern parsingPattern;

	/**
	 * 
	 * @return the supported specifiers
	 */
	protected abstract Specifier[] createSupportedSpecifiers();

	/**
	 * @param formatPattern
	 *            the formatPattern to set
	 */
	@Override
	protected void init() throws FormatException {
		super.init();
		if (parsingPattern == null) {
			// Only if not yet parsed
			if (formatPattern != null) {
				final ArrayList<Specifier> specs = new ArrayList<Specifier>();
				final StringBuilder parsingPatternStr = new StringBuilder();
				final Matcher m = SPECIFIER_PATTERN.matcher(formatPattern);
				int leftPos = 0;
				while (m.find()) {
					if (m.start() > leftPos) {
						parsingPatternStr.append(Pattern.quote(formatPattern.substring(leftPos, m.start())));
					}
					leftPos = m.end();
					final String minWidthStr = m.group(2);
					final String maxWidthStr = m.group(4);
					final String specName = m.group(5);
					final String specModifier = m.group(7);
					int minWidth = -1;
					if (minWidthStr != null && minWidthStr.length() > 0) {
						minWidth = Integer.parseInt(minWidthStr);
					}
					int maxWidth = -1;
					if (maxWidthStr != null && maxWidthStr.length() > 0) {
						maxWidth = Integer.parseInt(maxWidthStr);
					}
					Specifier spec = null;
					for (final Specifier specTest : createSupportedSpecifiers()) {
						if (specTest.getSpecifierKey().equals(specName)) {
							spec = specTest;
							break;
						}
					}
					if (spec == null) {
						log.debug(
								"Format specifier {} in pattern '{}' is unknown and will be parsed as simple text pattern",
								specName, formatPattern);
						spec = new ArbitraryTextSpecifier(specName, false);
					} else if (spec instanceof IgnoreSpecifier) {
						log.debug("Format specifier '{}' in pattern '{}' is ignored", specName, formatPattern);
						continue;
					}
					spec.setMaxWidth(maxWidth);
					spec.setMinWidth(minWidth);
					spec.setModifier(specModifier);
					parsingPatternStr.append("(");
					parsingPatternStr.append(spec.getRegex());
					parsingPatternStr.append(")");
					spec.fieldName = specifiersFieldMapping.get(specName);
					if (StringUtils.isBlank(spec.fieldName)) {
						spec.fieldName = specName;
					}
					specs.add(spec);
				}
				parsingPatternStr.append(Pattern.quote(formatPattern.substring(leftPos)));
				parsingPattern = Pattern.compile(parsingPatternStr.toString());
				parsingSpecifiers = specs.toArray(new Specifier[specs.size()]);
				log.debug("Prepared parsing pattern '{}' for log4j conversion pattern: {}", parsingPattern,
						formatPattern);
			} else {
				parsingSpecifiers = null;
				parsingPattern = null;
			}
		}
	}

	@Override
	protected ReadingContext<Matcher> getReadingContext() throws FormatException {
		// Cloning specifiers for thread-safety
		final Specifier[] localParsingSpecifiers = new Specifier[parsingSpecifiers.length];
		for (int i = 0; i < parsingSpecifiers.length; i++) {
			try {
				localParsingSpecifiers[i] = parsingSpecifiers[i].clone();
			} catch (final CloneNotSupportedException e) {
				throw new FormatException("Failed to clone specifier: " + parsingSpecifiers[i], e);
			}
		}
		return new ReadingContext<Matcher>() {
			@Override
			public Matcher matches(final String line) {
				final Matcher m = parsingPattern.matcher(line);
				return m.matches() ? m : null;
			}

			@Override
			public void fillAttributes(final LogEntry entry, final Matcher ctx) throws FormatException {
				int specNumber = 1;
				for (final Specifier spec : localParsingSpecifiers) {
					final String match = ctx.group(specNumber++);
					spec.set(entry, match);
				}
			}
		};
	}

	/**
	 * @return the formatPattern
	 */
	public String getFormatPattern() {
		return formatPattern;
	}

	/**
	 * @param formatPattern
	 *            the formatPattern to set
	 */
	public void setFormatPattern(final String formatPattern) {
		this.formatPattern = formatPattern;
		this.parsingPattern = null;
	}

	@Override
	protected String getPatternInfo() {
		return formatPattern;
	}

	/**
	 * @return the specifiersFieldMapping
	 */
	public Map<String, String> getSpecifiersFieldMapping() {
		return specifiersFieldMapping;
	}

	/**
	 * @param specifiersFieldMapping
	 *            the specifiersFieldMapping to set
	 */
	public void setSpecifiersFieldMapping(final Map<String, String> specifiersFieldMapping) {
		this.specifiersFieldMapping = specifiersFieldMapping;
		this.parsingPattern = null;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		init();
		final LinkedHashMap<String, FieldBaseTypes> fields = super.getFieldTypes();
		if (parsingSpecifiers != null) {
			for (final Specifier s : parsingSpecifiers) {
				fields.put(s.getFieldName(), s.getFieldType());
			}
		}
		return fields;
	}

}
