<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="request"><jsp:setProperty name="msgs" property="baseName" value="org.theospi.portfolio.presentation.bundle.Messages"/></jsp:useBean>
<fmt:setLocale value="${locale}" />
<fmt:setBundle basename="org.theospi.portfolio.presentation.bundle.Messages" />

<osp-c:authZMap prefix="osp.presentation." var="can" />
<c:set var="showCreate" value="${can.create && createAvailable}" />
<c:set var="canReviewInSite" value="${can.review}" />

<script type="text/javascript" src="/library/js/jquery-ui-latest/js/jquery.min.js">
</script>
<script type="text/javascript" src="/library/js/jquery-ui-latest/js/jquery-ui.min.js">
</script>

<script type="text/javascript">
    $(document).ready(function(){
	  $('.ospTable tr:even td').addClass('even');
	  
	  checkContinueButton();
	});


	var ospSelectedPresentations = new Object();
	var ospPresentationOwners = new Object();
	var ospDialogWidth = 800;
	var ospDialogHeight = 620;
	var oldOspFrameHeight = ospDialogHeight;
	
	function isObjectPropertiesEmpty(objects) {
	    for (var key in objects) {
			return false;
		}

		return true;
	}

	function checkContinueButton() {
	    var continueButton = jQuery("#continue_button");

		if (continueButton.length == 1) {
	        if (isObjectPropertiesEmpty(ospSelectedPresentations)) {
		        continueButton.attr('disabled', true);
	        }
	        else {
		        continueButton.attr('disabled', false);
	        }
	        
	        continueButton.show();
		}
	}
	
	function requestOnchange(checkbox, presentationId, presentationName) {
/*		var params = { id : presentationId };

		if (checkbox == null || presentationId == null) {
		    return;
		}
		
        if (checkbox.checked) {
		    params["requestAccess"] = true;
			}
		else {
			params["requestAccess"] = false;
		}

		$.post('updatePresentation.osp', params );
*/

        if (checkbox == null || presentationId == null) {
            return;
        }

        if (checkbox.checked) {
            ospSelectedPresentations[presentationId] = new Object();
            ospSelectedPresentations[presentationId].pid = presentationId;
            ospSelectedPresentations[presentationId].name = presentationName;
            
	    }
        else {
            if (presentationId in ospSelectedPresentations) {
                delete ospSelectedPresentations[presentationId];
            }
        }

		checkContinueButton();
	}
	
	function openSearchDialog() {
        var searchDialog = jQuery("#searchDialog");

        var dialogRequestedConfirmationText="<fmt:message key='share_request.requested'/>";

        var dialogTitle="<fmt:message key='share_request.dialog.title'/>";
        var dialogIntroMessage="<fmt:message key='share_request.dialog.intro_message'/>";
        var dialogShowPresentationRequests="<fmt:message key='share_request.dialog.show_presentation_requests'/>";
        var dialogSampleMessageText="<fmt:message key='share_request.dialog.sample_message_text'/>";
        var dialogTo="<fmt:message key='share_request.dialog.to'/>";
        var dialogMessageText="<fmt:message key='share_request.dialog.message_text'/>";
        <c:set var="selected_presentation_name">[<fmt:message key='share_request.dialog.selected_presentation_name' />]</c:set>
        <c:set var="selected_presentation_sitename">[<fmt:message key='share_request.dialog.selected_presentation_sitename' />]</c:set>
        var dialogMessageTextContent="<fmt:message key='share_request.dialog.message_text_content_alternate'>
    	                                  <c:if test='${! empty flname}'>
                                              <fmt:param value='${flname}'/>
                                              <fmt:param value="${selected_presentation_name}"/>
                                              <fmt:param value="${selected_presentation_sitename}"/>
                                          </c:if>
                                     </fmt:message>";
        var dialogClosingAndSignature="<fmt:message key='share_request.dialog.closing_and_signature'/>";
        var dialogPersonalMessage="<fmt:message key='share_request.dialog.personal_message'/>";
        
	    var ownerNames = "";
	    var presentationNames = "";
	    var hiddenFields = "";
	    
	    var usedUsers = new Object();

	    var i=1;
	    for (var presentationId in ospSelectedPresentations) {
		    if (! (ospPresentationOwners[presentationId].displayName in usedUsers)) {
		        usedUsers[ospPresentationOwners[presentationId].displayName] = true;
		        
			    if (ownerNames != "") {
			        ownerNames = ownerNames + ", " + ospPresentationOwners[presentationId].displayName;
			    }
			    else {
			        ownerNames = ospPresentationOwners[presentationId].displayName;
			    }
		    }

		    if (presentationNames != "") {
		    	presentationNames = presentationNames + ", " + ospSelectedPresentations[presentationId].name + 
		    	                    " (" + ospPresentationOwners[presentationId].displayName + ")";
		    }
		    else {
		    	presentationNames = ospSelectedPresentations[presentationId].name + " (" + ospPresentationOwners[presentationId].displayName + ")";
		    }

		    hiddenFields = hiddenFields + "<input class='hiddenFieldsDialogForm' name='presentation" + i + "' type='hidden' value='" + presentationId + "'/>\n"; 
		    i++;
	    }


        var message = "<form id='dialogForm' action='<osp:url value='requestPresentation.osp'/>&requestAccess=1' method='post'>\n" + 
                        "<p style='font-weight:bold;'>" + dialogShowPresentationRequests  + "</p>\n<p>" + presentationNames + "</p>\n" +  
                        "<p>" + dialogIntroMessage + "\n</p>" + 
                        "<p style='font-weight:bold;'>" + dialogSampleMessageText + "</p>\n" + 
                        "<blockquote>\n <div class='presentationRequestDialogGray'> " + 
                        "<p>" + dialogTo + ": " + ownerNames + "<p>\n" + 
                        "<p>" + dialogMessageTextContent + "</p>\n" + 
                        "<p>&nbsp;</p>\n" + 
                        "<p>" + dialogClosingAndSignature + "</p>\n" + 
                        "</div>\n " +
                        "<p style='font-weight:bold;'>" + dialogPersonalMessage + "</p>\n" +
                        "<textarea id='customMessage' name='customMessage' cols='80' rows='5'></textarea>\n" + 
                        hiddenFields + "\n" +
                        "</blockquote>\n " +
                      "</form>\n";
                                            
		var dialogOpts = {
				        open: function() {
				        	jQuery("#requestAccessConfirmation").hide();
					        resizeOspFrameForDialog(true);
				        },
				        close: function() {
					        resizeOspFrameForDialog(false);
				        },
				        beforeclose: function() {
				        },
				        autoOpen: false,
				        modal: true,
				        title: dialogTitle,
				        width: "auto",
				        height: "auto",
				        draggable: false,
				        resizable: false,
				        buttons: { 
				                   "Cancel": function() { $(this).dialog("close"); }, 
				                   "Send Request": function() {
					                                   var dialogForm = jQuery("#dialogForm");
                                                       var textArea = jQuery("#customMessage");
                                                       var hiddenFields = jQuery(".hiddenFieldsDialogForm");
                                                       var serializedData = dialogForm.serialize();

					                                   $.ajax({  
					                                	   type: "POST",  
					                                	   url: dialogForm.attr("action"),  
					                                	   data: serializedData,
					                                	   success: function() {  
					                                	     }
					                                   });  
					                                	 
                                                       $(this).dialog("close");

                                                       var requestAccessConfirmationDiv = jQuery("#requestAccessConfirmation");

                                                       requestAccessConfirmationDiv.html(dialogRequestedConfirmationText);
                                                       requestAccessConfirmationDiv.show();

                                                       var checkedCheckboxes = jQuery(".searchCheckbox:checked");
                                                       checkedCheckboxes.each(function(index) {
                                                                                  $(this).attr('checked', false);
                                                       });

                                                   	   ospSelectedPresentations = new Object();
                                                    

                                               	       var continueButton = jQuery("#continue_button");
                                            		   continueButton.attr('disabled', true);
                                                   }
                			     }
				    };

	    searchDialog.html(message);
        searchDialog.dialog(dialogOpts);
        searchDialog.dialog('open');
	}

	function resizeOspFrameForDialog(isOpen) {
        var searchDialogHeight = jQuery("#searchDialog").height();
        var iframe = jQuery('iframe', window.parent.document);
        var body = jQuery('body', window.parent.document);

        var padding = 100;

        if (isOpen) {
        	oldOspFrameHeight = iframe.height();
        	jQuery('iframe', window.parent.document).width(iframe.width());
	 	    jQuery('iframe', window.parent.document).height(iframe.height() + padding);
        }
        else {
	 	    jQuery('iframe', window.parent.document).height(oldOspFrameHeight);
        }
	}
	
