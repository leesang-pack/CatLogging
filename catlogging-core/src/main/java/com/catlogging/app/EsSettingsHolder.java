package com.catlogging.app;

import com.catlogging.model.es.EsSettings;

import java.io.IOException;

public interface EsSettingsHolder {
		/**
		 * Returns current es settings.
		 *
		 * @return current es settings
		 */
		public EsSettings getSettings();

		/**
		 * Stores new es settings and applies it to the elasticsearch
		 * connection.
		 *
		 * @param settings
		 * @throws IOException
		 */
		public void storeSettings(final EsSettings settings) throws IOException;
	}