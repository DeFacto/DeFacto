import com.boilerpipe.BoilerPipeCaller;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
//import org.wikipedia.WikipediaSearcher;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 1/22/12
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private static Logger logger = null;
    static {
        logger = Logger.getLogger(Main.class.getName());
    }

    // target GSA's hostname
    private static final String HOSTNAME = "google3";

    // query string to search for
    private static final String QUERY_STRING = "java sdk";

    // The value for the frontend configured for the GSA
    // (If you dont know this, ask GSA admin for correct value for your target GSA.)
    private static final String SETTING_FRONTEND = "default_frontend";

    public static void main (String[] args) {

        String google = "";
        String searchTriple = "";
        String charset = "";
        int Start = 0;
        try{


//            PurifyrContacter.GetCleanHTMLPage("http://www.codeproject.com/Articles/329742/Cache-IQueryable-for-Better-Linq-to-SQL-Performanc");
            String outputHTML = BoilerPipeCaller.getCleanHTMLPage("http://www.codeproject.com/Articles/329742/Cache-IQueryable-for-Better-Linq-to-SQL-Performanc");
//            logger.info(outputHTML);

            outputHTML = Jsoup.parse(outputHTML).text();
            logger.info(outputHTML);

            //PageRank.GetPageRank("http://en.wikipedia.org/wiki/Berlin");

            /*
            // Open the file that is the first
            // command line parameter
            FileInputStream fileInputStream = new FileInputStream("/home/mohamed/LeipzigUniversity/JavaProjects/GoogleSearchInitiator/search_triples.txt");
            // Get the object of DataInputStream
            DataInputStream inputTriplesDataStream = new DataInputStream(fileInputStream);
            BufferedReader inputDataBufferedReader = new BufferedReader(new InputStreamReader(inputTriplesDataStream));

            BufferedWriter outSearchResults = new BufferedWriter(new FileWriter("/home/mohamed/LeipzigUniversity/JavaProjects/GoogleSearchInitiator/search_results.txt"));

            String strLine;
            //Read File Line By Line
            while ((strLine = inputDataBufferedReader.readLine()) != null)   {
                searchTriple = strLine;

                outSearchResults.write(searchTriple + "\n");

                 ArrayList<SearchResult> searchResultURLsList = TripleResultFinder.getCompleteSearchResults(searchTriple);


                //Write the search results to the output file
                for(SearchResult urlResult:searchResultURLsList){
                    outSearchResults.write(urlResult.getUrl() + "\n");
                }
                outSearchResults.write("///////////////////////////////////////////////\n");
            }
            //Close the input stream
            inputTriplesDataStream.close();

            outSearchResults.close();
            */

            // Show title and URL of 1st result.
        }
        catch (Exception exp){
            System.out.println("Start = " + Start);
            System.out.println("An error occurred due to " + exp.getStackTrace());

        }

    }




}







//        System.out.print("Hello World");
//        SPARULFormulator.deleteFromGraph("/home/mohamed/LeipzigUniversity/dump.nt", false);
/*StringBuffer searchContents = new StringBuffer();

try {
 URL url = new URL("http://www.google.com/search?q=Egypt");
 URLConnection conn =  url.openConnection();
 conn.setRequestProperty("User-Agent",
         "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)");
 BufferedReader in = new BufferedReader(
         new InputStreamReader(conn.getInputStream())
 );
 String str;

 while ((str = in.readLine()) != null) {
     System.out.println(str);
     searchContents.append(str + "\n");
 }

 in.close();


 ////////////////////////////////Extract links///////////////////////////////////
//            Pattern p = Pattern.compile("<a +href=\"([a-zA-z0-9\\:\\-\\/\\.]+)\">");
//            Matcher m = p.matcher(searchContents.toString());
//
//            ArrayList<String> foundUrls = new ArrayList<String>();
//
//            while(m.find()) {
//                foundUrls.add(m.group(1));
//                System.out.println(m.group(1));
//            }

 File input = new File("/home/mohamed/Desktop/test2.html");
 Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

 Elements links = doc.select("a[href]"); // a with href
 Elements pngs = doc.select("img[src$=.png]");
// img with src ending .png

 for(int i=0; i<links.size(); i++){
     System.out.println(links.get(i));
 }


 ////////////////////////////////////////////////////////////////////////////////


}
catch (MalformedURLException e) {}
catch (IOException e) {}*/



//        GSAClient client = new GSAClient("my.gsa.com", "google");
//        GSAQuery query = new GSAQuery();
//
//        // typical way to generate query term.
//        GSAQuery.GSAQueryTerm term = new GSAQuery.GSAQueryTerm(QUERY_STRING);
//        query.setQueryTerm(term);
//        System.out.println("Searching for: "+query.getQueryString());
//
//        // above 2 lines may be equivalently replaced by:
//        // query.setAndQueryTerms(new String[]{QUERY_STRING});
//
//        query.setFrontend(SETTING_FRONTEND); // required!
//
//        GSAResponse response = null;
//
//        try{
//            response = client.getGSAResponse(query);
//            System.out.println("Found " + response.getNumResults() + " results");
//            List results = response.getResults();
//            System.out.println("Showing top " + results.size() + " results");
//            System.out.println("[To get more top N results, use query.setMaxResults(int)]");
//            for (int i=0, iSize=results.size(); i<iSize; i++) {
//                GSAResult result = (GSAResult) results.get(i);
//                System.out.println("--------------------------------------------------");
//                System.out.println(result.getRating() +"\t" + result.getTitle());
//                System.out.println(result.getSummary());
//                System.out.println(result.getUrl());
//            }
//        }
//        catch (IOException exp){
//            System.out.print("Thereis a probelm due to " + exp.getCause());
//
//        }
