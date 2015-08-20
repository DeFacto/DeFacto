package org.aksw.defacto.restful.mongo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aksw.defacto.restful.utils.Cfg;
import org.aksw.defacto.restful.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.BsonArray;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class MongoManager {

    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }

    public static final Logger         LOG        = LogManager.getLogger(MongoManager.class);
    public static XMLConfiguration     config     = CfgManager.getCfg(MongoManager.class);

    public static String               HOST       = "db.host";
    public static String               PORT       = "db.port";
    public static String               NAME       = "db.name";
    public static String               COLLECTION = "db.collection";

    protected final static MongoClient mc         = new MongoClient(config.getString(HOST), config.getInt(PORT));
    protected DB                       db         = null;
    public DBCollection                coll       = null;

    public String                      name       = null;
    public String                      collection = null;

    private final String               idKey      = "_id";

    public static MongoManager getMongoManager() {
        return new MongoManager();
    }

    protected MongoManager() {
        getDefaultConfig();
    }

    public MongoManager getDefaultConfig() {
        name = config.getString(NAME);
        collection = config.getString(COLLECTION);
        return this;
    }

    public MongoManager setConfig(String name, String collection) {

        disconnect();

        this.name = name;
        return setCollection(collection);
    }

    public MongoManager setCollection(String collection) {
        this.collection = collection;
        this.coll = null;
        return this;
    }

    public void disconnect() {
        db = null;
        coll = null;
    }

    public void connect() {
        if (db == null) {
            db = mc.getDB(name);
        }
        if (coll == null)
            try {
                coll = db.getCollection(collection);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
    }

    public String insert(String json) {
        connect();
        try {
            DBObject o = (DBObject) JSON.parse(json);
            ObjectId id = ObjectId.get();
            o.put(idKey, id);
            coll.insert(o);
            return id.toString();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return "";
        }
    }

    public boolean findDoc(String json) {
        connect();
        return coll.find((DBObject) JSON.parse(json)).length() > 0 ? true : false;
    }

    public Iterator<DBObject> find(DBObject dbObject) {
        connect();
        return coll.find(dbObject).iterator();
    }

    public Iterator<DBObject> find(String json) {
        connect();
        return coll.find((DBObject) JSON.parse(json)).iterator();
    }

    public DBObject findDocumentById(String id) {
        connect();
        BasicDBObject obj = new BasicDBObject();
        obj.put(idKey, new ObjectId(id));
        return coll.findOne(obj);
    }

    public boolean deleteDocumentById(DBObject o) {
        connect();
        WriteResult wr = coll.remove(o);
        return wr.isUpdateOfExisting();
    }

    public boolean deleteDocumentById(String id) {
        connect();
        BasicDBObject obj = new BasicDBObject();
        obj.put(idKey, new ObjectId(id));
        return deleteDocumentById(obj);
    }

    public List<DBObject> getAll() {
        connect();
        DBCursor cursorDoc = coll.find();
        List<DBObject> list = new ArrayList<>();
        while (cursorDoc.hasNext())
            list.add((cursorDoc.next()));
        cursorDoc.close();
        return list;
    }

    public void print() {
        connect();
        DBCursor cursorDoc = coll.find();
        while (cursorDoc.hasNext())
            LOG.debug((cursorDoc.next()));
        cursorDoc.close();
    }

    public Iterator<DBObject> findOperation(String key, String op, BsonArray ba) {
        connect();
        return find(new Document(key, new Document(op, ba)).toJson());
    }

    public static void main(String[] a) throws ParseException {

        MongoManager db = MongoManager.getMongoManager().setCollection("test");

        String id = db.insert("123");
        LOG.info(id == "" ? "Could not insert" : "inserted");

        id = db.insert(new JSONObject().put("test", "haha").toString());
        LOG.info(id == "" ? "Could not insert" : "inserted");

        db.findOperation("_id", "$in", new BsonArray(Arrays.asList(new BsonObjectId(new ObjectId(id)))))
                .forEachRemaining(LOG::info);

    }
}
