/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.theospi.portfolio.worksite.mgt.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.siteassociation.api.SiteAssocManager;

/**
 * This class listens for several events and will create/remove groups in sites as needed, marking them as provided.
 * @author chrismaurer
 *
 */
public class SiteGroupProviderListener implements Observer {

    protected final transient Log logger = LogFactory.getLog(getClass());
    private EntityManager entityManager;
    private GroupProvider groupProvider;
    private SiteAssocManager siteAssocManager;
    private SiteService siteService;
    private AuthzGroupService authzGroupService;
    
    private boolean enableListener = true;
    
    private static final String MANAGED_GROUP_PROPERTY = "MANAGED_GROUP_PROPERTY";
    
    public void init() {
        logger.info("init()");
        if (isEnableListener()) {
            logger.info("Enabling listener");
            EventTrackingService.addObserver(this);         
        }
    }

    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code>
     *            method.
     */
    public void update(Observable o, Object arg) {
        if (arg instanceof Event) {
            String eventStr = ((Event)arg).getEvent();
            if (eventStr.equals(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP) || eventStr.equals(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP)) {
                processEvent((Event)arg, o);
            }
        }
    }

    protected void processEvent(Event event, Observable arg) {
        String eventStr = event.getEvent();

        Reference ref = getEntityManager().newReference(event.getResource());
        try {
            
            if (eventStr.equals(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP)) {
                // This was a realm update (likely coming from the roster sync job)
                //  so the ref is a realm ref rather than just a site ref
                ref = getEntityManager().newReference(ref.getId());
            }
            
            // When adding in the listening for realm.upd, it picks up group updates now and we want to ignore them
            if (!ref.getSubType().equals("group")) {
                Site siteFromId = getSiteService().getSite(ref.getId());
            
                // check out the update
                processUpdate(siteFromId);
            }
        } catch (IdUnusedException e) {
            logger.error("error getting site object", e);
        }
    }

    protected void processUpdate(Site site) {
        List<String> siteIds = getSiteAssocManager().getAssociatedFrom(site.getId());
        //String[] siteIds = getProviderGroupIds(site);
        for (String siteId : siteIds) {
            try {
                Site eachSite = getSiteService().getSite(siteId);
                //only process site if it is set to sync
                String[] unpackedIds = getGroupProvider().unpackId(eachSite.getProviderGroupId());
                List<String> unpackedList = new ArrayList<String>(Arrays.asList(unpackedIds));
                if(unpackedList.contains(site.getId())){
                    processUpdateSiteGroup(site.getId(), eachSite);
                }
            } catch (IdUnusedException e) {
                logger.error("error getting site object", e);
            }
        }
    }
    
    /**
     * @param rootSite
     * @param changingSite
     * @param add
     */
    protected void processUpdateSiteGroup(String siteToSync, Site changingSite) {
        //changingSite.loadAll();
        Collection<Group> groups = changingSite.getGroups();
        boolean needSave = false;
        Group groupToSave = null;
        for(Group group : groups) {
            //Find the group that came from the original site
            String property = group.getProperties().getProperty(MANAGED_GROUP_PROPERTY);
            if(siteToSync.equals(property)) {
                needSave = true;
                group.setProviderGroupId(siteToSync);
                groupToSave = group;
                break;
            }
        }
        
        if (needSave) {
            try {
                logger.debug("BEGIN Saving group: '" + groupToSave.getTitle() + "' in site '" + changingSite.getTitle() + "'");

                getSiteService().save(changingSite);
                logger.debug("END Saving group: '" + groupToSave.getTitle() + "' in site '" + changingSite.getTitle() + "'");
            } catch (IdUnusedException e) {
                logger.error(e);
            } catch (PermissionException e) {
                logger.error(e);
            }
        }
    }

    private String[] getProviderGroupIds(Site site){
        return getGroupProvider().unpackId(site.getProviderGroupId());          
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public GroupProvider getGroupProvider() {
        if(groupProvider == null){
            groupProvider = (org.sakaiproject.authz.api.GroupProvider) ComponentManager.get(GroupProvider.class);           
        }
        return groupProvider;
    }

    public void setEnableListener(boolean enableListener) {
        this.enableListener = enableListener;
    }

    public boolean isEnableListener() {
        return enableListener;
    }

    public void setSiteAssocManager(SiteAssocManager siteAssocManager) {
        this.siteAssocManager = siteAssocManager;
    }

    public SiteAssocManager getSiteAssocManager() {
        return siteAssocManager;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public AuthzGroupService getAuthzGroupService() {
        return authzGroupService;
    }
}