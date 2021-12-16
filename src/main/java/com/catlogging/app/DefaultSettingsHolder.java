package com.catlogging.app;

import com.catlogging.model.es.EsOperatingType;
import com.catlogging.model.es.EsSettings;
import com.catlogging.model.es.RemoteAddress;
import com.catlogging.util.HostInfoProp;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.ConfigValueStore;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import static com.catlogging.app.ElasticSearchConnect.PROP_ES_OPS_TYPE;
import static com.catlogging.app.ElasticSearchConnect.PROP_ES_REMOTE_ADDRESSES;

@Slf4j
public class DefaultSettingsHolder implements EsSettingsHolder {


	private EsSettings settings;

	private ConfigValueStore configValueStore;

	private ConfigValue<String> remoteAddresses;

	private ConfigValue<EsOperatingType> operatingType;

	public DefaultSettingsHolder(ConfigValue<String> remoteAddresses, ConfigValueStore configValueStore, ConfigValue<EsOperatingType> operatingType){
		this.remoteAddresses = remoteAddresses;
		this.configValueStore = configValueStore;
		this.operatingType = operatingType;
	}

	@Override
	public EsSettings getSettings() {
		settings = new EsSettings();
		settings.setOperatingType(operatingType.get());
		if (settings.getOperatingType() == EsOperatingType.REMOTE) {
			final List<RemoteAddress> addresses = HostInfoProp.parseHostInfo(remoteAddresses);
			log.info("Built remote addresses from config: {}", addresses);
			settings.setRemoteAddresses(addresses);
		}
		return settings;
	}

	@Override
	public void storeSettings(final EsSettings settings) throws IOException {

		configValueStore.store(PROP_ES_OPS_TYPE, settings.getOperatingType().toString());
		if (settings.getOperatingType() == EsOperatingType.REMOTE) {
			final StringBuilder addresses = new StringBuilder();
			for (final RemoteAddress a : settings.getRemoteAddresses()) {
				if (addresses.length() > 0) {
					addresses.append(",");
				}
				addresses.append(a.getHost() + ":" + a.getPort());
			}
			configValueStore.store(PROP_ES_REMOTE_ADDRESSES, addresses.toString());
		}
		this.settings = settings;

	}
}
