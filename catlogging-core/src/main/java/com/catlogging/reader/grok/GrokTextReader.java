/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 
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
package com.catlogging.reader.grok;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.config.PostConstructed;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.LogEntry;
import com.catlogging.model.SeverityLevel;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.support.AbstractPatternLineReader;
import com.catlogging.reader.support.AbstractPatternLineReader.ReadingContext;
import com.catlogging.util.grok.GrokConsumerConstructor;
import com.catlogging.util.grok.GrokConsumerConstructor.GrokConsumer;
import com.catlogging.util.grok.GrokMatcher;
import com.catlogging.util.grok.GrokPatternBean;
import com.catlogging.util.grok.GrokPatternBeanJsonModel;
import com.catlogging.util.grok.GroksRegistry;

/**
 * Grok text reader.
 * 
 * @author Tester
 * 
 */
@PostConstructed(constructor = GrokConsumerConstructor.class)
public class GrokTextReader extends AbstractPatternLineReader<GrokMatcher>
		implements GrokConsumer, GrokPatternBeanJsonModel, ReadingContext<GrokMatcher> {

	private static final Logger logger = LoggerFactory.getLogger(GrokTextReader.class);

	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@NotNull
	@Valid
	private GrokPatternBean grokBean = new GrokPatternBean();

	@JsonProperty
	private String overflowAttribute;

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return Collections.emptyList();
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		init();
		final LinkedHashMap<String, FieldBaseTypes> fields = super.getFieldTypes();
		fields.putAll(grokBean.getGrok(groksRegistry).getFieldTypes());
		if (overflowAttribute != null && !fields.containsKey(overflowAttribute)) {
			fields.put(overflowAttribute, FieldBaseTypes.STRING);
		}
		return fields;
	}

	@Override
	protected void init() throws FormatException {
		super.init();
		logger.debug("Compiled grok: {}", grokBean.getGrok(groksRegistry));
	}

	@Override
	public GrokMatcher matches(final String line) throws FormatException {
		final GrokMatcher m = grokBean.getGrok(groksRegistry).matcher(line);
		return m.matches() ? m : null;
	}

	@Override
	public void fillAttributes(final LogEntry entry, final GrokMatcher ctx) throws FormatException {
		ctx.setMatchingGroupsToFields(entry, false);
	}

	@Override
	protected ReadingContext<GrokMatcher> getReadingContext() throws FormatException {
		return this;
	}

	@Override
	protected void attachOverflowLine(final LogEntry entry, final String overflowLine) {
		if (overflowAttribute != null) {
			final String oldMsg = (String) entry.get(overflowAttribute);
			if (oldMsg == null) {
				entry.put(overflowAttribute, overflowLine);
			} else {
				entry.put(overflowAttribute, oldMsg + "\n" + overflowLine);
			}
		}
	}

	/**
	 * @return the grokPattern
	 */
	@Deprecated
	public String getGrokPattern() {
		return grokBean.getPattern();
	}

	/**
	 * @param grokPattern
	 *            the grokPattern to set
	 */
	@Deprecated
	public void setGrokPattern(final String grokPattern) {
		grokBean.setPattern(grokPattern);
	}

	/**
	 * @return the overflowAttribute
	 */
	public String getOverflowAttribute() {
		return overflowAttribute;
	}

	/**
	 * @param overflowAttribute
	 *            the overflowAttribute to set
	 */
	public void setOverflowAttribute(final String overflowAttribute) {
		this.overflowAttribute = StringUtils.isNotEmpty(overflowAttribute) ? overflowAttribute.trim() : null;
	}

	@Override
	protected String getPatternInfo() {
		return grokBean.getPattern();
	}

	/**
	 * @return the grokBean
	 */
	public GrokPatternBean getGrokBean() {
		return grokBean;
	}

	/**
	 * @param grokBean
	 *            the grokBean to set
	 */
	public void setGrokBean(final GrokPatternBean grokBean) {
		this.grokBean = grokBean;
	}

	@Override
	public void initGrokFactory(final GroksRegistry groksRegistry) {
		this.groksRegistry = groksRegistry;
	}

	@Override
	@JsonIgnore
	@Deprecated
	public String getPattern() {
		return grokBean.getPattern();
	}

	@Override
	public void setPattern(final String pattern) {
		grokBean.setPattern(pattern);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isMultiLine() {
		return grokBean.isMultiLine();
	}

	@Override
	public void setMultiLine(final boolean multiLine) {
		grokBean.setMultiLine(multiLine);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isDotAll() {
		return grokBean.isDotAll();
	}

	@Override
	public void setDotAll(final boolean dotAll) {
		grokBean.setDotAll(dotAll);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isCaseInsensitive() {
		return grokBean.isCaseInsensitive();
	}

	@Override
	public void setCaseInsensitive(final boolean caseInsensitive) {
		grokBean.setCaseInsensitive(caseInsensitive);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isSubStringSearch() {
		return grokBean.isSubStringSearch();
	}

	@Override
	public void setSubStringSearch(final boolean subStringSearch) {
		grokBean.setSubStringSearch(subStringSearch);
	}

}
