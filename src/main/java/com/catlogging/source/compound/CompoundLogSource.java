package com.catlogging.source.compound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.catlogging.config.BeanPostConstructor;
import com.catlogging.config.ConfigException;
import com.catlogging.config.PostConstructed;
import com.catlogging.model.Log;
import com.catlogging.model.Log.SizeMetric;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.model.Navigation.NavigationType;
import com.catlogging.model.support.BaseLogsSource;
import com.catlogging.model.support.DefaultLog;
import com.catlogging.reader.filter.FilteredLogEntryReader;
import com.catlogging.reader.filter.FilteredLogEntryReader.FilteredLogEntryReaderWithNotConfigurableTarget;
import com.catlogging.source.compound.CompoundLogSource.ComposedLogSourceProducer;
import com.catlogging.model.json.Views;
import com.catlogging.validators.NotDefaultPrimitiveValue;

/**
 * Composes multiple logs into one ordered by the timestamp field.
 * 
 * @author Tester
 *
 */
@Slf4j
@PostConstructed(constructor = ComposedLogSourceProducer.class)
public class CompoundLogSource extends BaseLogsSource<CompoundLogAccess> {

	/**
	 * Passes the {@link LogSourceProvider} dependency to the source.
	 * 
	 * @author Tester
	 *
	 */
	@Component
	public static class ComposedLogSourceProducer implements BeanPostConstructor<CompoundLogSource> {
		@Autowired
		private LogSourceProvider logSourceProvider;

//		public void postConstruct(final CompoundLogSource bean, final BeanConfigFactoryManager configManager)
		@Override
		public void postConstruct(final CompoundLogSource bean) throws ConfigException {
			bean.logSourceProvider = logSourceProvider;
		}

	}

	/**
	 * Bean used for storing log part references.
	 * 
	 * @author Tester
	 *
	 */
	public static class LogPartBean {
		@NotDefaultPrimitiveValue
		private long sourceId;

		@NotNull
		private String logPath;

		/**
		 * @return the sourceId
		 */
		public long getSourceId() {
			return sourceId;
		}

		/**
		 * @param sourceId
		 *            the sourceId to set
		 */
		public void setSourceId(final long sourceId) {
			this.sourceId = sourceId;
		}

		/**
		 * @return the logPath
		 */
		public String getLogPath() {
			return logPath;
		}

		/**
		 * @param logPath
		 *            the logPath to set
		 */
		public void setLogPath(final String logPath) {
			this.logPath = logPath;
		}

		@Override
		public String toString() {
			return "LogPartBean [sourceId=" + sourceId + ", logPath=" + logPath + "]";
		}

	}

	private LogSourceProvider logSourceProvider;

	@JsonProperty
	@Size(min = 1)
	private List<LogPartBean> parts;

	private List<LogInstance> instances;

	private static ThreadLocal<Set<Long>> cycleDetector = new ThreadLocal<>();

	public CompoundLogSource() {
		super();
		readerConfigurable = false;
	}

	protected List<LogInstance> getPartInstances() {
		if (cycleDetector.get() == null) {
			cycleDetector.set(new HashSet<Long>());
		}
		cycleDetector.get().add(getId());
		try {
			if (instances == null) {
				instances = new ArrayList<>();
				if (parts != null) {
					final Map<Long, LogSource<LogRawAccess<? extends LogInputStream>>> logSources = new HashMap<>();
					for (final LogPartBean part : parts) {
						if (cycleDetector.get().contains(Long.valueOf(part.getSourceId()))) {
							log.warn(
									"Part log source with id {} would build a cycle, it's excluded in composition for log: {}",
									part.sourceId, this);
							continue;
						}
						LogSource<LogRawAccess<? extends LogInputStream>> source = logSources.get(part.sourceId);
						if (source == null) {
							source = logSourceProvider.getSourceById(part.sourceId);
							if (source == null) {
								log.warn(
										"Part log source with id {} not found, it will be excluded in composition for log: {}",
										part.sourceId, getId());
								continue;
							}
							logSources.put(part.sourceId, source);
						}
						try {
							if (StringUtils.isNotBlank(part.getLogPath())) {
								final Log logg = source.getLog(part.getLogPath());
								if (logg != null) {
									instances.add(new LogInstance(part.sourceId, logg, source));
								} else {
									log.warn(
											"Part log {} in source {} not found, it will be excluded in composition for log: {}",
											part.sourceId, part.getLogPath(), getId());

								}
							} else {
								// Add all
								for (final Log log : source.getLogs()) {
									instances.add(new LogInstance(part.sourceId, log, source));
								}
							}

						} catch (final IOException e) {
							log.warn("Failed to load part log " + part.logPath + " in source" + part.sourceId
									+ ", it will be excluded", e);
						}
					}
				}
				log.debug("Resolved for compound source {} the following log parts: {}", this, instances);
			}
			return instances;
		} finally {
			cycleDetector.get().remove(getId());
			if (cycleDetector.get().isEmpty()) {
				cycleDetector.remove();
			}
		}
	}

	@Override
	public List<Log> getLogs() throws IOException {
		return Collections.singletonList(getLog(null));
	}

	@Override
	public Log getLog(final String path) throws IOException {
		long totalSize = 0;
		long latestModified = 0;
		for (final LogInstance li : getPartInstances()) {
			totalSize += li.getLog().getSize();
			if (li.getLog().getLastModified() > latestModified) {
				latestModified = li.getLog().getLastModified();
			}
		}
		return new DefaultLog(getName(), getName(), latestModified, SizeMetric.BYTE, totalSize);
	}

	@Override
	public CompoundLogAccess getLogAccess(final Log log) throws IOException {
		return new CompoundLogAccess(log, getPartInstances());
	}

	/**
	 * @return the parts
	 */
	public List<LogPartBean> getParts() {
		return parts;
	}

	/**
	 * @param parts
	 *            the parts to set
	 */
	public void setParts(final List<LogPartBean> parts) {
		this.parts = parts;
	}

	@Override
	@JsonView(Views.Info.class)
	@JsonSerialize(as = FilteredLogEntryReaderWithNotConfigurableTarget.class)
	@JsonDeserialize(as = FilteredLogEntryReaderWithNotConfigurableTarget.class)
	public FilteredLogEntryReader<CompoundLogAccess> getReader() {
		final FilteredLogEntryReader<CompoundLogAccess> reader = super.getReader();
		if (!(reader.getTargetReader() instanceof CompoundLogReader)) {
			reader.setTargetReader(new CompoundLogReader(getPartInstances()));
		}
		return reader;
	}

	@Override
	public NavigationType getNavigationType() {
		return NavigationType.DATE;
	}

	@Override
	public String toString() {
		return "CompoundLogSource [" + super.toString() + ", parts=" + parts + "]";
	}

}
