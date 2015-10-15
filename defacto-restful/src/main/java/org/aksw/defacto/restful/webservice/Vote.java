package org.aksw.defacto.restful.webservice;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.restful.mongo.MongoManager;
import org.aksw.defacto.restful.utils.Cfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

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
    protected MongoManager     db         = null;

    // max and min (-1*mavote) vote score
    double                     maxVote    = 5;

    /**
     * Defacto config and example data loading.
     */
    @PostConstruct
    protected void init() {
        db = MongoManager.getMongoManager().setCollection(collection);
        db.connect();
    }

    public JSONArray getVotes(String id) {
        if (db == null)
            init();
        Iterator<DBObject> iter = db.find(new JSONObject().put("factid", id).toString());

        JSONArray ja = new JSONArray();
        while (iter.hasNext())
            ja.put(new JSONObject(iter.next().toString()));
        return ja;
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
    public void vote(
            @RequestBody String json, HttpServletResponse response
            ) {

        JSONObject in = new JSONObject(json);
        LOG.info("vote: " + in.toString(2));

        JSONObject query = new JSONObject()
                .put("factid", in.getString("factId"))
                .put("s", in.getString("s"))
                .put("p", in.getString("p"))
                .put("o", in.getString("o"));

        JSONObject update = new JSONObject(query)
                .put("$inc",
                        new JSONObject().put("votes", in.getInt("votes") > 0 ? 1 : -1)
                );

        db.coll.update(
                (DBObject) JSON.parse(query.toString()),
                (DBObject) JSON.parse(update.toString()),
                true, true);

    }
}
