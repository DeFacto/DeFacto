package org.aksw.defacto.restful.webservice;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.restful.utils.Cfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/download")
public class Download {
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }
    public static Logger LOG = LogManager.getLogger(Download.class);

    @RequestMapping(
            value = "/files/{{id:^[0-9a-fA-F]{24}$}}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            method = RequestMethod.GET)
    public void getFile(@PathVariable("id") String id, HttpServletResponse response) {
        try {

            InputStream is = null;
            // TODO: implement me, read db

            FileCopyUtils.copy(is, response.getOutputStream());
            // response.setContentType("text/turtle");
            response.flushBuffer();

        } catch (IOException ex) {
            LOG.error("Error writing file to output stream: " + id, ex);
            throw new RuntimeException("IOe error writing file to stream");
        }
    }
}