package org.aksw.defacto.restful.webservice;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.restful.core.RestModel;
import org.aksw.defacto.restful.utils.SupportedRelations;
import org.aksw.defacto.util.EvidenceRDFGenerator;
import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * 
 * @author rspeck
 *
 */
@RestController
@RequestMapping("/demo")
public class Demo {

  public static Logger LOG = LogManager.getLogger(Fusion.class);
  protected RestModel model = null;

  @PostConstruct
  protected void init() {
    model = new RestModel();
  }

  /**
   * method: POST<br>
   * path: add/<br>
   * 
   * Used by the demo.
   *
   * @return json object
   */
  @RequestMapping(value = "/input", headers = "Accept=application/json",
      produces = "application/json", method = RequestMethod.POST)
  @ResponseBody
  public String input(@RequestBody final String jsonObject, final HttpServletResponse response) {
    JSONObject in = null;
    try {
      in = new JSONObject(jsonObject);

      DefactoModel defactoModel = null;
      Triple triple = null;

      // fact is set if input is an example fact
      if (in.has("fact")) {
        for (final FactBenchExample example : FactBenchExamplesLoader.loadExamples()) {
          if (example.getFact().equals(in.getString("fact"))) {
            LOG.info("Found example fact.");
            defactoModel = example.getModel();
            triple = example.getTriple();
            break;
          }
        }
      }

      if ((defactoModel == null) || (triple == null)) {
        LOG.info("A new fact.");
        // no example fact, so new input
        final String p = in.getString("p");
        if (!SupportedRelations.isSupportedRelation(new URI(p))) {
          try {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Relation (" + p + ") is not supported!");
            response.flushBuffer();
            return new JSONObject().toString();
          } catch (final IOException ee) {
            LOG.error(ee.getLocalizedMessage(), ee);
          }
        }
        triple = new Triple(NodeFactory.createURI(in.getString("s")), NodeFactory.createURI(p),
            NodeFactory.createURI(in.getString("o")));
        final String from =
            in.has("from") && (in.getString("from") != null) ? in.getString("from") : "";
        final String to = in.has("to") && (in.getString("to") != null) ? in.getString("to") : "";
        defactoModel = model.getModel(triple, from, to);
      }

      final Evidence evidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);

      // output
      final JSONObject jo = model.out(evidence);
      response.setStatus(HttpServletResponse.SC_OK);
      return jo.toString();

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      try {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.");
        response.flushBuffer();
      } catch (final IOException ee) {
        LOG.error(ee.getLocalizedMessage(), ee);
      }
    }
    return new JSONObject().toString();
  }

  /**
   * Download file.
   * 
   * method: POST<br>
   * path: download/<br>
   *
   * @param response
   */
  @RequestMapping(value = "/download",
      // produces = MediaType.APPLICATION_OCTET_STREAM_VALUE,
      method = RequestMethod.POST)
  public void download(@RequestBody final String jsonObject, final HttpServletResponse response) {

    LOG.info("input: " + jsonObject);
    // TODO: check input, clean
    JSONObject in = null;
    try {
      in = new JSONObject(jsonObject);

      DefactoModel defactoModel = null;
      Triple triple = null;

      // fact is set if input is an example fact
      if (in.has("fact")) {
        for (final FactBenchExample example : FactBenchExamplesLoader.loadExamples()) {
          if (example.getFact().equals(in.getString("fact"))) {
            LOG.info("Found example fact.");
            defactoModel = example.getModel();
            triple = example.getTriple();
            break;
          }
        }
      }

      if ((defactoModel == null) || (triple == null)) {
        LOG.info("A new fact.");
        final String p = in.getString("p");
        if (!SupportedRelations.isSupportedRelation(new URI(p))) {
          try {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Relation (" + p + ") is not supported!");
            response.flushBuffer();
            return;
          } catch (final IOException ee) {
            LOG.error(ee.getLocalizedMessage(), ee);
          }
        }

        // no example fact, so new input
        triple = new Triple(NodeFactory.createURI(in.getString("s")), NodeFactory.createURI(p),
            NodeFactory.createURI(in.getString("o")));
        final String from =
            in.has("from") && (in.getString("from") != null) ? in.getString("from") : "";
        final String to = in.has("to") && (in.getString("to") != null) ? in.getString("to") : "";
        defactoModel = model.getModel(triple, from, to);
      }

      LOG.info("Calls defacto ...");
      final Calendar startTime = Calendar.getInstance();
      final Evidence evidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
      final Calendar endTime = Calendar.getInstance();

      try {
        final String info = EvidenceRDFGenerator.getProvenanceInformationAsString(triple, evidence,
            startTime, endTime, "TURTLE");
        FileCopyUtils.copy(info.getBytes(), response.getOutputStream());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/turtle");
        response.flushBuffer();

      } catch (final IOException ex) {
        LOG.error("Error writing file to output stream.", ex);
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      try {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.");
        response.flushBuffer();
      } catch (final IOException ee) {
        LOG.error(ee.getLocalizedMessage(), ee);
      }
    }
  }
}
