/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.ini4j.Ini;

/**
 *
 * @author ngonga
 */
@Path("/getdefactotimes")
public class TimeSequenceService {

	// http://localhost:9998/getdefactotimes?s=http%3A%2F%2Fdbpedia.org%2Fresource%2FMichael_Ballack&p=http%3A%2F%2Fdbpedia.org%2Fontology%2Fteam&o=http%3A%2F%2Fdbpedia.org%2Fresource%2FChelsea_F.C.&slabel=Michael%20Ballack&olabel=Chelsea%20F.C.
	
    @GET
    @Produces("application/json")
    public Response getJson(@QueryParam("s") String subject, @QueryParam("slabel") String slabel,
            @QueryParam("p") String property, @QueryParam("o") String object, @QueryParam("olabel") String olabel) {
        ServiceMain.log.log(Level.INFO, "Processing <" + subject + ", " + property + "," + object + ">");
        try {

            org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
            Model model = ModelFactory.createDefaultModel();
            Resource subj = model.createResource(subject);
            subj.addProperty(RDFS.label, slabel);
            Resource obj = model.createResource(property);
            obj.addProperty(RDFS.label, olabel);
            obj.addProperty(model.createProperty(property), subj);
            Evidence ev = Defacto.checkFact(new DefactoModel(model, subject + " " + property + " " + object, true), Defacto.TIME_DISTRIBUTION_ONLY.YES);
            String result = "{\n";
            result += String.format("\t\"subject\":\t\"%s\"", subject) + ",\n" ;
            result += String.format("\t\"slabel\":\t\"%s\"", slabel) + ",\n" ;
            result += String.format("\t\"predicate\":\t\"%s\"", property) + ",\n" ;
            result += String.format("\t\"object\":\t\"%s\"", object) + ",\n" ;
            result += String.format("\t\"olabel\":\t\"%s\"", olabel) + ",\n" ;
            result += "\t\"time\":\t {\n";
            int i = 1;
            for ( Map.Entry<String,Long> times : ev.yearOccurrences.entrySet()) {
            	
            	result += "\t\t\""+times.getKey()+"\":\t" + times.getValue() + (i++ < ev.yearOccurrences.size() ? ",\n" : "\n");
            }
            result += "\t}\n}";
            return Response.ok(result).build();
        } catch (Exception e) {
            ServiceMain.log.log(Level.WARNING, "Error while processing <" + subject + "," + property + "," + object + ">");
            ServiceMain.log.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        }
        return Response.serverError().build();
    }
}
