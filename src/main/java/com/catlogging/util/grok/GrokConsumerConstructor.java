package com.catlogging.util.grok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.BeanPostConstructor;
import com.catlogging.config.ConfigException;
import com.catlogging.util.grok.GrokConsumerConstructor.GrokConsumer;

/**
 * Constructor to inject {@link GroksRegistry} to consumers.
 * 
 * @author Tester
 *
 */
@Component
public class GrokConsumerConstructor implements BeanPostConstructor<GrokConsumer> {
	@Autowired
	private GroksRegistry groksRegistry;

	/**
	 * Grok consumer with dependency to the {@link GroksRegistry}.
	 * 
	 * @author Tester
	 *
	 */
	public static interface GrokConsumer {
		/**
		 * Inits the consumer with the registry instance.
		 * 
		 * @param groksRegistry
		 */
		void initGrokFactory(GroksRegistry groksRegistry);
	}

//	public void postConstruct(GrokConsumer bean, BeanConfigFactoryManager configManager) throws ConfigException {
	@Override
	public void postConstruct(GrokConsumer bean) throws ConfigException {
		bean.initGrokFactory(groksRegistry);
	}
}
