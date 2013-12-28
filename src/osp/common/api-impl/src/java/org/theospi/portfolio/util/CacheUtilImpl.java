/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2011 The Sakai Foundation
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

package org.theospi.portfolio.util;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.theospi.portfolio.util.api.CacheUtil;

public class CacheUtilImpl implements CacheUtil {
    private Cache siteCache = null;
    private Cache userCache = null;
    private Cache userSiteGroupCache = null;
    private SiteService siteService = null;
    private UserDirectoryService userDirectoryService = null;

    protected final Log logger = LogFactory.getLog(getClass());

    public void init() {
        logger.info("init()");
    }
    
    private Site getSite(String siteId) {
        Site site = null;
        try {
            Element elem = null;
            if(siteId != null)
                elem = siteCache.get(siteId);
            if(siteCache != null && elem != null) {
                if(elem.getValue() == null)
                    return null;
                return (Site)elem.getValue();
            }
        } catch(CacheException e) {
            logger.warn("the site ehcache had an exception", e);
        }

        try {
            site = getSiteService().getSite(siteId);
        } catch (IdUnusedException e) {
            logger.warn("could not get siteId: " + siteId);
        }

        if(siteCache != null && siteId != null && site != null)
            siteCache.put(new Element(siteId, site));
        return site;
    }

    public String fetchSiteName(String siteId) {
        Site site = getSite(siteId);
        String title = site.getTitle();

        return title;
    }
    
    private String createGroupList(String userId, String siteId) {
        String groupList = "";
        Site site = getSite(siteId);
        Collection<Group> groups = site.getGroupsWithMember(userId);
        if (groups.size() > 0) {
            int count=0;
            for (Group group : groups) {
                if (count==0)
                    groupList = group.getTitle();
                else
                    groupList += ", " + group.getTitle();
                count++;
            }
        }
        return groupList;
    }

    public String fetchGroupList(String userId, String siteId) {
        String groupList = null;
        String key = userId + ":" + siteId;
        
        try {
            Element elem = null;
            if(userId != null && siteId != null)
                elem = userSiteGroupCache.get(key);
            if(userSiteGroupCache != null && elem != null) {
                if(elem.getValue() == null)
                    return null;
                return elem.getValue().toString();
            }
        } catch(CacheException e) {
            logger.warn("the site ehcache had an exception", e);
        }
        
        groupList = createGroupList(userId, siteId);
        
        if(userSiteGroupCache != null && userId != null && siteId != null && groupList != null)
            userSiteGroupCache.put(new Element(key, groupList));
        return groupList;
    }

    public User fetchUser(String userId) throws UserNotDefinedException {
        User user = null;
        try {
            Element elem = null;
            if(userId != null)
                elem = userCache.get(userId);
            if(userCache != null && elem != null) {
                if(elem.getObjectValue() == null)
                    return null;
                return (User)elem.getObjectValue();
            }
        } catch(CacheException e) {
            logger.warn("the user ehcache had an exception", e);
        }

        user = getUserDirectoryService().getUser(userId);

        if(userCache != null && userId != null && user != null)
            userCache.put(new Element(userId, user));
        return user;
    }

    /**
     * @return the siteCache
     */
    public Cache getSiteCache()
    {
        return siteCache;
    }

    /**
     * @param siteCache the siteCache to set
     */
    public void setSiteCache(Cache siteCache)
    {
        this.siteCache = siteCache;
    }

    /**
     * @param userCache the userCache to set
     */
    public void setUserCache(Cache userCache) {
        this.userCache = userCache;
    }

    /**
     * @return the userCache
     */
    public Cache getUserCache() {
        return userCache;
    }

    /**
     * @param userSiteGroupCache the userSiteGroupCache to set
     */
    public void setUserSiteGroupCache(Cache userSiteGroupCache) {
        this.userSiteGroupCache = userSiteGroupCache;
    }

    /**
     * @return the userSiteGroupCache
     */
    public Cache getUserSiteGroupCache() {
        return userSiteGroupCache;
    }

    /**
     * @param siteService the siteService to set
     */
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    /**
     * @return the siteService
     */
    public SiteService getSiteService() {
        return siteService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    /**
     * @return the userService
     */
    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }

}