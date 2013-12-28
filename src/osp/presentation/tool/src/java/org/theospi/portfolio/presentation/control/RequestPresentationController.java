/**********************************************************************************
* $URL:https://source.sakaiproject.org/svn/osp/trunk/presentation/tool/src/java/org/theospi/portfolio/presentation/control/RequestPresentationController.java $
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.theospi.portfolio.presentation.control;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import org.theospi.portfolio.presentation.model.Presentation;
import org.theospi.portfolio.presentation.PresentationFunctionConstants;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.Web;

public class RequestPresentationController extends AbstractPresentationController {

   protected final Log logger = LogFactory.getLog(getClass());
   
   private UserDirectoryService userDirectoryService;
   private EmailService emailService;
   private PortalService portalService;
   private SiteService siteService;

   
    public final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
	public final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
	public final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";
	public final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
	public final String PLAIN_TEXT_HEADERS= "Content-Type: text/plain\n\n";
	public final String HTML_HEADERS = "Content-Type: text/html; charset=ISO-8859-1\n\n";
	public final String HTML_END = "\n  </body>\n</html>\n";

   
   @SuppressWarnings("unchecked")
   public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
	
	  boolean result = false;
	  Hashtable<String, Object> model = new Hashtable<String, Object>();

	  String requestAccess = (String)request.get("requestAccess");
	  String grantAccess = (String)request.get("grantAccess");
	  String denyAccess = (String)request.get("denyAccess");
	  String customMessage = (String)request.get("customMessage");
	  String presentationId = (String)request.get("presentationId");
	  String requestingUserId = (String)request.get("requestingUserId");
      ResourceLoader rl = new ResourceLoader("org.theospi.portfolio.presentation.bundle.Messages");
      String requestAccessMessage = null;
      
	  if (requestAccess != null) {
		  logger.debug("*** requesting access called!!!");
		  
		  int i=1;
		  
		  while ((presentationId = (String)request.get("presentation" + i++)) != null) {
			  if (customMessage == null) {
				  customMessage = "";
			  }
			  
			  result = requestAccess(presentationId, customMessage);
		  }
	  }
	  else {
		  if (grantAccess != null) {
			  logger.debug("*** grant access called!!!");
			  result = grantAccess(presentationId, requestingUserId);
			  
			  if (result) {
			      requestAccessMessage = rl.getString("share_request.approved");
    		      model.put("requestAccessMessage", requestAccessMessage);
    		      return new ModelAndView("list", model);
			  }
		  }
		  else {
			  if (denyAccess != null) {
				  logger.debug("*** deny access called!!!");
				  result = denyAccess(presentationId, requestingUserId);
				  if (result) {
  				      requestAccessMessage = rl.getString("share_request.denied");
	    		      model.put("requestAccessMessage", requestAccessMessage);
	    		      return new ModelAndView("list", model);
				  }
			  }
		  }
		  
	  }
		  
	  return new ModelAndView("success", model);

   }

   private boolean requestAccess(String presentationId, String personalMessage) {
	   String from = null;
	   String requesterName = null;
	   String requesterUserId = null;
	   String to = null;
	   String emailBody = null;
	   
	   
	   from = userDirectoryService.getCurrentUser().getEmail();
	   requesterName = userDirectoryService.getCurrentUser().getDisplayName();
	   requesterUserId = userDirectoryService.getCurrentUser().getEid();

	   Id id = getIdManager().getId(presentationId);
	   
       Presentation presentation = getPresentationManager().getPresentation(id, false);
       
       try {
	       to = (userDirectoryService.getUser(presentation.getOwner().getId().getValue())).getEmail();
	   } 
       catch (UserNotDefinedException e) {
		   e.printStackTrace();
		   return false;
	   }
       
       ResourceLoader rl = new ResourceLoader("org.theospi.portfolio.presentation.bundle.Messages");
       
       String emailTitle = rl.getString("share_request.email.title");
       String emailClosingAndSignature = rl.getString("share_request.dialog.closing_and_signature");
       String emailPersonalMessage = rl.getString("share_request.dialog.personal_message");

       String presentationToolId = presentation.getToolId();
       String siteId = presentation.getSiteId();

       Site site = null;
       
       try 
       {
	       site = siteService.getSite(siteId);
	   } 
       catch (IdUnusedException e) 
       {
		   e.printStackTrace();
		   return false;
	   }
       
       
       String emailMessageTextContent = rl.getFormattedMessage("share_request.dialog.message_text_content_alternate",
                                                               new Object[]{requesterName, presentation.getName(),
    		                                                                site.getTitle()});
       String emailInstructionsHeader = rl.getFormattedMessage("share_request.email.sharing_instructions_header",
    		                                                   new Object[]{requesterName});
       String emailInstructions1 = rl.getFormattedMessage("share_request.email.sharing_instructions_1",
    		                                              new Object[]{presentation.getName()});
       String emailInstructions2 = rl.getString("share_request.email.sharing_instructions_2");
       String emailInstructions3 = rl.getString("share_request.email.sharing_instructions_3");
       String emailInstructions4 = rl.getFormattedMessage("share_request.email.sharing_instructions_4",
    		                                              new Object[]{requesterUserId});
       String emailInstructions5 = rl.getFormattedMessage("share_request.email.sharing_instructions_5",
    		                                              new Object[]{requesterName});
       String emailInstructions6 = rl.getString("share_request.email.sharing_instructions_6");

       
       String serverUrl = ServerConfigurationService.getServerUrl();
       String shareUrl = serverUrl + "/portal/directtool/" + presentationToolId + 
    		            "/sharePresentation.osp?1=1&id=" + presentationId;
       
       emailBody = "<p>" + emailMessageTextContent + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p>" + emailClosingAndSignature + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p style='font-weight:bold;'>" + emailPersonalMessage + ":</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p>" + personalMessage + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p style='font-weight:bold;'>" + emailInstructionsHeader + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<ol>\n";
       emailBody = emailBody + "<li>" + emailInstructions1 + "<br/p>\n";
       emailBody = emailBody + "<a href='" + shareUrl + "'>" + shareUrl + "</a></li>\n";
       emailBody = emailBody + "<li>" + emailInstructions2 + "</li>\n";
       emailBody = emailBody + "<li>" + emailInstructions3 + "</li>\n";
       emailBody = emailBody + "<li>" + emailInstructions4 + "</li>\n";
       emailBody = emailBody + "<li>" + emailInstructions5 + "</li>\n";
       emailBody = emailBody + "<li>" + emailInstructions6 + "</li>\n";
       emailBody = emailBody + "</ol>\n";

	   sendEmail(from, to, emailTitle, emailBody);

	   return true;
   }
   
   private boolean requestAccessWithAcceptAndDenyLinks(String presentationId, String personalMessage) {
	   String from = null;
	   String requesterName = null;
	   String to = null;
	   String emailBody = null;
	   
	   
	   from = userDirectoryService.getCurrentUser().getEmail();
	   requesterName = userDirectoryService.getCurrentUser().getDisplayName();

	   Id id = getIdManager().getId(presentationId);
	   
       Presentation presentation = getPresentationManager().getPresentation(id, false);
       
       try {
	       to = (userDirectoryService.getUser(presentation.getOwner().getId().getValue())).getEmail();
	   } 
       catch (UserNotDefinedException e) {
		   e.printStackTrace();
		   return false;
	   }
       
       ResourceLoader rl = new ResourceLoader("org.theospi.portfolio.presentation.bundle.Messages");
       
       String emailTitle = rl.getString("share_request.email.title");
       String emailMessageTextContent = rl.getFormattedMessage("share_request.dialog.message_text_content",
    		                              new Object[]{requesterName, presentation.getName()});
       String emailAcceptRequest = rl.getString("share_request.dialog.accept_request");
       String emailDenyRequest = rl.getString("share_request.dialog.deny_request");
       String emailClosingAndSignature = rl.getString("share_request.dialog.closing_and_signature");
       String emailPersonalMessage = rl.getString("share_request.dialog.personal_message");

       String ownerMyWorkspaceSiteId = siteService.getUserSiteId(presentation.getOwner().getId().getValue());
       

       Site site = null;
       
       try 
       {
           site = siteService.getSite(ownerMyWorkspaceSiteId);
	   } 
       catch (IdUnusedException e) 
       {
		   e.printStackTrace();
		   return false;
	   }
       
       ToolConfiguration tc = site.getToolForCommonId("osp.presentation");
       
       // does the site have the presentation tool?
       if (tc == null) { 
           return false;
       }
       
       String encodeToolState = null;
       
       String pageId = tc.getPageId();
       String presentationToolId = tc.getId();
       
       String serverUrl = ServerConfigurationService.getServerUrl();
       String baseUrl = serverUrl + "/portal/site/" + ownerMyWorkspaceSiteId + "/page/" + pageId;
       
       encodeToolState = encodeToolState(presentationToolId, "/requestPresentation.osp?grantAccess=1&presentationId=" + presentationId + 
                                                             "&requestingUserId=" + userDirectoryService.getCurrentUser().getId());

       String acceptLink = baseUrl + "?" + encodeToolState;

       encodeToolState = encodeToolState(presentationToolId, "/requestPresentation.osp?denyAccess=1&presentationId=" + presentationId + 
                                                             "&requestingUserId=" + userDirectoryService.getCurrentUser().getId());

       String denyLink = baseUrl + "?" + encodeToolState;
       
       emailBody = "<p>" + emailMessageTextContent + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p><a href='" + acceptLink + "'>" + emailAcceptRequest + "</a> | " +
                   "<a href='" + denyLink + "'>" + emailDenyRequest + "</a></p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p>" + emailClosingAndSignature + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p style='font-weight:bold;'>" + emailPersonalMessage + ":</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p>" + personalMessage + "</p>\n";

	   sendEmail(from, to, emailTitle, emailBody);

	   return true;
   }

   private boolean grantAccess(String presentationId, String requestingUserId) {
	   String from = null;
	   String ownerName = null;
	   String to = null;
	   String emailBody = null;
	   String emailTitle = null;
	   
	   from = userDirectoryService.getCurrentUser().getEmail();
	   ownerName = userDirectoryService.getCurrentUser().getDisplayName();
	   String currentUserId = userDirectoryService.getCurrentUser().getId();
	   
	   Id id = getIdManager().getId(presentationId);
	   
       Presentation presentation = getPresentationManager().getPresentation(id, false);
       
       try {
	       to = userDirectoryService.getUser(requestingUserId).getEmail();
	   } 
       catch (UserNotDefinedException e) {
		   e.printStackTrace();
		   return false;
	   }

       if (! currentUserId.equals(presentation.getOwner().getId().getValue())) {
    	   return false;
       }

       if (! getAuthzManager().isAuthorized(getAgentManager().getAgent(requestingUserId), 
                                          PresentationFunctionConstants.VIEW_PRESENTATION, 
                                          presentation.getId())) {
    	   
	       getAuthzManager().createAuthorization(getAgentManager().getAgent(requestingUserId),
			                        	                           PresentationFunctionConstants.VIEW_PRESENTATION, 
				                                                   presentation.getId());


	       emailTitle = "Presentation Request Approved";
	       ResourceLoader rl = new ResourceLoader("org.theospi.portfolio.presentation.bundle.Messages");
	       
	       String emailMessageTextContent = rl.getFormattedMessage("share_request.email_body.accepted",
                                            new Object[]{ownerName});
	       String emailClosingAndSignature = rl.getString("share_request.dialog.closing_and_signature");
	       
	       String serverUrl = ServerConfigurationService.getServerUrl();
	       String presentationUrl = serverUrl + "/osp-presentation-tool/viewPresentation.osp?id=" + presentationId;

	       emailBody = "<p>" + emailMessageTextContent + "</p>\n";
	       emailBody = emailBody + "<p>&nbsp;</p>\n";
	       emailBody = emailBody + "<p>" + presentation.getName() + "</p>\n";
	       emailBody = emailBody + "<p>&nbsp;</p>\n";
	       emailBody = emailBody + "<p><a href='" + presentationUrl + "'>" + presentationUrl + "</a></p>\n";
	       emailBody = emailBody + "<p>&nbsp;</p>\n";
	       emailBody = emailBody + "<p>" + emailClosingAndSignature + "</p>\n";
	       
	       // send approved email
		   sendEmail(from, to, emailTitle, emailBody);

       }

	   return true;
   }
   
   private boolean denyAccess(String presentationId, String requestingUserId) {
	   String from = null;
	   String ownerName = null;
	   String to = null;
	   String emailBody = null;
	   String emailTitle = null;

	   from = userDirectoryService.getCurrentUser().getEmail();
	   ownerName = userDirectoryService.getCurrentUser().getDisplayName();
	   String currentUserId = userDirectoryService.getCurrentUser().getId();
	   
	   Id id = getIdManager().getId(presentationId);
	   
       Presentation presentation = getPresentationManager().getPresentation(id, false);

       if (! currentUserId.equals(presentation.getOwner().getId().getValue())) {
    	   return false;
       }
       
       try {
	       to = userDirectoryService.getUser(requestingUserId).getEmail();
	   } 
       catch (UserNotDefinedException e) {
		   e.printStackTrace();
		   return false;
	   }

       emailTitle = "Presentation Request Denied";
       ResourceLoader rl = new ResourceLoader("org.theospi.portfolio.presentation.bundle.Messages");
       
       String emailMessageTextContent = rl.getFormattedMessage("share_request.email_body.denied",
                                        new Object[]{ownerName});
       String emailClosingAndSignature = rl.getString("share_request.dialog.closing_and_signature");
       
       emailBody = "<p>" + emailMessageTextContent + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p>" + presentation.getName() + "</p>\n";
       emailBody = emailBody + "<p>&nbsp;</p>\n";
       emailBody = emailBody + "<p>" + emailClosingAndSignature + "</p>\n";

	   // send deny email
	   sendEmail(from, to, emailTitle, emailBody);
     
	   return true;
   }
   
   /**
    * Convenience method for sending email messages
    * @param fromEmail
    * @param toEmail
    * @param subject
    * @param emailBody
    */
   private void sendEmail(String fromEmail, String toEmail, String subject, String emailBody) {
	   Collection<User> receivers = null;
	   
	   receivers = userDirectoryService.findUsersByEmail(toEmail);

	   if (! receivers.isEmpty()) {
	       emailService.sendToUsers(receivers, getHeaders(toEmail, fromEmail, subject), formatMessage(subject, emailBody));
	   }
   }
   
   /** helper methods for formatting the message */
	private String formatMessage(String subject, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append(MIME_ADVISORY);
		sb.append(BOUNDARY_LINE);
		sb.append(PLAIN_TEXT_HEADERS);
		sb.append(Validator.escapeHtmlFormattedText(message));
		sb.append(BOUNDARY_LINE);
		sb.append(HTML_HEADERS);
		sb.append(htmlPreamble(subject));
		sb.append(message);
		sb.append(HTML_END);
		sb.append(TERMINATION_LINE);
		
		return sb.toString();
	}
	
	private String htmlPreamble(String subject) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
		sb.append("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
		sb.append("<html>\n");
		sb.append("<head><title>");
		sb.append(subject);
		sb.append("</title></head>\n");
		sb.append("<body>\n");
		
		return sb.toString();
	}
	
	private List<String> getHeaders(String emailTo, String emailFrom, String subject){
		List<String> headers = new ArrayList<String>();
		
		headers.add("MIME-Version: 1.0");
		headers.add("Content-Type: multipart/alternative; boundary=\""+MULTIPART_BOUNDARY+"\"");
		headers.add("Subject: " + subject);
		headers.add("From: " + emailFrom);
		if (StringUtils.isNotBlank(emailTo)) {
			headers.add("To: " + emailTo);
		}

		return headers;
	}
	
	private String encodeToolState(String presentationToolId, String urlStub) {
		
		String result = "";
		
		Map<String, String[]> toolState = portalService.encodeToolState(presentationToolId, urlStub);

		Set<String> keySet = toolState.keySet();

		for(String key: keySet) {
			System.out.println("key = " + key);

			result = result + key;
			
			String[] values = toolState.get(key);

			for(int i=0; i < values.length; i++) {
				System.out.println("value = " + values[i]);
//				result = result + "=" + Web.escapeUrl(values[i]);
				result = result + "=" +  URLEncoder.encode(values[i]);
			}
		}
		
		return result;
	}

	
   public UserDirectoryService getUserDirectoryService() {
       return userDirectoryService;
   }

   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   public EmailService getEmailService() {
      return emailService;
   }

   public void setEmailService(EmailService emailService) {
      this.emailService = emailService;
   }

   public PortalService getPortalService() {
	  return portalService;
   }

   public void setPortalService(PortalService portalService) {
	  this.portalService = portalService;
   }

   public SiteService getSiteService() {
      return siteService;
   }

   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }
}
