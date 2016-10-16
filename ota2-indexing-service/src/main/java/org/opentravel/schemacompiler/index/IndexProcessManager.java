/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Process manager that ensures an <code>IndexAgent</code> is running at all times until
 * the manager is shut down.
 * 
 * @author S. Livezey
 */
public class IndexProcessManager {
	
	public static final int FATAL_EXIT_CODE = 69; // service unavailable exit code
	
	private static final String AGENT_CONFIG_SYSPROP = "ota2.index.agent.config";
	private static final String AGENT_CONFIG_FILE    = "indexing-agent.properties";
	private static final String AGENT_CONFIG_JVMOPTS = "agent.jvmOptions";
	
    private static final boolean DEBUG = false;
    
    private static Log log = LogFactory.getLog(IndexProcessManager.class);
    
    private static JMXConnectorServer jmxServer;
	private static boolean shutdownRequested = false;
	private static Thread launcherThread;
	private static Process agentProcess;
	
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args  the command-line arguments (ignored)
	 */
	public static void main(String[] args) {
		try {
			startJMXServer();
			log.info("Indexing process manager started.");
			launcherThread = new Thread( new AgentLauncher() );
			shutdownRequested = false;
			launcherThread.start();
			
			while (launcherThread.isAlive()) {
				try {
					Thread.sleep(1000);
					
				} catch (InterruptedException e) {}
			}
			
		} catch (Throwable t) {
			t.printStackTrace( System.out );
		}
	}
	
	/**
	 * Shuts down the process manager as well as the indexing agent child process.
	 */
	public static void shutdown() {
		if ((launcherThread != null) && launcherThread.isAlive()) {
			try {
				shutdownRequested = true;
				
				if (agentProcess != null) {
					agentProcess.destroy();
				}
				launcherThread.interrupt();
				jmxServer.stop();
				log.info("Indexing process manager shut down.");
				
			} catch (IOException e) {
				log.error("Error shutting down JMX server", e);
			}
		}
	}
	
	/**
	 * Starts the JMX server that will expose the shutdown hook for this manager.
	 * 
	 * @throws IOException  thrown if the JMX service cannot be launched
	 */
	private static void startJMXServer() throws IOException {
		try {
			JMXServiceURL jmxUrl = new JMXServiceURL( IndexProcessManagerMBean.JMX_SERVER_URL );
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName( IndexProcessManagerMBean.MBEAN_NAME );
	        
	        mbs.registerMBean( new ShutdownHook(), name );
	        LocateRegistry.createRegistry( IndexProcessManagerMBean.JMX_PORT );
			jmxServer = JMXConnectorServerFactory.newJMXConnectorServer( jmxUrl, null, mbs );
			jmxServer.start();
			
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException
				| MBeanRegistrationException | NotCompliantMBeanException e) {
			throw new IOException(e);
		}
		
	}
	
	/**
	 * Launches the given Java main class as an external sub-process.
	 * 
	 * @param mainClass  the main class to be executed externally
	 * @return Process
	 * @throws IOException  thrown if the external process cannot be launched
	 */
	private static Process launchJavaProcess(Class<?> mainClass) throws IOException {
		String javaCmd = System.getProperty("java.home") + File.separatorChar +
				"bin" + File.separatorChar + "java.exe";
		String repoConfigLocation = System.getProperty("ota2.repository.config");
		String log4jConfig = "-Dlog4j.configuration=file:/" +
				System.getProperty("user.dir") + "/conf/log4j-agent.properties";
		Properties agentConfig = getAgentConfigurationSettings();
		String jvmOpts = agentConfig.getProperty( AGENT_CONFIG_JVMOPTS );
		String oomeOption = getJvmOptionForOutOfMemoryErrors();
		String classpath = System.getProperty("java.class.path");
		boolean isWindows = SystemUtils.isWindows();
		
		if (repoConfigLocation == null) {
			repoConfigLocation = System.getProperty("user.dir") + "/conf/ota2-repository-config.xml";
		}
		repoConfigLocation = "-Dota2.repository.config=" + repoConfigLocation;
		
		// For windows, we must wrap all of the path arguments in double quotes in case
		// they contain spaces.
		if (isWindows) {
			javaCmd = "\"" + javaCmd + "\"";
			oomeOption = "\"" + oomeOption + "\"";
			repoConfigLocation = "\"" + repoConfigLocation + "\"";
			log4jConfig = "\"" + log4jConfig + "\"";
			classpath = "\"" + classpath + "\"";
		}
		
		// Build the list of parameters for the executable command
		List<String> command = new ArrayList<>();
		
		command.add( javaCmd );
		if (jvmOpts != null) command.addAll( Arrays.asList( jvmOpts.split("\\s+") ) );
		if (oomeOption != null) command.add( oomeOption );
		command.add( repoConfigLocation );
		command.add( log4jConfig );
		command.add( "-cp" );
		command.add( classpath );
		command.add( mainClass.getName() );
		
		for (String cmd : command) {
			System.out.print( cmd + " ");
		}
		System.out.println();
		
		log.info("Starting indexing agent process...");
		return new ProcessBuilder()
			.command( command )
			.redirectOutput( Redirect.PIPE )
			.start();
	}
	
