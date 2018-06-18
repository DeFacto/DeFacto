package org.aksw.defacto.evaluation;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class IndexInspection {

	public static void main(String[] args) throws IOException  {
		
		System.out.println("EN: " +  getNumDocs("/Users/gerb/Development/workspaces/experimental/boa/defacto/en/index/corpus"));
		System.out.println("DE: " +  getNumDocs("/Users/gerb/Development/workspaces/experimental/boa/qa/de/index/corpus"));
		System.out.println("FR: " +  getNumDocs("/Users/gerb/Development/workspaces/experimental/boa/defacto/fr/index/corpus"));
	}

	private static int getNumDocs(String string) throws IOException {

		IndexReader reader = IndexReader.open(FSDirectory.open(new File(string)));
		return reader.numDocs();
	}
}
