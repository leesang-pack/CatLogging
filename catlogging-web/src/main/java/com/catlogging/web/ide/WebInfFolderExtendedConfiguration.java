package com.catlogging.web.ide;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

import com.catlogging.web.util.WebInfConfigurationHomeUnpacked;

public class WebInfFolderExtendedConfiguration extends WebInfConfigurationHomeUnpacked {

	@Override
	protected List<Resource> findJars(final WebAppContext context) throws Exception {
		List<Resource> r = super.findJars(context); // let original
													// WebInfConfiguration do
													// it's thing first
		if (r == null) {
			r = new LinkedList<Resource>();
		}

		final List<Resource> containerJarResources = context.getMetaData().getOrderedWebInfJars();
		r.addAll(containerJarResources);

		return r;
	}

}