package com.catlogging.model.es;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public final class EsSettings {
		@NotNull
		private EsOperatingType operatingType = EsOperatingType.EMBEDDED;

		@Valid
		private List<RemoteAddress> remoteAddresses;

		/**
		 * @return the operatingType
		 */
		public EsOperatingType getOperatingType() {
			return operatingType;
		}

		/**
		 * @param operatingType
		 *            the operatingType to set
		 */
		public void setOperatingType(final EsOperatingType operatingType) {
			this.operatingType = operatingType;
		}

		/**
		 * @return the remoteAddresses
		 */
		public List<RemoteAddress> getRemoteAddresses() {
			return remoteAddresses;
		}

		/**
		 * @param remoteAddresses
		 *            the remoteAddresses to set
		 */
		public void setRemoteAddresses(final List<RemoteAddress> remoteAddresses) {
			this.remoteAddresses = remoteAddresses;
		}

	}