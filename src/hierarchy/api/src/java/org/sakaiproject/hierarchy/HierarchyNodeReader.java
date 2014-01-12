/******************************************************************************
 * HierarchyProvider.java - created by aaronz on Jun 30, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.hierarchy;

import java.util.Map;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Allows user to get hierarchical node data from the hierarchy
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyNodeReader {

   /**
    * Get the hierarchy root node for a specific hierarchy if it exists
    * @param hierarchyId a unique string which identifies this hierarchy
    * @return the {@link HierarchyNode} representing the root of the hierarchy
    * if found, otherwise returns a null
    */
   public HierarchyNode getRootNode(String hierarchyId);

   /**
    * Get a node based on the unique id,
    * convenience method for {@link #getNodesByIds(String[])}
    * @param nodeId a unique id for a hierarchy node
    * @return the {@link HierarchyNode} representing this node or null if it does not exist
    */
   public HierarchyNode getNodeById(String nodeId);

   /**
    * Get a set of nodes based on their unique ids,
    * <b>NOTE:</b> this method is here for efficiency
    * @param nodeIds unique ids for hierarchy nodes
    * @return a map of nodeId -> {@link HierarchyNode}
    */
   public Map<String, HierarchyNode> getNodesByIds(String[] nodeIds);

   /**
    * Get all the parent nodes for a specific node all the way to the root node, 
    * returns empty set if this is the root node
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param directOnly if true then only include the nodes which are directly connected to this node, 
    * else return every node that is a parent of this node
    * @return a Set of {@link HierarchyNode} objects representing all parent nodes for the specified child,
    * empty set if no parents found
    */
   public Set<HierarchyNode> getParentNodes(String nodeId, boolean directOnly);

   /**
    * Get all children nodes for this node in the hierarchy all the way to the leaves, 
    * will return no nodes if this node has no children
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param directOnly if true then only include the nodes which are directly connected to this node, 
    * else return every node that is a child of this node
    * @return a Set of {@link HierarchyNode} objects representing all children nodes for the specified parent,
    * empty set if no children found
    */
   public Set<HierarchyNode> getChildNodes(String nodeId, boolean directOnly);

}
