package com.catlogging.system.version;

import java.io.IOException;

/**
 * Provides info regarding new software updates.
 * 
 * @author Tester
 *
 */
public interface UpdatesInfoProvider {
	/**
	 * Wrapper for context information.
	 * 
	 * @author Tester
	 *
	 */
	public interface UpdatesInfoContext {
		String getCurrentVersion();
	}

	/**
	 * Returns updates information for current context.
	 * @param context 
	 * @return update information
	 * @throws IOException when target doesn't provide the proper information
	 */
	VersionInfo getLatestStableVersion(UpdatesInfoContext context) throws IOException;
}
