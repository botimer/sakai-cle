/**
 * HierarchyUtils.java - hierarchy - 2007 Sep 11, 2007 11:06:45 AM - azeckoski
 */

package org.sakaiproject.hierarchy.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;


/**
 * Simple utils to assist with working with the hierarchy
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class HierarchyUtils {

   /**
    * Create a sorted list of nodes based on a set of input nodes,
    * list goes from root (or highest parent) down to the bottom most node
    * @param nodes
    * @return a list of {@link HierarchyNode}
    */
   public static List<HierarchyNode> getSortedNodes(Collection<HierarchyNode> nodes) {
      List<HierarchyNode> sortedNodes = new ArrayList<HierarchyNode>();
      for (HierarchyNode hierarchyNode : nodes) {
         if (sortedNodes.size() < 1) {
            sortedNodes.add(hierarchyNode);
         } else {
            int i;
            for (i = 0; i < sortedNodes.size(); i++) {
               HierarchyNode sortedNode = sortedNodes.get(i);
               if (sortedNode.parentNodeIds.contains(hierarchyNode.id)) {
                  break;
               }
            }
            sortedNodes.add(i, hierarchyNode);
         }
      }
      return sortedNodes;
   }

   /**
    * Create a set of all the unique child node ids based on the set of supplied nodes,
    * can optionally be limited to return only direct children and to include the supplied node ids
    * @param nodes a collection of {@link HierarchyNode}
    * @param includeSuppliedNodes includes the nodeIds of the supplied collection of nodes in the returned set
    * @param directOnly only use the direct children of each node
    * @return the set of all unique child node ids
    */
   public static Set<String> getUniqueChildNodes(Collection<HierarchyNode> nodes, boolean includeSuppliedNodeIds, boolean directOnly) {
      Set<String> s = new HashSet<String>();
      for (HierarchyNode hierarchyNode : nodes) {
         if (includeSuppliedNodeIds) {
            s.add(hierarchyNode.id);
         }
         if (directOnly) {
            s.addAll(hierarchyNode.directChildNodeIds);
         } else {
            s.addAll(hierarchyNode.childNodeIds);
         }
      }
      return s;
   }

   /**
    * Create a set of all the unique parent node ids based on the set of supplied nodes,
    * can optionally be limited to return only direct parents and to include the supplied node ids
    * @param nodes a collection of {@link HierarchyNode}
    * @param includeSuppliedNodes includes the nodeIds of the supplied collection of nodes in the returned set
    * @param directOnly only use the direct parents of each node
    * @return the set of all unique parent node ids
    */
   public static Set<String> getUniqueParentNodes(Collection<HierarchyNode> nodes, boolean includeSuppliedNodeIds, boolean directOnly) {
      Set<String> s = new HashSet<String>();
      for (HierarchyNode hierarchyNode : nodes) {
         if (includeSuppliedNodeIds) {
            s.add(hierarchyNode.id);
         }
         if (directOnly) {
            s.addAll(hierarchyNode.directParentNodeIds);
         } else {
            s.addAll(hierarchyNode.parentNodeIds);
         }
      }
      return s;
   }

}

