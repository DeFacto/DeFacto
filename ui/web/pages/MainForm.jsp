<%@ page import="org.aksw.InformationFinder.TripleResultFinder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.aksw.results.SearchResult" %>
<%--
  Created by IntelliJ IDEA.
  User: mohamed
  Date: 2/16/12
  Time: 3:27 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>



<html>
<head>
    <title>AKSW Data Provenance Finder</title>

    <style type="text/css">
        .highlight {
            background-color: #ff0000;
            border: 1px solid #CCC
        }

        .highlight {
            padding: 0px 3px;
            margin: 0 -4px;
        }
    </style>

</head>
<body>

<%! ArrayList<SearchResult> arrSearchResults = new ArrayList<SearchResult>();%>

<%!  private void getData(String strRequiredTriple, String strBoaConfidenceThreshold, String strMaxSearchResults,
                          String strMinPageRank){

    if((strRequiredTriple == null) || (strRequiredTriple == ""))
        return;
//    TripleResultFinder.getCompleteSearchResults("<http://dbpedia.org/resource/C_Sharp_%28programming_language%29> <http://dbpedia.org/ontology/designer> <http://dbpedia.org/resource/DotGNU> .").size();

    double boaConfidenceThreshold = Double.parseDouble(strBoaConfidenceThreshold);
    int maxSearchResults =Integer.parseInt(strMaxSearchResults);
    int minPageRank = Integer.parseInt(strMinPageRank);

    arrSearchResults = TripleResultFinder.getCompleteSearchResults(strRequiredTriple, boaConfidenceThreshold, maxSearchResults, minPageRank);


    for(SearchResult strRes:arrSearchResults){
        System.out.println("RESULT = " + strRes);
    }
}
%>


<%
    //    private void viewWebsite(){
    String option=request.getParameter("lstResult");
    //if(option!=null)
       // response.getWriter().write(option);
//    }
%>


<%
    getData(request.getParameter("txtInputTriple"), request.getParameter("txtBoaConfidenceThreshold"),
            request.getParameter("txtNoSearchResultsPerQuery"), request.getParameter("txtLowestPageRank"));
    session.setAttribute("InputTriple", request.getParameter("txtInputTriple"));
    session.setAttribute("BoaConfidenceThreshold", request.getParameter("txtBoaConfidenceThreshold"));
    session.setAttribute("lowestPageRank", request.getParameter("txtLowestPageRank"));
    session.setAttribute("noSearchResultsPerQuery", request.getParameter("txtNoSearchResultsPerQuery"));

    session.setAttribute("keywordList", TripleResultFinder.getListOfKeywords());

    ArrayList<String> lst = (ArrayList<String>) session.getAttribute("keywordList");
//    response.getWriter().write(lst.size());

//    session.setAttribute("berlin", "berlin");

%>

<span></span>
<form action="MainForm.jsp" METHOD="post" name="frmInformationFinder"  >

    <!--input type="text" name="txtHidden" style="visibility: hidden;"  value="Hello"/-->
    <table border="0" style="width: 100%">
        <tbody>
        <tr>
            <td style="width: 15%;">BOA Confidence Threshold<br/>
            </td>
            <td style="width: 70%"><input type="text" name="txtBoaConfidenceThreshold" style="width: 10%"  value="<%=session.getAttribute("BoaConfidenceThreshold") == null? "0.9" : session.getAttribute("BoaConfidenceThreshold")%>" /><br/>
            </td>
            <td>
            </td>
        </tr>

        <tr>
            <td style="width: 15%;">Lowest PageRank<br/>
            </td>
            <td style="width: 70%"><input type="text" name="txtLowestPageRank" style="width: 10%"  value="<%=session.getAttribute("lowestPageRank") == null? "4" : session.getAttribute("lowestPageRank")%>" /><br/>
            </td>
            <td>
            </td>
        </tr>

        <tr>
            <td style="width: 15%;">No. of Search Result per Query<br/>
            </td>
            <td style="width: 70%"><input type="text" name="txtNoSearchResultsPerQuery" style="width: 10%"  value="<%=session.getAttribute("noSearchResultsPerQuery") == null? "25" : session.getAttribute("noSearchResultsPerQuery")%>" /><br/>
            </td>
            <td>
            </td>
        </tr>

        <tr>
            <td style="width: 15%;">Triple<br/>
            </td>
            <td style="width: 70%"><input type="text" name="txtInputTriple" style="width: 90%"  value="<%=session.getAttribute("InputTriple") == null? "" : session.getAttribute("InputTriple")%>" /><br/>
            </td>
            <td><input type="submit" name="btnGetResults" value="Get Results" />
            </td>
        </tr>
        <tr>

            <td style="width: 15%;"> <label for="lstResult">Results</label><br />
            </td>
            <td style="width: 70%">
                <select id="lstResult" name="lstResult"  size="5" style="width: 90%" >
                    <!--option value="0">Test 0</option>
                    <option value="1">Test 1</option>
                    <option value="2">Test 2</option-->
                    <%
                        for(int i = 0; i < arrSearchResults.size(); i++){
//                            response.getWriter().write(arrSearchResults.get(i));

                    %>
                    <OPTION value="<%= i%>"><%=arrSearchResults.get(i).getTitle() %></OPTION>
                    <%
                        }
                    %>
                </select>
                <br />
            </td>
            <td><input type="button" name="btnViewWebsite" value="View Website" onclick="openWebsite()"/><br />
            </td>
        </tr>
        <tr>
            <td><br />
            </td>
            <td><br />
            </td>
            <td><br />
            </td>
        </tr>
        </tbody>
    </table>
</form>


<script type="text/javascript">
    function openWebsite()
    {

        <%--var testVar = '<%=session.getAttribute("InputTriple")%>';--%>
//        alert(testVar);
//        var value1 = document.frmInformationFinder.lstResult.options[document.frmInformationFinder.lstResult.selectedIndex].text;
        var URLDisplayPage = "ResultingWebsite.jsp?url=" + document.frmInformationFinder.lstResult.options[document.frmInformationFinder.lstResult.selectedIndex].text;

        window.open(URLDisplayPage);

    }
</script>


<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
<script type="text/javascript" src="/highlight.js"></script>
<script type="text/javascript">
    /*$(function() {
        $('#document').bind('ready', function(ev) {
            alert("this will flre when the DOM is loaded.");
            // pull in the new value
//                var searchTerm = $(this).val();

            // remove any old highlighted terms
            $('body').removeHighlight();

            // disable highlighting if empty
//                if ( searchTerm ) {
            // highlight the new term
//                    $('body').highlight( searchTerm );
            $('body').highlight( 'Hello' );
//                }
        });
    });*/

    $(document).ready(function(){
        //insert code here
//        alert("this will flre when the DOM is loaded.");
        $('body').removeHighlight();

        // disable highlighting if empty
//                if ( searchTerm ) {
        // highlight the new term
//                    $('body').highlight( searchTerm );
        //$('body').highlight( 'world' );
    });

</script>



</body>
</html>