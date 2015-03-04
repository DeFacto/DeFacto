package org.aksw.defacto;

import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;

import java.util.Set;

/**
 * Created by root on 2/22/15.
 */
public class temp {

   public static void main(String[] args){

      try{

         Set<FactBenchExample> examples = FactBenchExamplesLoader.loadExamples();

      }catch(Exception e){
         System.out.println(e.toString());
      }

   }
}
