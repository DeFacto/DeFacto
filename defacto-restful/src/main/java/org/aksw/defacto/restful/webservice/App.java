package org.aksw.defacto.restful.webservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.restful.utils.Cfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Ini;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author rspeck
 *
 */
@SpringBootApplication
public class App {
  static {
    PropertyConfigurator.configure(Cfg.LOG_FILE);
  }

  public static Logger LOG = LogManager.getLogger(App.class);

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {
    try {
      // write shut down file
      writeShutDownFile("stop");

      final InputStream stream = Defacto.class.getClassLoader().getResourceAsStream("defacto.ini");
      if (stream == null) {
        LOG.error("Could not load resource file \"defacto.ini\"");
        return;
      }

      // load config
      Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(stream));
      // run app
      SpringApplication.run(App.class, args);
      // shutdown hook
      Runtime.getRuntime().addShutdownHook(new Thread((Runnable) () -> {
        // TODO: check data then shutdown the server
        //
        //
        LOG.info("Stopping server with shutdownHook.");
      } , "shutdownHook"));

    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Gives the applications process id.
   *
   * @return applications process id
   */
  public static synchronized String getProcessId() {

    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    final int index = jvmName.indexOf('@');
    if (index < 1) {
      return null;
    }
    try {
      return Long.toString(Long.parseLong(jvmName.substring(0, index)));
    } catch (final NumberFormatException e) {
      return null;
    }
  }

  /**
   * Writes a system depended file to shut down the application with kill cmd and process id.
   *
   * @return true if the file was written
   */
  public static synchronized boolean writeShutDownFile(final String fileName) {

    // get process Id
    final String id = getProcessId();
    if (id == null) {
      return false;
    }

    String cmd = "";
    String fileExtension = "";

    cmd = "kill " + id + System.getProperty("line.separator") + "rm " + fileName + ".sh";
    fileExtension = "sh";
    LOG.info(fileName + "." + fileExtension);

    final File file = new File(fileName + "." + fileExtension);
    try {
      final BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(cmd);
      out.close();
    } catch (final Exception e) {
      LOG.error(e.getMessage());
    }
    file.setExecutable(true, false);
    file.deleteOnExit();
    return true;
  }
}
