/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.

 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.catlogging.web.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * JAR starter class, which extracts required Jetty libs and JettyLauncher to
 * temp folder and launches Jetty from there.
 * 
 * @author Tester
 * 
 */
public class Starter {

	private static final String JETTY_LAUNCHER_CLASS = "com.catlogging.web.util.JettyLauncher";

	/**
	 * Extracts required Jetty libs and JettyLauncher to temp folder and
	 * launches Jetty from there.
	 * 
	 * @param args
	 *            Program parameters fully delegated to JettyLauncher
	 * @throws Exception
	 *             in case of any errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(final String[] args) throws Exception {
		if (!prepareHomeDir()) {
			System.exit(-1);
		}
		System.out.println("Preparing Jetty...");
		ProtectionDomain domain = Starter.class.getProtectionDomain();
		URL warUrl = domain.getCodeSource().getLocation();
		File warFile = new File(warUrl.toURI());

		JarFile jarFile = null;
		ArrayList<URL> execLibs = new ArrayList<URL>();
		try {
			jarFile = new JarFile(warUrl.getPath());
			for (String execLib : getExecLibs(jarFile)) {
				execLibs.add(extractExecLib(warFile, jarFile, "WEB-INF/exec/" + execLib, execLib));
			}

			File launcherLib = getFileInHomeDir("exec/launcher.jar");
			if (!launcherLib.exists() || launcherLib.lastModified() < warFile.lastModified()) {
				ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(launcherLib)));
				for (String z : new String[] { JETTY_LAUNCHER_CLASS,
						"com.catlogging.web.util.WebContextWithExtraConfigurations",
						"com.catlogging.web.util.WebInfConfigurationHomeUnpacked",
						"com.catlogging.web.util.WebInfConfigurationUnpackOverridable" }) {
					ZipEntry ze1 = new ZipEntry(z.replace('.', '/') + ".class");
					zos.putNextEntry(ze1);
					copy(jarFile.getInputStream(jarFile.getEntry("WEB-INF/classes/" + z.replace('.', '/') + ".class")),
							zos);
				}
				zos.close();
			}
			execLibs.add(launcherLib.toURI().toURL());
		} finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}

		ClassLoader urlClassLoader = new URLClassLoader(execLibs.toArray(new URL[execLibs.size()]));
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		System.out.println("Launching Jetty...");
		Class jettyLauncher = urlClassLoader.loadClass(JETTY_LAUNCHER_CLASS);
		Method mainMethod = jettyLauncher.getMethod("start", new Class[] { String[].class, URL.class });
		mainMethod.invoke(jettyLauncher.newInstance(), new Object[] { args, warUrl });
		System.out.println("Jetty stopped");
	}

	private static boolean prepareHomeDir() throws Exception {
		if (System.getProperty("catlogging.home") == null) {
			System.setProperty("catlogging.home", System.getProperty("user.home") + "/catlogging");
		}
		String catloggingHomeDir = System.getProperty("catlogging.home");
		File catloggingHomeDirFile = new File(catloggingHomeDir);
		System.out.println("Starting catlogging with home directory: " + catloggingHomeDirFile.getPath());
		if (!catloggingHomeDirFile.exists()) {
			System.out.println("Home directory isn't present, going to create it");
			String errMsg = "Failed to create home directory \"" + catloggingHomeDirFile.getPath()
					+ "\". catlogging can't operate without a write enabled home directory. Please create the home directory manually and grant the user catlogging is running as the write access.";
			try {
				if (catloggingHomeDirFile.mkdirs()) {
					return prepareHomeDirStructure(catloggingHomeDirFile);
				}
				System.err.println(errMsg);
			} catch (Exception e) {
				System.err.println(errMsg);
				throw e;
			}
		} else if (!catloggingHomeDirFile.canWrite()) {
			System.err.println("Configured home directory \"" + catloggingHomeDirFile.getPath()
					+ "\" isn't write enabled. catlogging can't operate without a write enabled home directory. Please grant the user catlogging is running as the write access.");
		} else {
			return prepareHomeDirStructure(catloggingHomeDirFile);
		}
		return false;
	}

	private static boolean prepareHomeDirStructure(final File homeDir) {
		for (String sub : new String[] { "exec", "logs" }) {
			File subFolder = new File(homeDir, sub);
			if (!subFolder.exists()) {
				boolean created = false;
				try {
					created = subFolder.mkdir();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				if (!created) {
					System.err.println("Failed to create folder in home directory: " + subFolder.getAbsolutePath());
					return false;
				}
			}
		}
		return true;
	}

	private static String[] getExecLibs(final JarFile jarFile) throws IOException {
		InputStream libsis = null;
		try {
			libsis = jarFile.getInputStream(jarFile.getEntry("WEB-INF/exec/libs.txt"));
			return new BufferedReader(new InputStreamReader(libsis)).readLine().split(":");
		} finally {
			if (libsis != null) {
				libsis.close();
			}
		}
	}

	private static File getFileInHomeDir(final String file) {
		return new File(new File(System.getProperty("catlogging.home")), file);
	}

	private static URL extractExecLib(final File sourcePackage, final JarFile jarFile, final String libPath,
			final String lib) throws Exception {
		File tempLib = getFileInHomeDir("exec/" + lib);
		if (!tempLib.exists() || tempLib.lastModified() < sourcePackage.lastModified()) {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempLib));
			copy(jarFile.getInputStream(jarFile.getEntry(libPath)), out);
			out.close();
			tempLib.deleteOnExit();
		}
		return tempLib.toURI().toURL();
	}

	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		byte[] buffer = new byte[4096 * 32];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			if (len > 0) {
				out.write(buffer, 0, len);
			}
		}
	}

}
