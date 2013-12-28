<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%
		response.setContentType("text/html; charset=UTF-8");
%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" media="all" href="/osp-common-tool/css/eport.css" />
    <link href="<c:out value="${sakai_skin_base}"/>"
          type="text/css"
          rel="stylesheet"
          media="all" />
    <link href="<c:out value="${sakai_skin}"/>"
          type="text/css"
          rel="stylesheet"
          media="all" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <title><%= org.sakaiproject.tool.cover.ToolManager.getCurrentTool().getTitle()%></title>
    <script type="text/javascript"  src="/library/js/headscripts.js">
    </script>
    <script type="text/javascript" src="/osp-common-tool/js/eport.js"></script>
    	<script type="text/javascript" language="JavaScript" src="/library/js/jquery-ui-latest/js/jquery.min.js">
		</script>
  <%
      String panelId = request.getParameter("panel");
      if (panelId == null) {
         panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
      }

  %>

  <script type="text/javascript">
   function resetHeight() {
      setMainFrameHeight('<%= org.sakaiproject.util.Validator.escapeJavascript(panelId)%>');
   }
   
	function resize(){
		mySetMainFrameHeightMatrix('<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>');
	}
   
   function mySetMainFrameHeightMatrix(id)
   {
   	// run the script only if this window's name matches the id parameter
   	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
   	if (typeof window.name != "undefined" && id != window.name) return;

   	var frame = parent.document.getElementById(id);
   	if (frame)
   	{

   		var objToResize = (frame.style) ? frame.style : frame;
     
       // SAK-11014 revert           if ( false ) {

   		var height; 		
   		var offsetH = document.body.offsetHeight;
   		var innerDocScrollH = null;

   		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
   		{
   			// very special way to get the height from IE on Windows!
   			// note that the above special way of testing for undefined variables is necessary for older browsers
   			// (IE 5.5 Mac) to not choke on the undefined variables.
    			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
   			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
   		}
   	
   		if (document.all && innerDocScrollH != null)
   		{
   			// IE on Windows only
   			height = innerDocScrollH;
   		}
   		else
   		{
   			// every other browser!
   			height = offsetH;
   		}
      // SAK-11014 revert		} 

      // SAK-11014 revert             var height = getFrameHeight(frame);

   		// here we fudge to get a little bigger
   		var newHeight = height + 40;

   		// but not too big!
   		if (newHeight > 32760) newHeight = 32760;

   		// capture my current scroll position
   		var scroll = findScroll();

   		// resize parent frame (this resets the scroll as well)
   		objToResize.height=newHeight + "px";

   		// reset the scroll, unless it was y=0)
   		if (scroll[1] > 0)
   		{
   			var position = findPosition(frame);
   			parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
   		}
   	}
   }

   function loaded() {
      resetHeight();
      parent.updCourier(doubleDeep, ignoreCourier);
      if (parent.resetHeight) {
         parent.resetHeight();
      }
   }
   
   iframeId = '<%= org.sakaiproject.util.Validator.escapeJavascript(panelId)%>';
   
   urlPrefix = '/tool/<%=org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId()%>';
   
  </script>
  <%= request.getAttribute("editorHeadScript") %>
  
  </head>
  <body onload="loaded();">
      <div class="portletBody">
         <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>
             <jsp:include page="<%= (String)request.getAttribute(\"_body\")%>" /> 
         <c:if test="${not empty requestScope.panelId}"></div></c:if>
      </div>
   </body>
</html>