	/**
	 * Returns the JVM option that will force a shutdown of the JVM if an
	 * <code>OutOfMemoryError</code> is encountered in the child process.  The options
	 * returned by this method reflect the logic implemented in the GemFire open source
	 * server.
	 * 
	 * @return String
	 */
	private static String getJvmOptionForOutOfMemoryErrors() {
		String jvmOption = "";
		
		if (SystemUtils.isHotSpotVM()) {
			if (SystemUtils.isWindows()) {
				// ProcessBuilder "on Windows" needs every word (space separated) to be
				// a different element in the array/list. See #47312. Need to study why!
				jvmOption = "-XX:OnOutOfMemoryError=taskkill /F /PID %p";
				
			} else { // All other platforms (Linux, Mac OS X, UNIX, etc)
				jvmOption = "-XX:OnOutOfMemoryError=kill -KILL %p";
			}
			
		} else if (SystemUtils.isJ9VM()) {
			// NOTE IBM states the following IBM J9 JVM command-line option/switch has
			// side-effects on "performance", as noted in the reference documentation...
			// http://publib.boulder.ibm.com/infocenter/javasdk/v6r0/index.jsp?topic=/com.ibm.java.doc.diagnostics.60/diag/appendixes/cmdline/commands_jvm.html
			jvmOption = "-Xcheck:memory";
			
		} else if (SystemUtils.isJRockitVM()) {
			// NOTE the following Oracle JRockit JVM documentation was referenced to
			// identify the appropriate JVM option to set when handling OutOfMemoryErrors.
			// http://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/jrdocs/refman/optionXX.html
			jvmOption = "-XXexitOnOutOfMemory";
		}
		return jvmOption;
	}
	
	/**
	 * Loads the command-line configuration settings for the indexing agent JVM.
	 * 
	 * @return Properties
	 */
	private static Properties getAgentConfigurationSettings() {
		Properties configSettings = new Properties();
		File configFile = null;
		
		if (System.getProperties().containsKey( AGENT_CONFIG_SYSPROP )) {
			configFile = new File( System.getProperty( AGENT_CONFIG_SYSPROP ) );
			if (!configFile.exists()) configFile = null;
		}
		if (configFile == null) {
			configFile = new File( System.getProperty("user.dir") + "/conf/" + AGENT_CONFIG_FILE );
			if (!configFile.exists()) configFile = null;
		}
		
		if (configFile != null) {
			try (InputStream is = new FileInputStream( configFile )) {
				configSettings.load( is );
				
			} catch (IOException e) {
				log.warn("Error reading indexing agent configuration settings.", e);
			}
		}
		return configSettings;
	}

	/**
	 * Runner that handles the launching of agent processes.
	 */
	private static class AgentLauncher implements Runnable {
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				boolean agentFatalError = false;
				
				while (!shutdownRequested) {
					agentProcess = launchJavaProcess( IndexingAgent.class );
					
					if (DEBUG) {
						try (Reader reader = new InputStreamReader( agentProcess.getInputStream() )) {
							int ch;
							
							while ((ch = reader.read()) >= 0) {
								System.out.print((char) ch);
							}
						} catch (Throwable t) {
							log.error("Error piping sub-process output.", t);
						}
					}
					int exitCode = agentProcess.waitFor();
					
					agentFatalError = (exitCode == FATAL_EXIT_CODE);
					
					if (agentFatalError) {
						log.warn("The indexing agent encountered a fatal error (see agent log for details).");
						shutdown();
						
					} else {
						log.warn("The indexing agent appears to have crashed - restarting...");
					}
				}
				
			} catch (IOException e) {
				log.fatal("Fatal exception encountered while trying to launch indexing agent", e);
				
			} catch (InterruptedException e) {
				log.info("Indexing agent shut down.");
			}
		}
	}
	
	/**
	 * MBean implementation of the <code>IndexProcessManagerMBean</code> interface.
	 */
	private static class ShutdownHook extends StandardMBean implements IndexProcessManagerMBean {
		
		/**
		 * Default constructor.
		 */
		public ShutdownHook() {
			super( IndexProcessManagerMBean.class, true );
		}

		/**
		 * @see org.opentravel.schemacompiler.index.IndexProcessManagerMBean#shutdown()
		 */
		@Override
		public void shutdown() {
			IndexProcessManager.shutdown();
		}
		
	}
}
