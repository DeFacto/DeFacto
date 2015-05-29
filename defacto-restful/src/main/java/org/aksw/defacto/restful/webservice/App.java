package org.aksw.defacto.restful.webservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;

import org.aksw.defacto.restful.utils.Cfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
    public static void main(String[] args) {

        writeShutDownFile("stop");
        SpringApplication.run(App.class, args);

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Checking data before shutting down server...");
                // TODO: check data then shut down server.
                //
                //
                LOG.info("Stopping server with shutdownHook.");

            }
        }, "ServerShutdownHook"));
    }

    /**
     * Gives the applications process id.
     * 
     * @return applications process id
     */
    public static synchronized String getProcessId() {

        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        if (index < 1)
            return null;
        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Writes a system depended file to shut down the application with kill cmd
     * and process id.
     * 
     * @return true if the file was written
     */
    public static synchronized boolean writeShutDownFile(String fileName) {

        // get process Id
        String id = getProcessId();
        if (id == null)
            return false;

        String cmd = "";
        String fileExtension = "";

        cmd = "kill " + id + System.getProperty("line.separator") + "rm " + fileName + ".sh";
        fileExtension = "sh";
        LOG.info(fileName + "." + fileExtension);

        File file = new File(fileName + "." + fileExtension);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(cmd);
            out.close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        file.setExecutable(true, false);
        file.deleteOnExit();
        return true;
    }
}
