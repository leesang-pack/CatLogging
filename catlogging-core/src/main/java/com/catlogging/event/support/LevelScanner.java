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
package com.catlogging.event.support;

import java.util.LinkedHashMap;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.event.Event;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.LogEntry;
import com.catlogging.model.SeverityLevel;
import com.catlogging.reader.FormatException;

/**
 * Scanns for log entries matching the level of interest.
 * 
 * @author Tester
 * 
 */
public class LevelScanner extends SingleEntryIncrementalMatcher {

	/**
	 * Level comparator type.
	 * 
	 * @author Tester
	 * 
	 */
	public enum LevelComparatorType {
		EQ, EQ_OR_GREATER, GREATER, EQ_OR_LESS, LESS, NEQ;

		public boolean matches(final int severityOrdinalNumber, final SeverityLevel check) {
			switch (this) {
			case EQ:
				return severityOrdinalNumber == check.getOrdinalNumber();
			case GREATER:
				return check.getOrdinalNumber() > severityOrdinalNumber;
			case EQ_OR_GREATER:
				return check.getOrdinalNumber() >= severityOrdinalNumber;
			case EQ_OR_LESS:
				return check.getOrdinalNumber() <= severityOrdinalNumber;
			case LESS:
				return check.getOrdinalNumber() < severityOrdinalNumber;
			case NEQ:
				return check.getOrdinalNumber() != severityOrdinalNumber;
			}
			return false;
		}
	}

	@JsonProperty
	@JsonInclude(Include.NON_DEFAULT)
	private int severityNumber;

	@NotNull
	@JsonProperty
	private LevelComparatorType comparator = LevelComparatorType.EQ_OR_LESS;

	@Override
	public Event matches(final LogEntry entry) {
		if (entry.getSeverity() != null && comparator.matches(severityNumber, entry.getSeverity())) {
			final Event event = new Event();
			return event;
		}
		return null;
	}

	/**
	 * @return the comparator
	 */
	public LevelComparatorType getComparator() {
		return comparator;
	}

	/**
	 * @param comparator
	 *            the comparator to set
	 */
	public void setComparator(final LevelComparatorType comparator) {
		this.comparator = comparator;
	}

	/**
	 * @return the severityNumber
	 */
	public int getSeverityNumber() {
		return severityNumber;
	}

	/**
	 * @param severityNumber
	 *            the severityNumber to set
	 */
	public void setSeverityNumber(final int severityNumber) {
		this.severityNumber = severityNumber;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return new LinkedHashMap<>();
	}

}
