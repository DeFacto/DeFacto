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

  public static Logger LOG = LogManager.getLogger(Vote.class);
  public static final String collection = "data";
  protected MongoManager db = null;

  // max and min (-1*mavote) vote score
  double maxVote = 5;

  /**
   * Defacto config and example data loading.
   */
  @PostConstruct
  protected void init() {
    db = MongoManager.getMongoManager().setCollection(collection);
    db.connect();
  }

  public JSONArray getVotes(final String id) {
    if (db == null) {
      init();
    }
    final Iterator<DBObject> iter = db.find(new JSONObject().put("id", id).toString());
    final JSONArray ja = new JSONArray();
    while (iter.hasNext()) {
      ja.put(new JSONObject(iter.next().toString()));
    }
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
  @RequestMapping(value = "/vote", headers = "Accept=application/json",
      produces = "application/json", method = RequestMethod.POST)
  @ResponseBody
  public void vote(@RequestBody final String json, final HttpServletResponse response) {

    final JSONObject in = new JSONObject(json);
    LOG.info("vote: " + in.toString(2));

    final DBObject doc = db.findDocumentById(in.getString("id"));
    doc.removeField("upvotes");
    doc.removeField("downvotes");

    // TODO: set a vote limit
    final JSONObject update = new JSONObject().put("$inc",
        new JSONObject().put(in.getInt("votes") > 0 ? "upvotes" : "downvotes", 1));

    db.coll.update(doc, (DBObject) JSON.parse(update.toString()), true, true);
  }
}
