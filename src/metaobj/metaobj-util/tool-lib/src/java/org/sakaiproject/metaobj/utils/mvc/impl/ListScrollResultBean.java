package org.sakaiproject.metaobj.utils.mvc.impl;

import java.util.List;

public class ListScrollResultBean {
	private List itemList;
	private int startIndex;
	private int altStartIndex;
	private int recordsProcessed;
	private int recordsSkipped;
	private int recordsReturned;
	
	public ListScrollResultBean () {
		
	}
	
	public ListScrollResultBean(List itemList, int startIndex, int recordsProcessed, int recordsSkipped, int recordsReturned) {
		this.itemList = itemList;
		this.startIndex = startIndex;
		this.recordsProcessed = recordsProcessed;
		this.recordsSkipped = recordsSkipped;
		this.recordsReturned = recordsReturned;
		this.altStartIndex = startIndex;
	}

	public List getItemList() {
		return itemList;
	}

	public void setItemList(List itemList) {
		this.itemList = itemList;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getAltStartIndex() {
	   return altStartIndex;
   }

	public void setAltStartIndex(int altStartIndex) {
	   this.altStartIndex = altStartIndex;
   }

	public int getRecordsProcessed() {
		return recordsProcessed;
	}

	public void setRecordsProcessed(int recordsProcessed) {
		this.recordsProcessed = recordsProcessed;
	}

	public int getRecordsSkipped() {
		return recordsSkipped;
	}

	public void setRecordsSkipped(int recordsSkipped) {
		this.recordsSkipped = recordsSkipped;
	}

	public int getRecordsReturned() {
		return recordsReturned;
	}

	public void setRecordsReturned(int recordsReturned) {
		this.recordsReturned = recordsReturned;
	}
	

}
