import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.*;

public class OpenIEDemo {

  public static void main(String[] args) throws Exception {

    filename = args[0]
    data = readFile(filename);
    Document doc = new Document(data);

    for (Sentence sent : doc.sentences()) {
      for (RelationTriple triple : sent.openieTriples()) {
        System.out.println(triple.confidence + "\t" +
            triple.subjectLemmaGloss() + "\t" +
            triple.relationLemmaGloss() + "\t" +
            triple.objectLemmaGloss());
      }
    }
  }

  private String readFile(String filename){
  List<String> records = new ArrayList<String>();
  try
  {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line;
    String document;
    while ((line = reader.readLine()) != null)
    {
      records.add(line);
      document = document + ' ' + line
    }
    reader.close();
    return document;
  }
  catch (Exception e)
  {
    System.err.format("Exception occurred trying to read '%s'.", filename);
    e.printStackTrace();
    return null;
  }
}

public void toFile(String data, String out)
  throws IOException {
    FileOutputStream outputStream = new FileOutputStream(out);
    byte[] strToBytes = str.getBytes();
    outputStream.write(strToBytes);
    outputStream.close();
}


}