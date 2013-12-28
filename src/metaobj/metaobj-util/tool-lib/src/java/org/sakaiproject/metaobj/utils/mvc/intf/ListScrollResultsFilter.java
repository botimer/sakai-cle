package org.sakaiproject.metaobj.utils.mvc.intf;

import java.util.List;
import java.util.Map;

import org.sakaiproject.metaobj.utils.mvc.impl.ListScrollResultBean;

/**
 * Do some extra processing to filter results down to just a page's worth of data so that 
 * processing doesn't have to happen on the full list somewhere else.
 * @author chrismaurer
 *
 */
public interface ListScrollResultsFilter {

	/**
	 * 
	 * @param request
	 * @param model
	 * @param sourceList
	 * @param startingIndex
	 * @param pageSize
	 * @return
	 */
	public ListScrollResultBean process(Map request, Map model, List sourceList, int startingIndex, int pageSize);
	
	/**
	 * Do the processing
	 * @param request
	 * @param sourceList
	 * @param startingIndex
	 * @param pageSize
	 * @return
	 */
	public ListScrollResultBean process(Map request, Map model, List sourceList, int startingIndex, int pageSize, boolean startAtFront);
}
