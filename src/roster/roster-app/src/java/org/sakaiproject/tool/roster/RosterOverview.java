/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/roster/trunk/roster-app/src/java/org/sakaiproject/tool/roster/RosterOverview.java $
 * $Id: RosterOverview.java 125280 2013-05-31 03:23:54Z azeckoski@unicon.net $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.roster;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterCsv;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RosterOverview extends BaseRosterPageBean {
	private static final String DISPLAY_ROSTER_PRIVACY_MSG = "roster.privacy.display";

	// UI method calls
	public boolean isRenderModifyMembersInstructions() {
		String siteRef = getSiteReference();
		return filter.services.securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef) ||
				filter.services.securityService.unlock(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, siteRef);
	}

	/**
	 * Determine whether privacy message should be displayed. Will be shown if
	 * roster.privacy.display in sakai.properties is "true" and the user does
	 * not have roster.viewhidden permission
	 * 
	 * @return
	 */
	public boolean isRenderPrivacyMessage() {
		String msgEnabled = ServerConfigurationService.getString(DISPLAY_ROSTER_PRIVACY_MSG, Boolean.TRUE.toString());
		if (Boolean.TRUE.toString().equalsIgnoreCase(msgEnabled)) {
			return ! filter.services.securityService.unlock(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, getSiteReference());
		} else {
			return ! filter.services.securityService.unlock(RosterFunctions.ROSTER_FUNCTION_VIEWALL, getSiteReference());
		}
	}
			
	public String getPageTitle() {
        filter.services.eventTrackingService.post(filter.services.eventTrackingService.newEvent("roster.view",getSiteReference(),false));
        return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "title_overview");
	}

	public boolean isExportablePage() {
		return filter.services.rosterManager.currentUserHasExportPerm();
	}
	public void export(ActionEvent event) {
		List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		
		// Add the header row
		List<Object> header = new ArrayList<Object>();
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_name"));
        header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_userId"));

        if (isEmailColumnRendered()) {
            header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_email"));
        }
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_role"));
		
		spreadsheetData.add(header);
		for(Iterator<Participant> participantIter = getParticipants().iterator(); participantIter.hasNext();) {
			Participant participant = participantIter.next();
			List<Object> row = new ArrayList<Object>();
			row.add(participant.getUser().getSortName());
            row.add(participant.getUser().getDisplayId());

            if (isEmailColumnRendered()){
                row.add(participant.getUser().getEmail());
            }
			row.add(participant.getRoleTitle());
            spreadsheetData.add(row);
        }

        String spreadsheetNameRaw;
        if(StringUtils.trimToNull(filter.sectionFilter) == null) {
        	spreadsheetNameRaw = filter.getCourseFilterTitle();
        } else {
        	CourseSection section = filter.services.sectionAwareness.getSection(filter.getSectionFilter());
        	spreadsheetNameRaw = filter.getCourseFilterTitle() + "_" + section.getTitle();
        }

        String spreadsheetName = getDownloadFileName(spreadsheetNameRaw);
        SpreadsheetUtil.downloadSpreadsheetData(spreadsheetData, spreadsheetName, new SpreadsheetDataFileWriterXls());
    }

    /**
     * check to see if Early Alerts can be displayed
     * @return true, if allowed
     */
    public boolean isRenderAddAlert(){
        return ServerConfigurationService.getBoolean("ssp.allowed.alerts", false) && isMaintainer();
    }

    /**
     * check to see if current user is a site maintainer
     * @return true, if user has a maintain role
     */
    private boolean isMaintainer() {
        boolean isMaintainer = false;
        try {
            String siteId = filter.services.toolManager.getCurrentPlacement().getContext();
            Site site = filter.services.siteService.getSite(siteId);
            Set<String> maintainers = site.getUsersHasRole(site.getMaintainRole());
            String userId = filter.services.userDirectoryService.getCurrentUser().getId();
            isMaintainer = maintainers.contains(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMaintainer;
    }

    /**
     * Processes the event click when instructor wants to add an Early Alert in the SSP system
     */
    public void addAlert(){
        Map<String, String> attributes = new HashMap<String, String>();
        String eaView = "ea.new";

        // get participant external id
        String participantEid = StringUtils.trimToNull((String) FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap().get("participantEid"));
        attributes.put("participanteid", participantEid);

        // get current date and time
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeStamp = sdf.format(date);
        attributes.put("timestamp", timeStamp);

        try {
            // get title of the site
            String siteId = filter.services.toolManager.getCurrentPlacement().getContext();
            Site site = filter.services.siteService.getSite(siteId);
            String siteTitle = URLEncoder.encode(site.getTitle(), "UTF-8");
            attributes.put("sitetitle", siteTitle);

            // get the current user
            User user = filter.services.userDirectoryService.getCurrentUser();
            String instructorEid = user.getEid();
            attributes.put("instructoreid", instructorEid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // set the view property
        attributes.put("view", eaView);

        String requestUrl = buildRequestUrl(attributes);
        sendRequest(requestUrl);
    }

    /**
     * gets the MD5 token for the URL to send to the SSP server
     * @param instructorEid external ID of the instructor
     * @param timeStamp the current timestamp
     * @return the MD5 token
     */
    private String getToken(String instructorEid, String timeStamp) {
        String tokenHash = null;
        String shared = ServerConfigurationService.getString("ssp.alerts.shared.password", "");
        String tokenString = instructorEid + timeStamp + shared;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] token = tokenString.getBytes("UTF-8");
            byte[] tokenMd5 = md.digest(token);
            StringBuilder tokenEncoded = new StringBuilder(2*tokenMd5.length);

            for(byte b : tokenMd5) {
                tokenEncoded.append(String.format("%02x", b&0xff));
            }

            tokenHash = tokenEncoded.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokenHash;
    }

    /**
     * builds the URL to send to the SSP server
     * @param attributes HashMap of the attributes for the URL
     * @return the full URL to send
     */
    private String buildRequestUrl(Map<String, String> attributes) {
        StringBuilder requestUrl = new StringBuilder();
        String sspUrl = ServerConfigurationService.getString("ssp.server.url", "");
        String token = getToken(attributes.get("instructoreid"), attributes.get("timestamp"));

        // builds the URL with the necessary tokens
        try {
            requestUrl.append(sspUrl)
                .append("?username=")
                .append(attributes.get("instructoreid"))
                .append("&formattedCourse=")
                .append(attributes.get("sitetitle"))
                .append("&studentSchoolId=")
                .append(attributes.get("participanteid"))
                .append("&timeStamp=")
                .append(URLEncoder.encode(attributes.get("timestamp"), "UTF-8"))
                .append("&token=")
                .append(token)
                .append("&view=")
                .append(attributes.get("view"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return requestUrl.toString();
    }

    /**
     * sends the URL to the SSP server
     * @param requestUrl
     */
    private void sendRequest(String requestUrl) {
        String responseJson = "";
        try {
            // connect to SSP server
            URL url = new URL(requestUrl);
            URLConnection connection = url.openConnection();

            // read response from SSP server
            InputStreamReader inStream = new InputStreamReader(connection.getInputStream());
            BufferedReader buff= new BufferedReader(inStream);
            StringBuilder json = new StringBuilder();
            String line = "";
            while ((line = buff.readLine()) != null) {
                json.append(line);
            }
            responseJson = json.toString();

            // process JSON from response
            Gson gson = new Gson();
            Map<String, String> responseContent = gson.fromJson(responseJson, new TypeToken<Map<String, String>>(){}.getType());

            // handle redirect
            doRedirect(responseContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * handles the redirect to the Early Alert page for the instructor and student
     * @param responseContent HashMap of the JSON response
     */
    private void doRedirect(Map<String, String> responseContent) {
        String redirectUrl = responseContent.get("URL");

        // redirect browser to SSP Early Alert page
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
