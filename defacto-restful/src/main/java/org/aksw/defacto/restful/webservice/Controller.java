package org.aksw.defacto.restful.webservice;

import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.aksw.defacto.restful.Play;
import org.aksw.defacto.restful.mongo.MongoDB;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DBObject;

/**
 * 
 * @author rspeck
 *
 */
@RestController
@RequestMapping("/rest")
public class Controller {

    protected MongoDB db = new MongoDB();

    /**
     * 
     */
    @PostConstruct
    public void init() {

    }

    /**
     * 
     * @return
     */
    @RequestMapping("/")
    @ResponseBody
    String home() {
        Play.main(null);
        return Play.test;
    }

    // -- MONGO
    protected void insert(JSONObject jo) {
        db.insert(jo.toString());
    }

    protected Iterator<DBObject> search(JSONObject jo) {
        return db.search(jo.toString());
    }
}
