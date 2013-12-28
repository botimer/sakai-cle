/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/metaobj/trunk/metaobj-util/tool-lib/src/java/org/sakaiproject/metaobj/utils/mvc/impl/ListScrollIndexerImpl.java $
 * $Id: ListScrollIndexerImpl.java 130481 2013-10-15 17:36:54Z dsobiera@indiana.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.metaobj.utils.mvc.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.metaobj.utils.mvc.intf.ListScroll;
import org.sakaiproject.metaobj.utils.mvc.intf.ListScrollIndexer;
import org.sakaiproject.metaobj.utils.mvc.intf.ListScrollResultsFilter;
import org.sakaiproject.metaobj.utils.mvc.intf.FilterableListScrollIndexer;

public class ListScrollIndexerImpl implements ListScrollIndexer, FilterableListScrollIndexer {
   protected final transient Log logger = LogFactory.getLog(getClass());
   private int perPage;
   
   //private ListScrollPageProcessor pageProcessor;

   public List indexList(Map request, Map model, List sourceList) {
      return indexList( request, model, sourceList, false );
   }
   
   public List indexList(Map request, Map model, List sourceList, boolean hideOnePageScroll) {
   	return indexList( request, model, sourceList, false, null );
   }
   
   /** 
    ** Create sublist from given list of items to allow paging (i.e. scrolling) through a list
    **
    ** @param request (input) request parameters
    ** @param model (output) presentation parameters
    ** @param sourceList list of items
    ** @param hideOnePageScroll if true, do not display scroll buttons if not needed
    **/
   public List indexList(Map request, Map model, List sourceList, boolean hideOnePageScroll, ListScrollResultsFilter pageFilter) {
      int startingIndex = 0;
      int total = sourceList.size();
      boolean hideRecCounts = false;

      String ensureVisible = (String) request.get(ListScroll.ENSURE_VISIBLE_TAG);

      if (ensureVisible != null) {
         int visibleIndex = Integer.parseInt(ensureVisible);
         int startingPage = (visibleIndex / perPage);
         startingIndex = startingPage * perPage;
      }
      else {
         String newStart = (String) request.get(ListScroll.STARTING_INDEX_TAG);

         if (newStart != null) {
            startingIndex = Integer.parseInt(newStart);
            if (startingIndex < 0) {
               startingIndex = 0;
            }
         }
      }

      if (startingIndex > total) {
         int lastPage = (int) Math.ceil(((double) total) / ((double) perPage));
         lastPage--;
         startingIndex = lastPage * perPage;
      }

      int endingIndex = startingIndex + perPage;

      if (endingIndex > sourceList.size()) {
         endingIndex = sourceList.size();
      }

      int virtualPreviousIndex = ListScroll.NO_VIRTUAL_INDEX;
      int virtualNextIndex = ListScroll.NO_VIRTUAL_INDEX;
      boolean processPreviousFromEnd = false;
      boolean processLastFromEnd = false;
      
      List subList;
      if (pageFilter != null){
      	String reverseList = (String)request.get(ListScroll.REVERSE_PROCESS_LIST_TAG);
      	boolean startAtFront = true;
      	if (reverseList != null && "true".equals(reverseList)) {
      		startAtFront = false;
      	}
      	ListScrollResultBean bean = pageFilter.process(request, model, sourceList, startingIndex, perPage, startAtFront);
      	subList = bean.getItemList();
      	logger.debug("Processed: " + bean.getRecordsProcessed());
      	logger.debug("Skipped: " + bean.getRecordsSkipped());
      	logger.debug("Returned: " + bean.getRecordsReturned());
      	logger.debug("Alt Starting Index: " + bean.getAltStartIndex());
      	startingIndex = bean.getAltStartIndex();
      	virtualNextIndex = startingIndex + bean.getRecordsProcessed();
      	if (!startAtFront) {
      		//need to account for switching to the other direction
      		virtualNextIndex++;
      		virtualPreviousIndex = startingIndex;
      	}
      	else {
      		virtualPreviousIndex = startingIndex - 1;
      	}
      	hideRecCounts = true;
      	processPreviousFromEnd = true;
      	processLastFromEnd = true;
      	logger.debug("Next previous will be: " + virtualPreviousIndex);
      	logger.debug("Next next will be: " + virtualNextIndex);
      }
      else
      	subList = sourceList.subList(startingIndex, endingIndex);
      
      model.put("listScroll", new ListScroll(perPage, sourceList.size(), startingIndex, hideOnePageScroll, hideRecCounts, 
      		virtualPreviousIndex, virtualNextIndex, processPreviousFromEnd, processLastFromEnd));

      return subList;
   }

   public int getPerPage() {
      return perPage;
   }

   public void setPerPage(int perPage) {
      this.perPage = perPage;
   }
//
//	public ListScrollPageProcessor getPageProcessor() {
//	   return pageProcessor;
//   }
//
//	public void setPageProcessor(ListScrollPageProcessor pageProcessor) {
//	   this.pageProcessor = pageProcessor;
//   }
}
