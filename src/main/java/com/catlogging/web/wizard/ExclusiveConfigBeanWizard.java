package com.catlogging.web.wizard;

import com.catlogging.config.ConfiguredBean;

/**
 * Support wizards with exclusive typing.
 * 
 * @author Tester
 *
 * @param <BeanType>
 */
public interface ExclusiveConfigBeanWizard<BeanType extends ConfiguredBean> extends ConfigBeanWizard<BeanType> {
	Class<? super BeanType> getExclusiveType();
}
