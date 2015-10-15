package org.aksw.defacto.restful.utils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.restful.webservice.Fusion;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author rspeck
 *
 */
public class SupportedRelations {
    public static Logger                 LOG                   = LogManager.getLogger(SupportedRelations.class);

    private static final String          supportedRelations    = "supported_relations.txt";

    /* uri to label */
    protected static Map<String, String> supportedRelationsMap = new HashMap<>();

    static {
        try {
            Files.lines(
                    Paths.get(
                            Fusion.class.getClassLoader().getResource(supportedRelations).getPath()
                            )
                    ).forEach(line -> {
                        try {
                            String[] split = line.split(",");
                            supportedRelationsMap.put(new URI(split[1]).toString(), split[0]);
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    });
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    public static boolean isSupportedRelation(URI relation) {
        try {
            if (supportedRelationsMap.containsKey(relation.toString()))
                return true;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public static String getSupportedRelation() {
        JSONArray o = new JSONArray();
        supportedRelationsMap.forEach((k, v) -> {
            o.put(new JSONObject().put("key", k).put("value", v));
        });
        return o.toString();
    }
}
