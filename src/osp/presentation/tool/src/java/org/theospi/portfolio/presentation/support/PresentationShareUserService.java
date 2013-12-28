package org.theospi.portfolio.presentation.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.metaobj.shared.mgt.AgentManager;
import org.sakaiproject.metaobj.shared.model.Agent;
import org.theospi.event.EventConstants;
import org.theospi.event.EventService;
import org.theospi.portfolio.presentation.control.UserAgentComparator;
import org.theospi.portfolio.presentation.model.Presentation;
import org.theospi.portfolio.presentation.PresentationFunctionConstants;

public class PresentationShareUserService {
	protected final Log logger = LogFactory.getLog(getClass());
	private UserAgentComparator userAgentComparator = new UserAgentComparator();
	
	private MemoryService memoryService;
	private PresentationService presentationService;
	private EventService eventService;
	private AgentManager agentManager;
	
	/** A site cache. */
	private ShareUserCacheImpl shareUserCache = null;

	/** The # seconds to cache the site queries. 0 disables the cache. */
	protected int cacheSeconds = 60 * 5;

	public void setCacheSeconds(String time)
	{
		cacheSeconds = Integer.parseInt(time);
	}

	public void setCacheMinutes(String time)
	{
		cacheSeconds = Integer.parseInt(time) * 60;
	}
	
	public void init() {
		if (cacheSeconds > 0)
		{
			shareUserCache = new ShareUserCacheImpl(memoryService, cacheSeconds, PresentationFunctionConstants.PRESENTATION_SHARE_USER);
		}

	}

	/**
	 * Force loading the cache from the database whether the cache has expired or not
	 * 
	 * @param presentationId
	 */
	public void loadCache(String presentationId) {
	    Presentation presentation = presentationService.getPresentation(presentationId, false);
	    
	    getSharedList(presentation, true);
	}

	/**
	 * Clone the SharedList so that we can avoid the concurrency exception
	 * 
	 * @param list
	 * @return
	 */
    private List cloneArrayList(List list) {
        if (list == null || ! (list instanceof ArrayList)) {
            logger.debug("*** cannot clone list **** - " + list);
            return list;
        }
        
        return (List)((ArrayList) list).clone();
    }
    
	public void setSharedList(List sharedList, Presentation presentation) {
	    shareUserCache.put(PresentationFunctionConstants.PRESENTATION_SHARE_USER_CACHE_PREFIX + presentation.getId().getValue(), 
	                       sharedList);
	}

    public List getSharedList(Presentation presentation) {
        return  getSharedList(presentation, false);
    }
    
    public List getSharedList(Presentation presentation, boolean forceDbReload) {
        List sharedList = (List) shareUserCache.get(PresentationFunctionConstants.PRESENTATION_SHARE_USER_CACHE_PREFIX + presentation.getId().getValue());

        if (sharedList == null || forceDbReload) {
            logger.debug("PresentationShareUserService - Pulling from db");
            sharedList = presentationService.getShareList( presentation );
            Collections.sort(sharedList, userAgentComparator);
            setSharedList(sharedList, presentation);
        }
        
        sharedList = cloneArrayList(sharedList); // to prevent concurrent access to cache
        return sharedList;
    }

    /**
     * 
     * @param presentationId
     * @param userId
     * @return null if user not found
     */
    public Agent getUserAgentFromCache(String presentationId, String userId) {
        if (presentationId == null || userId == null) {
            return null;
        }
        
        Presentation presentation = presentationService.getPresentation(presentationId, false);
        List<Agent> sharedList = getSharedList(presentation, false);

        for(Agent agent: sharedList) {
            if (agent.getId().getValue().equals(userId)) {
                return agent;
            }
        }

        return null;
    }

    /**
     * Send the add user event to the cluster
     * 
     * @param presentationId
     * @param userId
     */
    public void triggerAddUserEvent(String presentationId, String userId) {
      eventService.postEvent(EventConstants.EVENT_PRESENTATION_USER_ADD, 
      encodeReference(presentationId, userId));
    }
    
