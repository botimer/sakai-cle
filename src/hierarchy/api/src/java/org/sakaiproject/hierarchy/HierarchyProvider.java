/******************************************************************************
 * HierarchyProvider.java - created by aaronz on Jul 1, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.hierarchy;

/**
 * This interface provides methods to get hierarchical node data into sakai
 * for use in determining the structure above sites/groups related to
 * adminstration and access to data and control of permissions
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyProvider extends HierarchyNodeReader {

   public static final String HIERARCHY_PERM_NODE_UPDATE = "perm_node_update";
   public static final String HIERARCHY_PERM_NODE_REMOVE = "perm_node_remove";

   /**
    * Determine if a user has a specific hierarchy permission at a specific hierarchy node
    * <br/>The actual permissions this should handle are shown at the top of this class
    * 
    * @param userId the internal user id (not username)
    * @param nodeId a unique id for a hierarchy node
    * @param hierarchyPermConstant a HIERARCHY_PERM_NODE constant
    * @return true if the user has this permission, false otherwise
    */
   public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant);

}
