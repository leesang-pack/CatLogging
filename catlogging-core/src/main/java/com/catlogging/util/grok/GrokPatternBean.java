package com.catlogging.util.grok;

import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.reader.FormatException;

/**
 * Bean for configuring metadata belonging to a Grok pattern presentation.
 * 
 * @author Tester
 *
 */
public final class GrokPatternBean implements GrokPatternBeanJsonModel {
	@JsonProperty
	@NotEmpty
	@GrokPatternConstraint
	private String pattern;

	@JsonProperty
	private boolean subStringSearch = false;

	@JsonProperty
	private boolean multiLine = true;

	@JsonProperty
	private boolean dotAll = true;

	@JsonProperty
	private boolean caseInsensitive = true;

	@JsonIgnore
	private Grok grok;

	public Grok getGrok(final GroksRegistry registry) throws FormatException {
		if (grok == null) {
			try {
				grok = Grok.compile(registry, (subStringSearch ? ".*?" : "") + pattern + (subStringSearch ? ".*?" : ""),
						(multiLine ? Pattern.MULTILINE : 0) | (dotAll ? Pattern.DOTALL : 0)
								| (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0));
			} catch (final Exception e) {
				throw new FormatException("Failed to compile grok pattern: " + this + " -> " + e.getMessage(), e);
			}
		}
		return grok;
	}

	/**
	 * @return the pattern
	 */
	@Override
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	@Override
	public void setPattern(final String pattern) {
		this.pattern = pattern;
		this.grok = null;
	}

	/**
	 * @return the multiLine
	 */
	@Override
	public boolean isMultiLine() {
		return multiLine;
	}

	/**
	 * @param multiLine
	 *            the multiLine to set
	 */
	@Override
	public void setMultiLine(final boolean multiLine) {
		this.multiLine = multiLine;
		this.grok = null;
	}

	/**
	 * @return the dotAll
	 */
	@Override
	public boolean isDotAll() {
		return dotAll;
	}

	/**
	 * @param dotAll
	 *            the dotAll to set
	 */
	@Override
	public void setDotAll(final boolean dotAll) {
		this.dotAll = dotAll;
		this.grok = null;
	}

	/**
	 * @return the caseInsensitive
	 */
	@Override
	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	/**
	 * @param caseInsensitive
	 *            the caseInsensitive to set
	 */
	@Override
	public void setCaseInsensitive(final boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
		this.grok = null;
	}

	/**
	 * @return the subStringSearch
	 */
	@Override
	public boolean isSubStringSearch() {
		return subStringSearch;
	}

	/**
	 * @param subStringSearch
	 *            the subStringSearch to set
	 */
	@Override
	public void setSubStringSearch(final boolean subStringSearch) {
		this.subStringSearch = subStringSearch;
		this.grok = null;
	}

	@Override
	public String toString() {
		return "GrokPatternBean [pattern=" + pattern + ", subStringSearch=" + subStringSearch + ", multiLine="
				+ multiLine + ", dotAll=" + dotAll + ", caseInsensitive=" + caseInsensitive + ", grok=" + grok + "]";
	}

}