    /**
     * Add the specified user into the shared list cache for the presentation
     * It will do nothing is the user already exists int he cache for that presentation
     * 
     * @param presentationId
     * @param userId
     */
    public void addUser(String presentationId, String userId) {
        if (presentationId == null || userId == null) {
            return;
        }
        
        Presentation presentation = presentationService.getPresentation(presentationId, false);
        
        if (presentation != null) {
            List<Agent> sharedList = getSharedList(presentation, false);

            Agent existingAgent = getUserAgentFromCache(presentationId, userId);
            
            if (existingAgent == null) {
                Agent agent = agentManager.getAgent(userId);

                if (agent != null) {
                    sharedList.add(agent);
                    Collections.sort(sharedList, userAgentComparator);
                    setSharedList(sharedList, presentation);
                }
            }
        }
    }
  
    /**
     * Send the delete user event to the cluster
     * 
     * @param presentationId
     * @param userId
     */
    public void triggerRemoveUserEvent(String presentationId, String userId) {
        eventService.postEvent(EventConstants.EVENT_PRESENTATION_USER_DELETE, 
                encodeReference(presentationId, userId));
    }

    /**
     * Remove the specified user into the shared list cache for the presentation
     * It will do nothing is the user does not already exists in the cache for that presentation
     *
     * @param presentationId
     * @param userId
     */
    public void removeUser(String presentationId, String userId) {
        if (presentationId == null || userId == null) {
            return;
        }
        
        Presentation presentation = presentationService.getPresentation(presentationId, false);

        if (presentation != null) {
            Agent agent = getUserAgentFromCache(presentationId, userId);
            
            if (agent != null) {
                List<Agent> sharedList = getSharedList(presentation, false);

                sharedList.remove(agent);
                setSharedList(sharedList, presentation);
                return;
            }
        }
    }

    /**
     * Returns the presentationId from the shared list cache event reference (from the observer)
     * 
     * @param reference
     * @return
     */
    public static String getPresentationIdFromReference (String reference) {
        if (reference == null || 
            reference.indexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_PREFIX) == -1) {
            return null;
        }
        
        String presentationId =  null;

        int firstIndex = reference.indexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM, 1);
        int lastIndex = reference.lastIndexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM);

        if (firstIndex == -1 || lastIndex == -1 || firstIndex+1 > reference.length()) {
            return null;
        }
        
        presentationId = reference.substring(firstIndex+1, lastIndex);
        
        return presentationId;
    }

    /**
     * Returns the userId from the shared list cache event reference (from the observer)
     * 
     * @param reference
     * @return
     */
    public static String getUserIdFromReference (String reference) {
        if (reference == null ||
            reference.indexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_PREFIX) == -1) {
            return null;
        }
        
        String userId =  null;
        
        int lastIndex = reference.lastIndexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM);
                
        if (lastIndex == -1 || lastIndex+1 > reference.length()) {
            return null;
        }
        
        userId = reference.substring(lastIndex+1);

        return userId;
    }

    /**
     * Take a presentationId and userId and create an event reference
     * 
     * @param presentationId
     * @param userId
     * @return reference used for the event trigger
     */
    public static String encodeReference(String presentationId, String userId) {
        
        if (presentationId == null || userId == null ||
            presentationId.indexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM) != -1 || 
            userId.indexOf(PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM) != -1) {
            return null;
        }

        String reference = "";
        reference = PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM + PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_PREFIX + 
                    PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM + presentationId.trim() + 
                    PresentationFunctionConstants.PRESENTATION_SHARE_USER_EVENT_REFERENCE_DELIM + userId.trim();
        
        return reference;
    }
    
	public MemoryService getMemoryService() {
		return memoryService;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

    public PresentationService getPresentationService() {
        return presentationService;
    }

    public void setPresentationService(PresentationService presentationService) {
        this.presentationService = presentationService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

}
