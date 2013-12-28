package org.theospi.portfolio.presentation.support;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.theospi.event.EventConstants;
import org.theospi.portfolio.presentation.PresentationFunctionConstants;
import org.theospi.portfolio.presentation.control.UserAgentComparator;

public class ShareUserCacheListener implements Observer {
	protected final Log logger = LogFactory.getLog(getClass());
	private UserAgentComparator userAgentComparator = new UserAgentComparator();
	private ServerConfigurationService serverConfigurationService;

	private PresentationShareUserService presentationShareUserService;
	
	public void init() {
		  EventTrackingService.addObserver(this);
	}
	
	public void update(Observable o, Object arg) {
	      if (arg instanceof Event) {
	          
	          Event event = (Event) arg;
	          
	          if (event != null && event.getResource() != null && 
	              event.getResource().indexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_PREFIX ) != -1) {
	              processEvent(event, o);
	          }
	       }
	}

	private void processEvent(Event event, Observable arg) {
	    
	    if (event == null) {
	        return; 
	    }
	    
        String reference = event.getResource();
        String presentationId = PresentationShareUserService.getPresentationIdFromReference(reference);
        String userId = PresentationShareUserService.getUserIdFromReference(reference);
        
		if (event.getEvent().equals(EventConstants.EVENT_PRESENTATION_USER_CLEAR)) {
	        logger.debug("********ShareCacheListener - CLEAR detected");
		}
		else if (event.getEvent().equals(EventConstants.EVENT_PRESENTATION_USER_ADD)) {
            logger.debug("********ShareCacheListener - ADD detected - " + serverConfigurationService.getServerId() + " - " + serverConfigurationService.getServerName());
		    presentationShareUserService.addUser(presentationId, userId);
		}
        else if (event.getEvent().equals(EventConstants.EVENT_PRESENTATION_USER_DELETE)) {
            logger.debug("********ShareCacheListener - DELETE detected - " + serverConfigurationService.getServerId() + " - " + serverConfigurationService.getServerName());
            presentationShareUserService.removeUser(presentationId, userId);
        }
	}

    public PresentationShareUserService getPresentationShareUserService() {
        return presentationShareUserService;
    }

    public void setPresentationShareUserService(
            PresentationShareUserService presentationShareUserService) {
        this.presentationShareUserService = presentationShareUserService;
    }

    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }
}
