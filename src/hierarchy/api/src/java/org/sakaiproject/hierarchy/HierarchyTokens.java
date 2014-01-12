/**
 * HierarchyTokens.java - hierarchy - 2007 Sep 6, 2007 10:28:58 AM - AZ
 */

package org.sakaiproject.hierarchy;

import java.util.Map;
import java.util.Set;


/**
 * This adds in the ability to define permissions key token searching
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface HierarchyTokens {

   /**
    * Find all nodes for a hierarchy associated with a token key
    * 
    * @param hierarchyId a unique id which defines the hierarchy
    * @param permToken a permissions token key
    * @return a set of nodeIds, empty if no nodes found
    */
   public Set<String> getNodesWithToken(String hierarchyId, String permToken);

   /**
    * Find all the nodes for a hierarchy associated with a set of token keys
    * 
    * @param hierarchyId a unique id which defines the hierarchy
    * @param permTokens an array of permissions token keys
    * @return a map of tokenKey -> set of nodeIds, empty if no nodes found
    */
   public Map<String, Set<String>> getNodesWithTokens(String hierarchyId, String[] permTokens);

}
