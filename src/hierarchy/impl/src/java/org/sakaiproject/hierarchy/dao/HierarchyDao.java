/******************************************************************************
 * HierarchyDao.java - created by aaronz on Jun 30, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.hierarchy.dao;

import org.sakaiproject.genericdao.api.GeneralGenericDao;

/**
 * DAO for access to the database for entity broker internal writes
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyDao extends GeneralGenericDao {

    /**
     * This will apply migration type fixes to the database as needed
     */
    public void fixupDatabase();

}
