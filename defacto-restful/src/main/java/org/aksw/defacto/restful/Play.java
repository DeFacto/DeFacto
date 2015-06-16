package org.aksw.defacto.restful;

import java.util.Calendar;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.restful.core.DummyData;
import org.aksw.defacto.util.AGDISTIS;
import org.aksw.defacto.util.AGDISTISResult;
import org.aksw.defacto.util.EvidenceRDFGenerator;
import org.ini4j.Ini;

public class Play {
    public static String test = "";

    public static void main(String[] args) throws Exception {
        Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(Defacto.class.getClassLoader().getResourceAsStream("defacto.ini")));

        final DefactoModel model = DummyData.getEinsteinModel();

        Calendar startTime = Calendar.getInstance();
        final Evidence evidence = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.NO);
        Calendar endTime = Calendar.getInstance();

        System.out.println(evidence.getDeFactoScore());
        System.out.println(evidence.defactoTimePeriod.getFrom());

        System.out.println(EvidenceRDFGenerator.getProvenanceInformationAsString(DummyData.getDummyTriple(), evidence, startTime, endTime, "TURTLE"));
    }

    public static void main2(String[] args) {

        try {
            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(Defacto.class.getClassLoader().getResourceAsStream("defacto.ini")));

            AGDISTISResult result = AGDISTIS.disambiguate("Albert Einstein", "Nobel Prize");
            System.out.println(result);

            final DefactoModel model = DummyData.getEinsteinModel();

            // this is actually a dummy call of DeFacto
            /*
            final Pair<DefactoModel, Evidence> evidence = DummyData.createDummyData(5);
            */

            // TODO call DeFacto properly
            Calendar startTime = Calendar.getInstance();
            final Evidence evidence = Defacto.checkFact(model, Defacto.TIME_DISTRIBUTION_ONLY.NO);
            Calendar endTime = Calendar.getInstance();

            if (evidence != null) {
                // System.out.println(evidence.getDeFactoScore());
                test = "" + evidence.getDeFactoScore();
                System.out.println(evidence.defactoTimePeriod.getFrom());
                test += "/n" + evidence.defactoTimePeriod.getFrom();
                // System.out.println(EvidenceRDFGenerator.getProvenanceInformationAsString(DummyData.getDummyTriple(),
                // evidence, startTime, endTime, "TURTLE"));
                test += "/n" + EvidenceRDFGenerator.getProvenanceInformationAsString(DummyData.getDummyTriple(), evidence, startTime, endTime, "TURTLE");
            } else {
                System.out.println("Error to load the evidence :-( ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
