package com.catlogging.model.es;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class EsStatusAndSettings {
		private EsSettings settings;

		private EsStatus status;
		private String statusMessage;

		/**
		 * @return the settings
		 */
		public EsSettings getSettings() {
			return settings;
		}

		/**
		 * @return the status
		 */
		public EsStatus getStatus() {
			return status;
		}

		/**
		 * @return the statusMessage
		 */
		public String getStatusMessage() {
			return statusMessage;
		}

	}