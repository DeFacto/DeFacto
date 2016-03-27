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

  public static final Logger LOG = LogManager.getLogger(MongoManager.class);
  public static XMLConfiguration config = CfgManager.getCfg(MongoManager.class);

  public static String HOST = "db.host";
  public static String PORT = "db.port";
  public static String NAME = "db.name";
  public static String COLLECTION = "db.collection";

  protected final static MongoClient mc =
      new MongoClient(config.getString(HOST), config.getInt(PORT));
  protected DB db = null;
  public DBCollection coll = null;

  public String name = null;
  public String collection = null;

  private final String idKey = "_id";

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

  public MongoManager setConfig(final String name, final String collection) {

    disconnect();

    this.name = name;
    return setCollection(collection);
  }

  public MongoManager setCollection(final String collection) {
    this.collection = collection;
    coll = null;
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
    if (coll == null) {
      try {
        coll = db.getCollection(collection);
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  public String insert(final String json) {
    connect();
    try {
      final DBObject o = (DBObject) JSON.parse(json);
      final ObjectId id = ObjectId.get();
      o.put(idKey, id);
      coll.insert(o);
      return id.toString();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public boolean findDoc(final String json) {
    connect();
    return coll.find((DBObject) JSON.parse(json)).length() > 0 ? true : false;
  }

  public Iterator<DBObject> find(final DBObject dbObject) {
    connect();
    return coll.find(dbObject).iterator();
  }

  public Iterator<DBObject> find(final String json) {
    connect();
    return coll.find((DBObject) JSON.parse(json)).iterator();
  }

  public DBObject findDocumentById(final String id) {
    connect();
    final BasicDBObject obj = new BasicDBObject();
    obj.put(idKey, new ObjectId(id));
    return coll.findOne(obj);
  }

  public boolean deleteDocumentById(final DBObject o) {
    connect();
    final WriteResult wr = coll.remove(o);
    return wr.isUpdateOfExisting();
  }

  public boolean deleteDocumentById(final String id) {
    connect();
    final BasicDBObject obj = new BasicDBObject();
    obj.put(idKey, new ObjectId(id));
    return deleteDocumentById(obj);
  }

  public List<DBObject> getAll() {
    connect();
    final DBCursor cursorDoc = coll.find();
    final List<DBObject> list = new ArrayList<>();
    while (cursorDoc.hasNext()) {
      list.add((cursorDoc.next()));
    }
    cursorDoc.close();
    return list;
  }

  public void print() {
    connect();
    final DBCursor cursorDoc = coll.find();
    while (cursorDoc.hasNext()) {
      LOG.debug((cursorDoc.next()));
    }
    cursorDoc.close();
  }

  public Iterator<DBObject> findOperation(final String key, final String op, final BsonArray ba) {
    connect();
    return find(new Document(key, new Document(op, ba)).toJson());
  }

  public static void main(final String[] a) throws ParseException {

    final MongoManager db = MongoManager.getMongoManager().setCollection("test");

    String id = db.insert("123");
    LOG.info(id == "" ? "Could not insert" : "inserted");

    id = db.insert(new JSONObject().put("test", "haha").toString());
    LOG.info(id == "" ? "Could not insert" : "inserted");

    db.findOperation("_id", "$in", new BsonArray(Arrays.asList(new BsonObjectId(new ObjectId(id)))))
        .forEachRemaining(LOG::info);

  }
}
