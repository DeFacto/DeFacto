/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.webservices.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author ngonga
 */
@Path("/getdefactoscore")
public class ScoreFetchingService {

	// example: http://localhost:9998/getdefactoscore?s=http%3A%2F%2Fdbpedia.org%2Fresource%2FMichael_Ballack&p=http%3A%2F%2Fdbpedia.org%2Fontology%2Fteam&o=http%3A%2F%2Fdbpedia.org%2Fresource%2FChelsea_F.C.&slabel=Michael%20Ballack&olabel=Chelsea%20F.C.
	
	
    @GET
    @Produces("application/json")
    public Response getJson(@QueryParam("s") String subject, @QueryParam("slabel") String slabel,
            @QueryParam("p") String property, @QueryParam("o") String object, @QueryParam("olabel") String olabel) {
//        DefactoServer.log.log(Level.INFO, "Processing <" + subject + ", " + property + ", " + object + ">");
//        try {
//
//            org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
//            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
//            Model model = ModelFactory.createDefaultModel();
//            Resource subj = model.createResource(subject);
//            subj.addProperty(RDFS.label, slabel);
//            Resource obj = model.createResource(property);
//            obj.addProperty(RDFS.label, olabel);
//            obj.addProperty(model.createProperty(property), subj);
//            Double score = Defacto.checkFact(new DefactoModel(model, subject + " " + property + " " + object, true), TIME_DISTRIBUTION_ONLY.NO).getDeFactoScore();
//            return Response.ok("{\"defactoScore\": "+score+"}").build();
//        } catch (Exception e) {
//
//        	DefactoServer.log.log(Level.WARNING, "Error while processing <" + subject + ": \""+slabel+"\", " + property + ", " + object + ": \""+olabel+"\">");
//            DefactoServer.log.log(Level.WARNING, e.getMessage());
//            e.printStackTrace();
//        }
        return Response.serverError().build();
    }
}
