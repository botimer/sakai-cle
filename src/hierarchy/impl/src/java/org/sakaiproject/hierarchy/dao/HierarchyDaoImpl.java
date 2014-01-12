/******************************************************************************
 * HierarchyDaoImpl.java - created by aaronz on Jun 30, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.hierarchy.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;

/**
 * Implementation of DAO
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyDaoImpl extends HibernateGeneralGenericDao implements HierarchyDao {

    private static Log log = LogFactory.getLog(HierarchyDaoImpl.class);

    public void fixupDatabase() {
        // fix up some of the null fields
        long count = 0;
        count = countBySearch(HierarchyNodeMetaData.class, new Search("isDisabled","", Restriction.NULL) );
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update HierarchyNodeMetaData nm set nm.isDisabled = false where nm.isDisabled is null");
            log.info("Updated " + counter + " HierarchyNodeMetaData.isDisabled fields from null to boolean false");
        }
    }
}
