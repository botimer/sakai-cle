/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/metaobj/trunk/metaobj-util/tool-lib/src/java/org/sakaiproject/metaobj/utils/mvc/intf/ListScroll.java $
 * $Id: ListScroll.java 130481 2013-10-15 17:36:54Z dsobiera@indiana.edu $
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

package org.sakaiproject.metaobj.utils.mvc.intf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListScroll {
   protected final transient Log logger = LogFactory.getLog(getClass());

   public static final String STARTING_INDEX_TAG = "listScroll_startingIndex";
   public static final String ENSURE_VISIBLE_TAG = "listScroll_ensureVisibleIndex";
   public static final String REVERSE_PROCESS_LIST_TAG = "listScroll_reverseProcessList";
   public static final int NO_VIRTUAL_INDEX = -99;

   private int total;
   private int perPage;
   private int startingIndex;
   private boolean hideOnePageScroll = false;
   private boolean hideRecCounts = false;
   private int virtualPreviousIndex = NO_VIRTUAL_INDEX;
   private int virtualNextIndex = NO_VIRTUAL_INDEX;
   private boolean processPreviousFromEnd = false;
   private boolean processLastFromEnd = false;
   
   public ListScroll(int perPage, int total, int startingIndex) {
      this.perPage = perPage;
      this.total = total;
      this.startingIndex = startingIndex;
   }

   public ListScroll(int perPage, int total, int startingIndex, boolean hideOnePageScroll) {
      this.perPage = perPage;
      this.total = total;
      this.startingIndex = startingIndex;
      this.hideOnePageScroll = hideOnePageScroll;
   }
   
   public ListScroll(int perPage, int total, int startingIndex, boolean hideOnePageScroll, boolean hideRecCounts, int virtualPreviousIndex, int virtualNextIndex, boolean processPreviousFromEnd, boolean processLastFromEnd) {
      this.perPage = perPage;
      this.total = total;
      this.startingIndex = startingIndex;
      this.hideOnePageScroll = hideOnePageScroll;
      this.hideRecCounts = hideRecCounts;
      this.virtualPreviousIndex = virtualPreviousIndex;
      this.virtualNextIndex = virtualNextIndex;
      this.processPreviousFromEnd = processPreviousFromEnd;
      this.processLastFromEnd = processLastFromEnd;
      
   }

   public int getNextIndex() {
      int nextIndex = startingIndex + perPage;

      if (nextIndex >= total || virtualNextIndex >= total) {
         return -1;
      }
      
      if (virtualNextIndex != NO_VIRTUAL_INDEX) {
      	logger.debug("Returning VirtualNextIndex of " + virtualNextIndex + " instead of regular NextIndex of " + nextIndex);
      	return virtualNextIndex;
      }

      return nextIndex;
   }

   public int getPerPage() {
      return perPage;
   }

   public void setPerPage(int perPage) {
      this.perPage = perPage;
   }

   public int getPrevIndex() {
      int prevIndex = startingIndex - perPage;

      if (virtualPreviousIndex != NO_VIRTUAL_INDEX) {
      	logger.debug("Returning VirtualPreviousIndex of " + virtualPreviousIndex + " instead of regular PreviousIndex of " + prevIndex);
      	return virtualPreviousIndex;
      }
      
      if (prevIndex < 0) {
         return -1;
      }

      return prevIndex;
   }

   public int getStartingIndex() {
      return startingIndex;
   }

   public void setStartingIndex(int startingIndex) {
      this.startingIndex = startingIndex;
   }

   public int getTotal() {
      return total;
   }

   public void setTotal(int total) {
      this.total = total;
   }

   public int getFirstItem() {
      if (total == 0) {
         return 0;
      }
      return startingIndex + 1;
   }

   public int getLastItem() {
      int lastItem = startingIndex + perPage;

      if (lastItem > total) {
         return total;
      }
      return lastItem;
   }
   
   public boolean getHideOnePageScroll() {
      return hideOnePageScroll;
   }

   public void setHideOnePageScroll(boolean hideOnePageScroll) {
      this.hideOnePageScroll = hideOnePageScroll;
   }
   
   public boolean getHideRecCounts() {
      return hideRecCounts;
   }

   public void setHideRecCounts(boolean hideRecCounts) {
      this.hideRecCounts = hideRecCounts;
   }

	public boolean isProcessPreviousFromEnd() {
		return processPreviousFromEnd;
	}

	public void setProcessPreviousFromEnd(boolean processPreviousFromEnd) {
		this.processPreviousFromEnd = processPreviousFromEnd;
	}

	public boolean isProcessLastFromEnd() {
		return processLastFromEnd;
	}

	public void setProcessLastFromEnd(boolean processLastFromEnd) {
		this.processLastFromEnd = processLastFromEnd;
	}

}
