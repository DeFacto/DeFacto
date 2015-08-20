package org.aksw.defacto.restful.mongo;

import java.util.Iterator;

import org.aksw.defacto.restful.utils.Cfg;
import org.aksw.defacto.restful.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

/*
  db.insert(new JSONObject().put("uri", "http://test").put("x", new JSONArray()).toString());
  
    Iterator<DBObject> iter = db.search(new JSONObject().put("uri", "http://test").toString());
    while (iter.hasNext()) {
        LOG.info(iter.next());
    }
 */
public class MongoDB {

    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }

    public static final Logger     LOG        = LogManager.getLogger(MongoDB.class);
    public static XMLConfiguration config     = CfgManager.getCfg(MongoDB.class);

    // config keys
    public static String           HOST       = "db.host";
    public static String           PORT       = "db.port";
    public static String           NAME       = "db.name";
    public static String           COLLECTION = "db.collection";

    //
    protected DB                   db         = null;
    protected MongoClient          mc         = null;
    protected DBCollection         coll       = null;

    /**
     * 
     */
    public MongoDB() {
    }

    public void disconnect() {
        // TODO
    }

    /**
     * 
     */
    public void connect() {

        try {
            mc = new MongoClient(config.getString(HOST), config.getInt(PORT));
            db = mc.getDB(config.getString(NAME));
            coll = db.getCollection(config.getString(COLLECTION));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param json
     *            insert data
     */
    public void insert(String json) {
        if (coll == null)
            connect();

        coll.insert((DBObject) JSON.parse(json));
    }

    /**
     * 
     * @param json
     * @return
     */
    public boolean findDoc(String json) {
        if (coll == null)
            connect();
        return coll.find((DBObject) JSON.parse(json)).length() > 0 ? true : false;
    }

    public Iterator<DBObject> search(String json) {
        if (coll == null)
            connect();
        return coll.find((DBObject) JSON.parse(json)).iterator();
    }

    /**
     * 
     */
    public void print() {
        if (coll == null)
            connect();
        DBCursor cursorDoc = coll.find();
        while (cursorDoc.hasNext())
            LOG.debug((cursorDoc.next()));
        cursorDoc.close();
    }
}
