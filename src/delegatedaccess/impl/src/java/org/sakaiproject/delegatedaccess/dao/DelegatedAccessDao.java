package org.sakaiproject.delegatedaccess.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DelegatedAccessDao {

	public List<String> getDistinctSiteTerms(String termField);
	
	public String getSiteProperty(String propertyName, String siteId);
	
	public void updateSiteProperty(String[] siteIds, String propertyName, String propertyValue);

	public void removeSiteProperty(String[] siteIds, String propertyName);
	
	/**
	 * returns a Map of -> {siteRef, {nodeId, nodeId ...}}
	 * 
	 * @param siteRef
	 * @param hierarchyId
	 * @return
	 */
	public Map<String, List<String>> getNodesBySiteRef(String[] siteRef, String hierarchyId);
	
	public List<String> getEmptyNonSiteNodes(String hierarchyId);
	
	/**
	 * returns a list of {siteId, title} for sites returned in search
	 * if you search for instructorsIds as well, then the results will be {siteId, title, userId}
	 * 
	 * @param titleSearch
	 * @param propsMap
	 * @param instructorIds
	 * @param instructorType
	 * @param publishedOnly
	 * @return
	 */
	public List<String[]> searchSites(String titleSearch, Map<String, String> propsMap, String[] instructorIds, String instructorType, boolean publishedOnly);
	
	/**
	 * returns a list of {siteId, map{name->value}} for the site ids and properties searched
	 * @param props
	 * @param siteIds
	 * @return
	 */
	public Map<String, Map<String, String>> searchSitesForProp(String[] props, String[] siteIds);
	
	/**
	 * When a node is deleted or a hierarchy is destroyed, HierarchyService just orphans the permissions table data
	 * instead of deleting it.  This is an issue in Shopping Period job since it destroys the Shopping Period
	 * Hierarchy every time its ran
	 */
	public void cleanupOrphanedPermissions();
	
	/**
	 * returns a map of {nodeId -> {permission, permission...}) for the given user
	 * if a user doesn't have permissions for a node, it won't show up in the map
	 * 
	 * @param userId
	 * @param nodeIds
	 * @return
	 */
	public Map<String, Set<String>> getNodesAndPermsForUser(String userId, String[] nodeIds);
	
	/**
	 * Returns a subset of sites that are active.  This requires an external feature that populates a
	 * tables named CMS_ACTIVATED
	 * 
	 * DAC-40 Highlight Inactive Courses in site search
	 * requires the job "InactiveCoursesJob" attached in the jira
	 *
	 * @param siteIds
	 * @return
	 */
	public List<String> findActiveSites(String[] siteIds);
	
	/**
	 * Deletes the .anon and .auth roles for all sites
	 * @param siteRef
	 */
	public void removeAnonAndAuthRoles(String[] siteRefs);
	
	/**
	 * 
	 * @param fromRealm
	 * @param fromRole
	 * @param toRealm
	 * @param toRole
	 */
	public void copyRole(String fromRealm, String fromRole, String[] toRealm, String toRole);
	
	/**
	 * returns a list of user ids for users who have at least one of the following permissions in any node:
	 * site.visit, accessAdmin, or shoppingAdmin
	 * @return
	 */
	public List<String> getDelegatedAccessUsers();
	
	/**
	 * returns a list of site id which have have the Delegated Access tool
	 * @param siteIds
	 * @return
	 */
	public List<String> getSitesWithDelegatedAccessTool(String[] siteIds);
}
