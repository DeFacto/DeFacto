package org.aksw.defacto.restful.webservice;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.restful.mongo.MongoManager;
import org.aksw.defacto.restful.utils.Cfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author rspeck
 *
 */
@RestController
@RequestMapping("/fusion")
public class Vote {
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }
    public static Logger       LOG        = LogManager.getLogger(Vote.class);
    public static final String collection = "vote";
    protected MongoManager          db         = null;

    // max and min (-1*mavote) vote score
    double                     maxVote    = 5;

    /**
     * Defacto config and example data loading.
     */
    @PostConstruct
    protected void init() {
        db = MongoManager.getMongoManager().setCollection(collection);
    }

    /**
     * method: POST<br>
     * path: vote/<br>
     * 
     * @param json
     * @param response
     * @return
     */
    @RequestMapping(
            value = "/vote",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.POST)
    @ResponseBody
    public String vote(
            @RequestBody String json, HttpServletResponse response
            ) {
        LOG.info("vote: " + new JSONObject(json).toString(2));

        String id = null;
        String factid = null;
        Double score = null;

        try {

            String m = " ";
            JSONObject jo = new JSONObject(json);
            if (jo.has("score")) {
                score = jo.getDouble("score");
                if (score > maxVote)
                    score = maxVote;
                if (score < (-1 * maxVote))
                    score = -1 * maxVote;
            } else {
                m += "score (double),";
            }
            if (jo.has("id")) {
                id = jo.getString("id");
            } else {
                m += "id (String),";
            }
            if (jo.has("factId")) {
                factid = jo.getString("factId");
            } else {
                m += "factId (String),";
            }

            if (score != null && id != null && factid != null) {
                LOG.info("save vote to db");

                db.insert(new JSONObject()
                        .put("score", score)
                        .put("id", id)
                        .put("factId", factid).toString()
                        );

                response.setStatus(HttpServletResponse.SC_OK);
                return new JSONObject().toString();
            } else {
                m = "Could not found all parameters. Missin:" + m.substring(0, m.length() - 1);
                LOG.warn(m);
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, m);
                    response.flushBuffer();
                } catch (IOException ioe) {
                    LOG.error(ioe.getLocalizedMessage(), ioe);
                }
                return new JSONObject().toString();
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.\n");
                response.flushBuffer();
            } catch (IOException ioe) {
                LOG.error(ioe.getLocalizedMessage(), ioe);
            }
            return new JSONObject().toString();
        }
    }
}
