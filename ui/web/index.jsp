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
              background-color: #ff0000;
              border:1px solid #CCC
          }

          .highlight {
              padding:0px 3px;
              margin:0 -4px;
          }
      </style>


  </head>
  <body>

  Search: <input type="text" id="text-search" />

  <p><b>Web development</b> is a broad term for the work involved in developing a <a href="/wiki/Web_site" title="Web site" class="mw-redirect">web site</a> for the Internet (<a href="/wiki/World_Wide_Web" title="World Wide Web">World Wide Web</a>) or an <a href="/wiki/Intranet" title="Intranet">intranet</a> (a private network). This can include <a href="/wiki/Web_design" title="Web design">web design</a>, <a href="/wiki/Web_content_development" title="Web content development" class="mw-redirect">web content development</a>, client liaison, <a href="/wiki/Client-side_scripting" title="Client-side scripting">client-side</a>/<a href="/wiki/Server-side_scripting" title="Server-side scripting">server-side</a> <a href="/wiki/Programming" title="Programming" class="mw-redirect">scripting</a>, <a href="/wiki/Web_server" title="Web server">web server</a> and <a href="/wiki/Network_security" title="Network security">network security</a> configuration, and <a href="/wiki/E-commerce" title="E-commerce" class="mw-redirect">e-commerce</a> development. However, among web professionals, "web development" usually refers to the main non-design aspects of building web sites: writing <a href="/wiki/Markup_language" title="Markup language">markup</a> and <a href="/wiki/Computer_programming" title="Computer programming">coding</a>. Web development can range from developing the simplest static single page of <a href="/wiki/Plain_text" title="Plain text">plain text</a> to the most complex web-based <a href="/wiki/Internet_application" title="Internet application" class="mw-redirect">internet applications</a>, <a href="/wiki/Electronic_business" title="Electronic business">electronic businesses</a>, or <a href="/wiki/Social_network_service" title="Social network service">social network services</a>.</p>
  <p>For larger organizations and businesses, web development teams can consist of hundreds of people (<a href="/wiki/Web_developer" title="Web developer">web developers</a>). Smaller organizations may only require a single permanent or contracting <a href="/wiki/Webmaster" title="Webmaster">webmaster</a>, or secondary assignment to related job positions such as a <a href="/wiki/Graphic_designer" title="Graphic designer">graphic designer</a> and/or <a href="/wiki/Information_systems" title="Information systems" class="mw-redirect">information systems</a> technician. Web development may be a collaborative effort between departments rather than the domain of a designated department.</p>

  (Text from <a href="http://en.wikipedia.org/wiki/Web_development">Wikipedia</a>)

  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
  <script type="text/javascript" src="js/highlight.js"></script>
  <script type="text/javascript">
      $(function() {
          $('#text-search').bind('keyup change', function(ev) {
              // pull in the new value
              var searchTerm = $(this).val();

              // remove any old highlighted terms
              $('body').removeHighlight();

              // disable highlighting if empty
              if ( searchTerm ) {
                  // highlight the new term
                  $('body').highlight( searchTerm );
                  $('body').highlight( 'can' );
                  $('body').highlight( 'may' );
                  $('body').highlight( 'be' );
              }
          });
      });
  </script>


  </body>
</html>