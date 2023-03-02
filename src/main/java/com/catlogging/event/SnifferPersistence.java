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
package com.catlogging.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.catlogging.aspect.sql.QueryAdaptor;
import com.catlogging.model.sniffer.ScheduleInfo;
import com.catlogging.model.sniffer.Sniffer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.aspect.AspectHost;
import com.catlogging.aspect.AspectProvider;
import com.catlogging.model.Log;
import com.catlogging.model.LogSource;
import com.catlogging.util.ListQueryBuilder;
import com.catlogging.util.PageableResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;


/**
 * Persistence for observers.
 * 
 * @author Tester
 * 
 */
public interface SnifferPersistence {

	// Sniffer에서 정의되었기때문에 여긴 JPA 알림용도
	@Entity
	@DiscriminatorValue("AspectSniffer")
	@Getter
	@SuperBuilder
	@NoArgsConstructor
	class AspectSniffer extends Sniffer implements AspectHost {
		@JsonProperty
		@Transient	//jpa 무시
		private final HashMap<String, Object> aspects = new HashMap<String, Object>();

		@Override
		public <AspectType> void setAspect(final String key, final AspectType aspect) {
			aspects.put(key, aspect);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <AspectType> AspectType getAspect(final String key, final Class<AspectType> aspectType) {
			return (AspectType) aspects.get(key);
		}
	}

	interface SnifferListBuilder extends ListQueryBuilder<PageableResult<AspectSniffer>> {
		SnifferListBuilder withEventsCounter(AspectProvider<AspectSniffer, Integer> eventsCounter);

		SnifferListBuilder withScheduleInfo(QueryAdaptor<AspectSniffer, ScheduleInfo> adaptor);
	}

	/**
	 * Application event to be published when a sniffer is created or updated.
	 * 
	 * @author Tester
	 *
	 */
	class SnifferChangedEvent {
		private final Sniffer sniffer;

		public SnifferChangedEvent(final Sniffer sniffer) {
			super();
			this.sniffer = sniffer;
		}

		/**
		 * @return the sniffer
		 */
		public Sniffer getSniffer() {
			return sniffer;
		}

	}

	public SnifferListBuilder getSnifferListBuilder();

	public long createSniffer(Sniffer sniffer);

	public void updateSniffer(Sniffer sniffer);

	public Sniffer getSniffer(long id);

	public void deleteSniffer(Sniffer sniffer);

	public IncrementData getIncrementData(Sniffer sniffer, LogSource<?> source, Log log) throws IOException;

	public Map<Log, IncrementData> getIncrementDataByLog(Sniffer sniffer, LogSource<?> source) throws IOException;

	public void storeIncrementalData(Sniffer observer, LogSource<?> source, Log log, IncrementData data);

}
