/******************************************************************************
 * Hierarchy.java - created by aaronz on 30 June 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.hierarchy;

import org.sakaiproject.hierarchy.model.HierarchyNode;


/**
 * This service interface defines the capabilities of the hierarchy system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyService extends HierarchyNodeReader, HierarchyNodeWriter, HierarchyTokens, HierarchyPermissions {

   /**
    * Creates a new hierarchy with the unique id specified, exception if this id is already used
    * @param hierarchyId a unique id which defines the hierarchy
    * @return the object representing the root node of the new hierarchy
    */
   public HierarchyNode createHierarchy(String hierarchyId);

   /**
    * Sets the root node of this hierarchy, note that although a hierarchy might have multiple
    * nodes at the top of the hierarchy, it always has a primary node which is considering the
    * "entry point" into the hierarchy<br/>
    * A node must have no parents to be set to the root node<br/>
    * The first node added to a hierarchy becomes the root node by default
    * @param hierarchyId a unique id which defines the hierarchy
    * @param nodeId a unique id for a hierarchy node
    * @return the object representing the node which is now the root node
    */
   public HierarchyNode setHierarchyRootNode(String hierarchyId, String nodeId);

   /**
    * Completely and permanantly destroy a hierarchy and all related nodes
    * @param hierarchyId a unique id which defines the hierarchy
    */
   public void destroyHierarchy(String hierarchyId);

}