</script>
<link rel="stylesheet" type="text/css" href="/library/skin/tool_base.css" />
<link rel="stylesheet" type="text/css" href="/library/js/jquery-ui-latest/css/ui-lightness/jquery-ui.css" />
<link rel="stylesheet" type="text/css" href="/osp-common-tool/css/presentationDialogText.css" />
<!--[if gt IE 5.0]><![if lt IE 7]>
<style type="text/css">
/* that IE 5+ conditional comment makes this only visible in IE 5+*/ 
ul.makeMenu{border:none}
ul.makeMenu ul {  /* copy of above declaration without the > selector */                                                                      
  display: none; position: absolute; top: 18px; left: 0px; background-image:none; background-color:#ffffff;width: 8em; z-index:3;border:1px solid  #666  	
}
ul.makeMenu ul li {display:block}
ul.makeMenu{width:5em !important}

.attach{width:1em !important;}
</style>
<![endif]><![endif]-->
<!--[if gt IE 6.0]>
<style type="text/css">
ul.makeMenu li {
  list-style-type: none; 
  margin: 0px; 
  padding:5px; 
  position: relative;
  color: #000000;
  display:inline;
}

.menuOpen{margin:2px !important;padding:2px !important;background:#fff;width:6em;border-width:1px 1px 1px 0;border-style:solid;border-color: #ccc;}
.menuOpen:hover{border-width:1px 1px 1px 0;border-style:solid;border-color: #ccc;;background:#fff}
ul.makeMenu{border:none !important;background:transparent;border:1px solid #ccc !important;margin:2px;padding:2px;padding-left:5px;width:3em;}
ul.makeMenu:hover{border:none !important; background:transparent}
ul.makeMenu ul{top: 15px !important;left:0}
.attach{width:1em !important;}
ul.makeMenu{border:none !important}
</style>
<![endif]-->

<style>

div.filterrow span.filterleft {
  float: left;
  text-align: left;
  width: 25%;
  }

div.filterrow span.filterright {
  float: left;
  text-align: left;
  }
  
ul.makeMenu li:hover > ul {
    display: none;
    margin-top: 0;
}

.menuOpen {
    cursor: pointer;
}

ul.makeMenu li > ul {
    top: auto;
}  
</style>
	
<script  type ="text/javascript">
$(document).ready(function() {
    jQuery('body').click(function(e) { 
        
        if ( e.target.className !='menuOpen' &&e.target.className !='dropdn'  ){
            $('.makeMenuChild').fadeOut();
        }
        else {
            if( e.target.className =='dropdn' ) {
                $('.makeMenuChild').hide();
                $(e.target).parent('li').find('ul').show().find('li:first a').focus();

            }
            else {
                $('.makeMenuChild').hide();
                $(e.target).find('ul').show().find('li:first a').focus();
            }
        }
    });
});
</script>

<ul class="navIntraTool">
    <div id="requestAccessConfirmation" class="success" style="display: none;"></div>
    <c:if test="${! empty requestAccessMessage}">
        <span class="success"><c:out value="${requestAccessMessage}"/></span>
    </c:if>
    <c:if test="${showCreate}">
        <li class="firstToolBarItem"><span><a href="<osp:url value="createPresentation.osp"/>"
            title='<c:out value="${msgs.action_new_portfolio}"/>'> <c:out value="${msgs.action_new_portfolio}"/> </a></span></li>
    </c:if>
    
    <c:if test="${isMaintainer}">
        <li><span><a href="<osp:url value="osp.permissions.helper/editPermissions">
                <osp:param name="message"><fmt:message key="message_permissionsEdit">
                 <fmt:param><c:out value="${tool.title}"/></fmt:param>
                 <fmt:param><c:out value="${worksite.title}"/></fmt:param>
                </fmt:message>
                </osp:param>
                <osp:param name="name" value="presentation"/>
                <osp:param name="qualifier" value="${worksite.id}"/>
                <osp:param name="returnView" value="listPresentationRedirect"/>
                <osp:param name="session.${lastViewKey}" value="/listPresentation.osp"/>
                </osp:url>"
            title='<c:out value="${msgs.action_permissions_title}"/>'> <c:out value="${msgs.action_permissions}"/> </a></span></li>
    </c:if>
</ul>

<ul class="tabNav specialLink">
    <c:choose>
      <c:when test="${filterList != 'mine'}">
          <li><a href="<osp:url value="listPresentation.osp"/>&filterList=mine"><c:out value="${msgs.action_filter_mine}"/></a></li>
      </c:when>
      <c:otherwise>
      	<li class="selected"><span><c:out value="${msgs.action_filter_mine}"/></span></li>
			</c:otherwise>
    </c:choose>
    
    <c:choose>
      <c:when test="${filterList != 'shared'}">
         <li><a href="<osp:url value="listPresentation.osp"/>&filterList=shared"><c:out value="${msgs.action_filter_shared}"/></a></li>
      </c:when>
      <c:otherwise>
        <li class="selected"><span><c:out value="${msgs.action_filter_shared}"/></span></li>
      </c:otherwise>
    </c:choose>
    
    <c:choose>
      <c:when test="${filterList != 'public'}">
          <li><a href="<osp:url value="listPresentation.osp"/>&filterList=public"><c:out value="${msgs.action_filter_public}"/></a></li>
      </c:when>
      <c:otherwise>
        <li class="selected"><span><c:out value="${msgs.action_filter_public}"/></span></li>
      </c:otherwise>
    </c:choose>
    <c:if test="${isSearchEnabled}">
    
        <c:choose>
    
            <c:when test="${filterList != 'search'}">
                <li><a href="<osp:url value="listPresentation.osp"/>&filterList=search"><fmt:message key="action_filter_search"/></a></li>
            </c:when>
            <c:otherwise>
                <li class="selected"><span><fmt:message key="action_filter_search"/></span></li>
            </c:otherwise>
        </c:choose>
    </c:if>
	</ul>
	<div class="tabNavPanel">
 <!-- temp separation; end of tabs -->

<div class="filterrow">
<span class="filterleft">

<c:if test="${filterList != 'search'}">
    <p class="smallNavIntraTool specialLink"><fmt:message key="title_show"/></p>
</c:if>
<ul class="smallNavIntraTool specialLink">
	<c:choose>
      <c:when test="${showHidden != 'visible' && filterList != 'search'}">
         <li class="firstItem"><span><a href="<osp:url value="listPresentation.osp"/>&showHiddenKey=visible&filterList=<c:out value="${filterList}"/>"><c:out value="${msgs.action_show_not_hidden}"/></a></span></li>
      </c:when>
      <c:otherwise>
         <c:if test="${filterList != 'search'}">
           <li class="firstItem"><span><c:out value="${msgs.action_show_not_hidden}"/></span></li>
         </c:if>
      </c:otherwise>
    </c:choose>
    
    <c:choose>
      <c:when test="${showHidden != 'hidden' && filterList != 'search'}">
          <li><span><a href="<osp:url value="listPresentation.osp"/>&showHiddenKey=hidden&filterList=<c:out value="${filterList}"/>"><c:out value="${msgs.action_show_hidden}"/></a></span></li>
      </c:when>
      <c:otherwise>
		<c:if test="${filterList != 'search'}">
		  <li><span><c:out value="${msgs.action_show_hidden}"/></span></li>
		</c:if>
      </c:otherwise>
    </c:choose>
    
    <c:choose>
      <c:when test="${showHidden != 'all' && filterList != 'search'}">
          <li><span><a href="<osp:url value="listPresentation.osp"/>&showHiddenKey=all&filterList=<c:out value="${filterList}"/>"><c:out value="${msgs.action_show_all}"/></a></span></li>
      </c:when>
      <c:otherwise>
		 <c:if test="${filterList != 'search'}">
		   <li><span><c:out value="${msgs.action_show_all}"/></span></li>
		 </c:if>
      </c:otherwise>
    </c:choose>
</ul>
<c:if test="${!myworkspace}">
<ul class="smallNavIntraTool specialLink">
   <c:choose>
      <c:when test="${showAllSites == 'true'}">
         <li class="firstItem"><span><a href="<osp:url value="listPresentation.osp"/>&showAllSitesKey=false&filterList=<c:out value="${filterList}"/>"><fmt:message key="action_show_thissite"/></a></span></li>
         <li><span><fmt:message key="action_show_allsites"/></span></li>
      </c:when>
      <c:otherwise>
         <li class="firstItem"><span><fmt:message key="action_show_thissite"/></span></li>
         <li><span><a onclick="jQuery('#osp_portfolio_spinner').show();" href="<osp:url value="listPresentation.osp"/>&showAllSitesKey=true&filterList=<c:out value="${filterList}"/>"><fmt:message key="action_show_allsites"/></a></span></li>
      </c:otherwise>
   </c:choose>
</ul>
</c:if>
</span>
	<c:if test="${filterList != 'mine'}">
        <span class="filterright">
			<form method="get" action="<osp:url value="listPresentation.osp"/>">
				<osp:form />
					<c:if test="${not empty userGroups && userGroupsCount > 0 && filterList != 'search'}">
						<label for="groups" class="smallNavIntraTool"><fmt:message
								key="filter_by_group" />
						</label>
						<select name="groups" id="groups"
							onchange="this.form.submit()">
							<option value="">
								<fmt:message key="select_group" />
							</option>
							<option value=""
								<c:if test="${empty filteredGroup}">selected="selected"</c:if>>
								<fmt:message key="groups_showall"></fmt:message>
							</option>
							<c:forEach var="group" items="${userGroups}">
								<option value="<c:out value="${group.id}"/>"
									<c:if test="${group.checked}"> selected="selected"</c:if>>
									<c:out value="${group.title}"></c:out>
								</option>
							</c:forEach>
							<option value="UNASSIGNED_GROUP"
								<c:if test="${filteredGroup == 'UNASSIGNED_GROUP'}">selected="selected"</c:if>>
								<fmt:message key="groups_unassigned"></fmt:message>
							</option>
						</select>
					</c:if>
			</form>
        </span>
	</c:if>
</div>
<div class="navPanel" id="NavPanelPager">
<div id="osp_portfolio_spinner" style="display: none;">
    <div>&nbsp;</div>
    <span class="smallNavIntraTool"><fmt:message key="portfolio_spinner.wait_text"/></span>
	<img src="/library/image/sakai/spinner.gif" alt='<fmt:message key="portfolio_spinner.wait_text"/>'/>
</div>
<osp:url var="listUrl" value="listPresentation.osp${pagerUrlParms}"/>
	<osp:listScroll listUrl="${listUrl}" className="listNav" />
</div>	

<c:if test="${filterList == 'search' && isSearchEnabled}">
<fmt:message key="share_request.instructions"/>
<p/>
    <form name="searchForm" id="searchForm" method="post" action="<osp:url value="listPresentation.osp"/>&filterList=search" >
        <input type="text" name="searchText" value=""/>
        <input type="submit" value="<fmt:message key="share_request.button.search"/>" />
        <c:if test="${show_members_presentations_link}">
            <span>- <fmt:message key="text_search_or"/> - 
            <c:if test="${empty memberSearch}">
                <a href="<osp:url value="listPresentation.osp"/>&filterList=search&memberSearch=1">
            </c:if>
                
            <fmt:message key="link_search_show_all_members_presentations"/>
            
            <c:if test="${empty memberSearch}">
                </a>
            </c:if>
            </span>
        
        </c:if>
    </form>
</c:if>
	

<%-- show no portfolio message if presentation list is empty --%>
<c:choose>
	<c:when  test="${empty presentations && filterList != 'search' && showHidden == 'all'}">
      <p align="center">
		<c:out value="${msgs.table_empty_list_all}"/>
      <c:if test="${filterList != 'shared' && showCreate}">
        <br/><a href="<osp:url value="createPresentation.osp"/>"
            title='<c:out value="${msgs.action_new_portfolio_now}"/>'> <c:out value="${msgs.action_new_portfolio_now}"/> </a>
      </c:if>
      </p>
	</c:when>
   
	<c:when  test="${empty presentations && filterList != 'search' && showHidden == 'hidden'}">
      <p align="center">
		<c:out value="${msgs.table_empty_list_hidden}"/>
      <c:if test="${filterList != 'shared' && showCreate}">
        <br/><a href="<osp:url value="createPresentation.osp"/>"
            title='<c:out value="${msgs.action_new_portfolio_now}"/>'> <c:out value="${msgs.action_new_portfolio_now}"/> </a>
      </c:if>
      </p>
	</c:when>
   
	<c:when  test="${empty presentations && filterList != 'search' && showHidden == 'visible'}">
      <p align="center">
		<c:out value="${msgs.table_empty_list_visible}"/>
      <c:if test="${filterList != 'shared' && showCreate}">
        <br/><a href="<osp:url value="createPresentation.osp"/>"
            title='<c:out value="${msgs.action_new_portfolio_now}"/>'> <c:out value="${msgs.action_new_portfolio_now}"/> </a>
      </c:if>
      </p>
	</c:when>

	<c:when  test="${empty presentations && filterList == 'search' && isSearchEnabled}">
      <c:choose>
	      <c:when test="${empty searchTextNotFound }">
              <p align="center">
              </p>
	      </c:when>
	      <c:otherwise>
              <p align="center">
	  	          <fmt:message key="table_empty_list_owner_search"/> <c:out value="${searchText}"/>
              </p>
          </c:otherwise>
      </c:choose>
	</c:when>
	
   <%-- Otherwise display list of portfolios --%>
	<c:otherwise>
		
	  <!--  
			reverse the sort-order of the currently sorted column for the next click.
			other columns will sort ascending if they are clicked. Also, set the class
			of the th-element to allow the presentation to show the direction of sorting
		-->
	  <c:choose>
			<c:when test="${sortOrderIsAscending}">
				 <c:set var="sortDirImageUrl" value="/library/image/sakai/sortascending.gif" />
				 <c:set var="sortorder" value="descending" /><%-- toggle --%>
			</c:when>
			<c:otherwise>
				 <c:set var="sortDirImageUrl" value="/library/image/sakai/sortdescending.gif" />
				 <c:set var="sortorder" value="ascending" /> <%-- toggle --%>
			</c:otherwise>
	  </c:choose>
     
		<c:choose>
			<c:when test="${sortOn eq 'name'}">
				 <c:set var="sortorder_name" value="${sortorder}" />
				 <c:set var="className_name" value="${sortOrderIsAscending ? 'SortedAscending' : 'SortedDescending'}" />
			</c:when>
			<c:otherwise>
				 <c:set var="sortorder_name" value="ascending" />
				 <c:set var="className_name" value="Unsorted" />
			</c:otherwise>
	  </c:choose>
	  <c:choose>
			<c:when test="${sortOn eq 'dateModified'}">
				 <c:set var="sortorder_dateModified" value="${sortorder}" />
				 <c:set var="className_dateModified" value="${sortOrderIsAscending ? 'SortedAscending' : 'SortedDescending'}" />
			</c:when>
			<c:otherwise>
				 <c:set var="sortorder_dateModified" value="descending" />
				 <c:set var="className_dateModified" value="Unsorted" />
			</c:otherwise>
	  </c:choose>
	  <c:choose>
			<c:when test="${sortOn eq 'owner'}">
				 <c:set var="sortorder_owner" value="${sortorder}" />
				 <c:set var="className_owner" value="${sortOrderIsAscending ? 'SortedAscending' : 'SortedDescending'}" />
			</c:when>
			<c:otherwise>
				 <c:set var="sortorder_owner" value="ascending" />
				 <c:set var="className_owner" value="Unsorted" />
			</c:otherwise>
	  </c:choose>
	  <c:choose>
			<c:when test="${sortOn eq 'reviewed'}">
				 <c:set var="sortorder_reviewed" value="${sortorder}" />
				 <c:set var="className_reviewed" value="${sortOrderIsAscending ? 'SortedAscending' : 'SortedDescending'}" />
			</c:when>
			<c:otherwise>
				 <c:set var="sortorder_reviewed" value="ascending" />
				 <c:set var="className_reviewed" value="Unsorted" />
			</c:otherwise>
	  </c:choose>
	  <c:choose>
			<c:when test="${sortOn eq 'worksite'}">
				 <c:set var="sortorder_worksite" value="${sortorder}" />
				 <c:set var="className_worksite" value="${sortOrderIsAscending ? 'SortedAscending' : 'SortedDescending'}" />
			</c:when>
			<c:otherwise>
				 <c:set var="sortorder_worksite" value="ascending" />
				 <c:set var="className_worksite" value="Unsorted" />
			</c:otherwise>
	  </c:choose>
     

	<table class="listHier ospTable" cellspacing="0" cellpadding="0"  border="0" summary="<c:out value="${msgs. table_presentationManager_summary}" />" >
	   <thead>
		  <tr>
		   <c:if test="${filterList == 'search' && isSearchEnabled}">
			<th scope="col">
				<fmt:message key="table_header_requestAccess" /> 
		    </th>
		   </c:if>
		    
			<th scope="col" class="${className_name}">
				<a href="<osp:url value="listPresentation.osp">
								<osp:param name="sortOn" value="name"/>
								<osp:param name="sortorder" value="${sortorder_name}"/>
	                            <c:if test="${! empty searchText}">
								    <osp:param name="searchText" value="${searchText}"/>
	                            </c:if>
	                            <c:if test="${! empty memberSearch}">
								    <osp:param name="memberSearch" value="${memberSearch}"/>
	                            </c:if>
	                            <c:if test="${! empty filterList}">
								    <osp:param name="filterList" value="${filterList}"/>
	                            </c:if>
	                            <c:if test="${! empty groups}">
								    <osp:param name="groups" value="${groups}"/>
	                            </c:if>
								</osp:url>">
				<c:out value="${msgs.table_header_name}" /></a> 
				<c:if	test="${sortOn eq 'name'}">
					<img src="${sortDirImageUrl}" />
				</c:if>
			</th>
          
			<th scope="col" class="attach"></th>
          
			<th scope="col" class="${className_owner}">
				<a href="<osp:url value="listPresentation.osp">
								<osp:param name="sortOn" value="owner"/>
								<osp:param name="sortorder" value="${sortorder_owner}"/>
	                            <c:if test="${! empty searchText}">
								    <osp:param name="searchText" value="${searchText}"/>
	                            </c:if>
	                            <c:if test="${! empty memberSearch}">
								    <osp:param name="memberSearch" value="${memberSearch}"/>
	                            </c:if>
	                            <c:if test="${! empty filterList}">
								    <osp:param name="filterList" value="${filterList}"/>
	                            </c:if>
	                            <c:if test="${! empty groups}">
								    <osp:param name="groups" value="${groups}"/>
	                            </c:if>
								</osp:url>">
				<c:out value="${msgs.table_header_owner}" /></a> 
				<c:if	test="${sortOn eq 'owner'}">
					<img src="${sortDirImageUrl}" />
				</c:if>
			</th>
          
			<th scope="col" class="${className_dateModified}">
				<a href="<osp:url value="listPresentation.osp">
								<osp:param name="sortOn" value="dateModified"/>
								<osp:param name="sortorder" value="${sortorder_dateModified}"/>
	                            <c:if test="${! empty searchText}">
								    <osp:param name="searchText" value="${searchText}"/>
	                            </c:if>
	                            <c:if test="${! empty memberSearch}">
								    <osp:param name="memberSearch" value="${memberSearch}"/>
	                            </c:if>
	                            <c:if test="${! empty filterList}">
								    <osp:param name="filterList" value="${filterList}"/>
	                            </c:if>
	                            <c:if test="${! empty groups}">
								    <osp:param name="groups" value="${groups}"/>
	                            </c:if>
								</osp:url>">
				<c:out value="${msgs.table_header_dateModified}" /></a> 
				<c:if	test="${sortOn eq 'dateModified'}">
					<img src="${sortDirImageUrl}" />
				</c:if>
			</th>
          
			 <c:if test="${myworkspace || showAllSites == 'true' || canReviewInSite}">
				 <th scope="col" class="${className_reviewed}">
					 <a href="<osp:url value="listPresentation.osp">
								 <osp:param name="sortOn" value="reviewed"/>
								 <osp:param name="sortorder" value="${sortorder_reviewed}"/>
	                            <c:if test="${! empty searchText}">
								    <osp:param name="searchText" value="${searchText}"/>
	                            </c:if>
	                            <c:if test="${! empty memberSearch}">
								    <osp:param name="memberSearch" value="${memberSearch}"/>
	                            </c:if>
	                            <c:if test="${! empty filterList}">
								    <osp:param name="filterList" value="${filterList}"/>
	                            </c:if>
	                            <c:if test="${! empty groups}">
								    <osp:param name="groups" value="${groups}"/>
	                            </c:if>
								</osp:url>">
					 <c:out value="${msgs.table_header_review}" /></a> 
					 <c:if test="${sortOn eq 'reviewed'}">
						 <img src="${sortDirImageUrl}" />
					 </c:if>
				 </th>
			 </c:if>
          
		 <c:if test="${filterList != 'search'}">
		     <th scope="col" class="attach"><c:out value="${msgs.table_header_status}"/></th>
          
			 <th scope="col" class="attach"><c:out value="${msgs.table_header_shared}"/></th>
          
			 <th scope="col" class="attach"><c:out value="${msgs.table_header_comments}"/></th>
	     </c:if>
          
			 <c:if test="${myworkspace || (filterList == 'search' && isSearchEnabled) || showAllSites == 'true'}">
				 <th scope="col" class="${className_worksite}">
		 			 <a href="<osp:url value="listPresentation.osp">
								 <osp:param name="sortOn" value="worksite"/>
								 <osp:param name="sortorder" value="${sortorder_worksite}"/>
	                            <c:if test="${! empty searchText}">
								    <osp:param name="searchText" value="${searchText}"/>
	                            </c:if>
	                            <c:if test="${! empty memberSearch}">
								    <osp:param name="memberSearch" value="${memberSearch}"/>
	                            </c:if>
	                            <c:if test="${! empty filterList}">
								    <osp:param name="filterList" value="${filterList}"/>
	                            </c:if>
	                            <c:if test="${! empty groups}">
								    <osp:param name="groups" value="${groups}"/>
	                            </c:if>
								 </osp:url>">
			                     <fmt:message key="table_header_worksite"/></a>
				     <c:if	test="${sortOn eq 'worksite'}">
					     <img src="${sortDirImageUrl}" />
				     </c:if>
				 </th>			 
			</c:if>          
		  </tr>
	   </thead>
		<tbody>
      
	  <c:forEach var="presentationBean" items="${presentations}" varStatus="loopCounter">
	
		<c:set var="presentation" value="${presentationBean.presentation}" />
		<c:set var="optionsAreNull" value="${presentation.template.propertyFormType != null and presentation.propertyForm == null}" />
		<c:set var="isAuthorizedTo" value="${presentation.authz}" />
		<osp-c:authZMap prefix="osp.presentation." var="presCan" qualifier="${presentation.id}"/>
	
		<tr
		<c:if test="${presentation.expired || optionsAreNull}">
		class="inactive"
		</c:if>
		>
		 <c:if test="${filterList == 'search' && isSearchEnabled}">
		  <td style="white-space:nowrap">
              <c:if test="${! presCan.view}">
                  <input id="pres-<c:out  value="${loopCounter.index}" />"
                         class='searchCheckbox' 
                         type="checkbox" 
				         value="true" 
				         onclick="jQuery(this).blur();"
				         onchange="requestOnchange(this, '<c:out value="${presentation.id}" />', '<c:out value="${presentation.escapedQuotesName}" />');" 
                         <c:if test="${presCan.request}">
                         checked="checked"
                         </c:if>
				  >
				  <script type="text/javascript">
				      var presentationId = "<c:out value='${presentation.id}'/>";
				      ospPresentationOwners[presentationId]=new Object();
				      ospPresentationOwners[presentationId].pid="<c:out value='${presentation.owner.id}'/>";
				      ospPresentationOwners[presentationId].displayName="<c:out value='${presentation.owner.displayName}' />";
				  </script>
			  </c:if>	
				     	  
		  </td>
		 </c:if>
		  <td style="white-space:nowrap">
		  <h4>
			 <c:choose>
				<c:when test="${(filterList !='search' and !optionsAreNull) or ((filterList == 'search' && isSearchEnabled) and (presCan.view or presentation.isPublic))}">
				  <a target="_blank" title='<c:out value="${msgs.table_presentationManager_new_window}"/>'
					  href="<c:out value="${baseUrl}"/><c:out value="${presentation.id.value}" />">
					  <c:out value="${presentation.name}" />
					</a>
				</c:when>
				<c:otherwise>
				  <c:out value="${presentation.name}" />
				</c:otherwise>
			 </c:choose>
		  </h4>	
		  </td>
        
        <!-- START selection of actions/options -->
		  <td>
		   <c:if test="${filterList != 'search'}">
           <form name="form${presentation.id.value}" style="margin:0">

               <%-- desNote: alternate rendering - using the resources menu as model - come back to it if time--%>
					<ul style="z-index:<c:out  value="${1000 - loopCounter.index}" />;margin:0;display:block" class="makeMenu" role="menubar">
						<li  class="menuOpen" tabindex="0" role="menuitem" aria-haspopup="true" aria-label="<fmt:message key="table_action_action"/>: <c:out value="${presentation.name}" />" >
							&nbsp;<fmt:message key="table_action_action"/>
							<img src = "/library/image/sakai/icon-dropdn.gif?panel=Main" border="0"  alt="Add"  class="dropdn"/> 
							<ul role="menu" class="makeMenuChild">
							<c:if test="${presentation.owner.id.value == osp_agent.id.value && !optionsAreNull}">
							    <li>
									<a role="menuitem" tabindex="-1" style="width:auto" href="<osp:url value="sharePresentation.osp"/>&id=<c:out value="${presentation.id.value}" />"><fmt:message key="action_share"/>
									</a>
							    </li>
							</c:if>               
						<c:if test="${isAuthorizedTo.edit || presentationBean.isCollab}">
							    <li>
									<a role="menuitem" tabindex="-1" style="width:auto"  
									   href="<osp:url value="editPresentation.osp"/>&id=<c:out value="${presentation.id.value}" />"> <fmt:message key="table_action_edit"/>
									</a>
								</li>
						</c:if>
                        <c:if test="${presentation.owner.id.value == osp_agent.id.value}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto" 
                                       href="<osp:url value="PresentationStats.osp"/>&id=<c:out value="${presentation.id.value}" />"><fmt:message key="table_action_viewStats"/></a>
                                </li>
                        </c:if>
                        
                        <c:if test="${presentation.owner.id.value == osp_agent.id.value}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto"  
                                       href="<osp:url includeQuestion="false" value="/repository/1=1"/>&manager=presentationManager&presentationId=<c:out value="${presentation.id.value}"/>/<c:out value="${presentation.name}" />.zip"><fmt:message key="table_action_download"/></a>
                                </li>
                        </c:if>
                        
                        <c:if test="${isAuthorizedTo.delete}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto" 
                                       href="<osp:url value="deletePresentation.osp"/>&id=<c:out value="${presentation.id.value}"/>"><fmt:message key="table_action_delete"/></a>
                                </li>                        
                        </c:if>
                        
                        <c:if test="${!presCan.hide}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto" 
                                       href="<osp:url value="hidePresentation.osp"/>&hideAction=hide&id=<c:out value="${presentation.id.value}" />&filterList=<c:out value="${filterList}"/>"><fmt:message key="table_action_hide"/></a>
                                </li>                        
                        </c:if>
                        
                        <c:if test="${presCan.hide}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto" 
                                       href="<osp:url value="hidePresentation.osp"/>&hideAction=show&id=<c:out value="${presentation.id.value}" />&filterList=<c:out value="${filterList}"/>"><fmt:message key="table_action_show"/></a>
                                </li>
                        </c:if>
                        
                        <c:if test="${!myworkspace && can.review && !presentation.isDefault}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto" 
                                       href="<osp:url value="reviewPresentation.osp"/>&review=true&id=<c:out value="${presentation.id.value}" />&filterList=<c:out value="${filterList}"/>"><fmt:message key="table_action_review_set"/></a>
                                </li>
                        </c:if>
                        <c:if test="${!myworkspace && can.review && presentation.isDefault}">
                                <li>
                                    <a role="menuitem" tabindex="-1" style="width:auto" 
                                       href="<osp:url value="reviewPresentation.osp"/>&review=false&id=<c:out value="${presentation.id.value}" />&filterList=<c:out value="${filterList}"/>"><fmt:message key="table_action_review_clear"/></a>
                                </li>                        
                        </c:if>

                        <!-- http://jira.sakaiproject.org/browse/SAK-17351 -->
                        <c:if test="${presentation.owner.id.value == osp_agent.id.value}">
                        <a href="<osp:url value="copyPresentation.osp"/>&id=<c:out value="${presentation.id.value}" />"><c:out value="${msgs.table_action_duplicate}"/></a>
                        </c:if>

                        </ul>
                   </li>
                   <li style="height:1px;width:1px;display:inline;">
                       <a href="#"  class="skip" onfocus="document.getElementById('menu-<c:out  value="${loopCounter.index}" />').style.display='none';document.getElementById('last-<c:out  value="${loopCounter.index}" />').focus()" ><c:out value="${msgs.table_action_action_close}"/></a>
                   </li>
               </ul>
			<a href="#" id="last-<c:out  value="${loopCounter.index}" />" class="skip"></a>
           </form>
           </c:if>
        </td>
        <!-- END selection of actions/options -->
        
		  <td><c:out value="${presentation.owner.displayName}" /></td>
        
		  <td><c:set var="dateFormat"><c:out value="${msgs.dateFormat_Middle}"/></c:set><fmt:formatDate value="${presentation.modified}" pattern="${dateFormat}"/></td> 
        
		  <c:if test="${myworkspace || showAllSites == 'true' || canReviewInSite}">
			 <td align="center" width="5%">
			 <c:if test="${presentation.isDefault}">
				<img alt='<c:out value="${msgs.alt_image_yes}"/>'  src="/library/image/sakai/checkon.gif" border="0"/>
			 </c:if>
			 </td>
		  </c:if>
        
		 <c:if test="${filterList != 'search'}">
		  <td align="center">
			 <c:if test="${!presentation.expired}">
				<img alt='<c:out value="${msgs.alt_image_yes}"/>'  src="/library/image/sakai/checkon.gif" border="0"/>
			 </c:if>
		  </td>
        
		  <td align="center">
			 <c:choose>
				 <c:when test="${presentationBean.publicPresentation}">
					<c:out value="${msgs.comments_public}"/>
				 </c:when>
				 <c:when test="${presentationBean.shared}">
					<img alt='<c:out value="${msgs.alt_image_yes}"/>'  src="/library/image/sakai/checkon.gif" border="0"/>
				 </c:when>
				 <c:otherwise/>
			 </c:choose>
		  </td>
		  
		  <td align="center">
				<c:choose>
				<c:when test="${presentationBean.commentNum > 0}">
  				<a href="<osp:url value="listComments.osp">
					<osp:param name="id" value="${presentation.id.value}" />
				</osp:url>" title='<c:out value="${msgs.table_header_comments}"/>'> 
				<c:out value="${presentationBean.commentNumAsString}" />
				</a>
				</c:when>
				<c:otherwise>
					 <c:out value="${presentationBean.commentNumAsString}" />
				</c:otherwise>
				</c:choose>
			</td>
         </c:if>
		  <c:if test="${myworkspace || (filterList == 'search' && isSearchEnabled) || showAllSites == 'true'}">
			 <td style="white-space:nowrap" ><c:out value="${presentation.worksiteName}" /></td>
		  </c:if>
		</tr>
	  </c:forEach>
	   </tbody>
	  </table>
	  <c:if test="${filterList == 'search' && isSearchEnabled}">
	      <div>
	          <input id="continue_button" type="button" value="Continue" style="display:none;" onclick="openSearchDialog();" />
	      </div>
	      <div id="searchDialog" style="display:none;">
	      </div>	      
	  </c:if>
	  <div style="height:20em"></div>
	 </c:otherwise>
</c:choose> 
</div>

<script language="JavaScript" type="text/JavaScript">
<!--

$('ul.makeMenu').keydown(function(e) { // "Add" and "Action" menus keyboard interaction handler
if(e.target.nodeName.toLowerCase() == 'li') {
    // Must be an 'add' or 'action' li
    switch(e.which) {
        case 38: // up arrow
            e.preventDefault();
            e.stopImmediatePropagation();
            $('.makeMenuChild').hide();
            $(e.target).find('.makeMenuChild').show().find('li:last a').first().focus();
            break;
        case 13: // enter
        case 32: // space
        case 40: // down arrow
            e.preventDefault();
            e.stopImmediatePropagation();
            $('.makeMenuChild').hide();
            $(e.target).find('.makeMenuChild').show().find('li:first a').first().focus();
            break;
        case 9:
            $('.makeMenuChild').hide();
            break;
    }
} else {
    // If not a li element, must be a submenu link (nothing else can have keyboard focus)
    switch(e.which) {
        case 32: // space
            e.preventDefault();
            e.stopImmediatePropagation();
            window.location.href = e.target.href; // .trigger('click') didn't work on the anchors, this does though
            break;
        case 38: // up arrow -- does not wrap around
            e.preventDefault();
            e.stopImmediatePropagation();
            $(e.target).parent('li').prev('li').find('a').first().focus();
            break;
        case 40: // down arrow -- does not wrap around
            e.preventDefault();
            e.stopImmediatePropagation();
            $(e.target).parent('li').next('li').find('a').first().focus();
            break;
        case 9: // tab key
            // don't stop propagation or default, let browser move the focus!
            $('.makeMenuChild').hide();
            break;
        case 27: // esc key
            e.preventDefault();
            e.stopImmediatePropagation();
            $('.makeMenuChild').hide();
            $(e.target).parents('ul.makeMenu > li').first().focus();
            break;
    }
}
});

//-->
</script>
