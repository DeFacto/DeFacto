<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.boilerpipe.BoilerPipeCaller" %>
<%--
  Created by IntelliJ IDEA.
  User: mohamed
  Date: 2/15/12
  Time: 11:48 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>


    <style type="text/css">
        .highlight {
            background-color: #13ffab;
            border: 1px solid #CCC
        }

        .highlight {
            padding: 0px 3px;
            margin: 0 -4px;
        }
    </style>


</head>
<body>



<%

    String strPageHTML = BoilerPipeCaller.getCleanHTMLPage(request.getParameter("url"));

    //If the BoilerPipe returns the body text with no problem, I render it directly
    if(strPageHTML.compareTo("") != 0)
        out.println(strPageHTML);
    else{ //In case it fails, we render the whole page as is
        URL url = new URL(request.getParameter("url"));
        URLConnection con = url.openConnection();
        con.connect();
        InputStream is = (InputStream) con.getContent();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while (line != null) {
            out.println(line);
            line = br.readLine();
        }
        br.close();

    }

%>


<%
    //response.getWriter().write(request.getParameter("url"));
    /*URL l_URL = new URL(request.getParameter("url"));
    BufferedReader l_Reader = new BufferedReader( new InputStreamReader( l_URL.openStream()));

    String l_InputLine = null ;
    while ((l_InputLine = l_Reader.readLine()) != null)
        out.println( l_InputLine );

    l_Reader.close();*/
%>

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
        <%
        ArrayList<String> arrKeywords = (ArrayList<String>) session.getAttribute("keywordList");
        for(int i=0; i < arrKeywords.size(); i++)
        {
        %>
        $('body').highlight( '<%=arrKeywords.get(i)%>' );
        <%
        }
        %>
        $('body').highlight( 'framework' );
    });

</script>


</body>
</html>