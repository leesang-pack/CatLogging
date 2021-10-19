package com.catlogging.fields.filter.support;

import java.util.LinkedHashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.config.PostConstructed;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.fields.FieldsMap;
import com.catlogging.fields.filter.FieldsFilter;
import com.catlogging.reader.FormatException;
import com.catlogging.util.grok.Grok;
import com.catlogging.util.grok.GrokConsumerConstructor;
import com.catlogging.util.grok.GrokConsumerConstructor.GrokConsumer;
import com.catlogging.util.grok.GrokMatcher;
import com.catlogging.util.grok.GrokPatternBean;
import com.catlogging.util.grok.GroksRegistry;

/**
 * Extracts data from an input field using regex.
 * 
 * @author Tester
 *
 */
@PostConstructed(constructor = GrokConsumerConstructor.class)
public class RegexFilter implements FieldsFilter, GrokConsumer {
	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@NotNull
	@Valid
	private GrokPatternBean grokBean = new GrokPatternBean();

	@JsonProperty
	@NotEmpty
	private String sourceField;

	/**
	 * @return the sourceField
	 */
	public String getSourceField() {
		return sourceField;
	}

	/**
	 * @param sourceField
	 *            the sourceField to set
	 */
	public void setSourceField(final String sourceField) {
		this.sourceField = sourceField;
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
	public void filter(final FieldsMap fields) throws FormatException {
		final Object value = fields.get(sourceField);
		if (value != null) {
			final Grok grok = grokBean.getGrok(groksRegistry);
			final GrokMatcher matcher = grok.matcher(value.toString());
			if (matcher.matches()) {
				matcher.setMatchingGroupsToFields(fields, false);
			}
		}

	}

	@Override
	public void filterKnownFields(final LinkedHashMap<String, FieldBaseTypes> knownFields) throws FormatException {
		knownFields.putAll(grokBean.getGrok(groksRegistry).getFieldTypes());

	}

	@Override
	public void initGrokFactory(final GroksRegistry groksRegistry) {
		this.groksRegistry = groksRegistry;
	}

}
