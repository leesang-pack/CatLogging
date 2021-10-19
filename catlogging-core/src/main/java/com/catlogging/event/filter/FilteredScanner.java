package com.catlogging.event.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.config.ConfigException;
import com.catlogging.config.WrappedBean;
import com.catlogging.event.Event;
import com.catlogging.event.IncrementData;
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Scanner;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.fields.FieldsHost;
import com.catlogging.fields.filter.FieldsFilter;
import com.catlogging.model.Log;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;

public class FilteredScanner implements Scanner {
	@JsonProperty
	@Valid
	private List<FieldsFilter> filters = new ArrayList<>();

	@JsonProperty
	@Valid
	private Scanner targetScanner;

	public FilteredScanner() {
		super();
	}

	public FilteredScanner(final Scanner targetScanner, final FieldsFilter... filters) {
		super();
		this.targetScanner = targetScanner;
		for (final FieldsFilter f : filters) {
			this.filters.add(f);
		}
	}

	@Override
	public <R extends LogRawAccess<? extends LogInputStream>> void find(final LogEntryReader<R> reader,
			final LogEntryReaderStrategy readerStrategy, final Log log, final R logAccess,
			final IncrementData incrementData, final EventConsumer eventConsumer) throws IOException {
		targetScanner.find(reader, readerStrategy, log, logAccess, incrementData, new EventConsumer() {
			@Override
			public void consume(final Event eventData) throws IOException, FormatException {
				filter(eventData);
				eventConsumer.consume(eventData);
			}
		});

	}

	private void filter(final Event event) throws FormatException {
		for (final FieldsFilter f : filters) {
			f.filter(event);
		}
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return FieldsHost.FieldHostUtils.getFilteredFieldTypes(targetScanner, filters);
	}

	/**
	 * @return the targetScanner
	 */
	public Scanner getTargetScanner() {
		return targetScanner;
	}

	/**
	 * @param targetScanner
	 *            the targetScanner to set
	 */
	public void setTargetScanner(final Scanner targetScanner) {
		this.targetScanner = targetScanner;
	}

	/**
	 * @return the filters
	 */
	public List<FieldsFilter> getFilters() {
		return filters;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(final List<FieldsFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Wrapper for delegated filtered scanner e.g. to allow lazy initiation.
	 * 
	 * @author Tester
	 */
	public static abstract class FilteredScannerWrapper extends FilteredScanner
			implements WrappedBean<FilteredScanner> {
		private FilteredScanner wrapped;

		public static final FilteredScanner unwrap(final FilteredScanner possiblyWrapped) {
			if (possiblyWrapped instanceof FilteredScannerWrapper) {
				return ((FilteredScannerWrapper) possiblyWrapped).getWrappedScanner();
			}
			return possiblyWrapped;
		}

		public final FilteredScanner getWrappedScanner() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public <R extends LogRawAccess<? extends LogInputStream>> void find(final LogEntryReader<R> reader,
				final LogEntryReaderStrategy readerStrategy, final Log log, final R logAccess,
				final IncrementData incrementData, final EventConsumer eventConsumer) throws IOException {
			try {
				getWrappedScanner().find(reader, readerStrategy, log, logAccess, incrementData, eventConsumer);
			} catch (final ConfigException e) {
				throw new IOException("Failed to create configured scanner", e);
			}
		}

		@Override
		public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
			try {
				return getWrappedScanner().getFieldTypes();
			} catch (final ConfigException e) {
				throw new FormatException("Failed to create configured scanner", e);
			}
		}

		@Override
		public Scanner getTargetScanner() {
			return getWrappedScanner().getTargetScanner();
		}

		@Override
		public void setTargetScanner(final Scanner targetScanner) {
			getWrappedScanner().setTargetScanner(targetScanner);
		}

		@Override
		public List<FieldsFilter> getFilters() {
			return getWrappedScanner().getFilters();
		}

		@Override
		public void setFilters(final List<FieldsFilter> filters) {
			getWrappedScanner().setFilters(filters);
		}

	}
}
