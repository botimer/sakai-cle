/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-services/src/java/org/sakaiproject/tool/assessment/integration/helper/ifc/SectionAwareServiceHelper.java $
 * $Id: SectionAwareServiceHelper.java 106463 2012-04-02 12:20:09Z david.horwitz@uct.ac.za $
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


package org.sakaiproject.tool.assessment.integration.helper.ifc;

import java.util.*;


/**
 * Facade to external role and authorization service.
 * based Ray's gradebook code
 */
public interface SectionAwareServiceHelper{
	public boolean isUserAbleToGrade(String siteid, String userUid);
	public boolean isUserAbleToGradeAll(String siteid, String userUid);
	public boolean isUserAbleToGradeSection(String sectionUid, String userUid);
	public boolean isUserAbleToEdit(String siteid, String userUid);
	public boolean isUserGradable(String siteid, String userUid);

	/**
	 * @return
	 *	an EnrollmentRecord list for each student that the current user
	 *  is allowed to grade.
	 */
	public List getAvailableEnrollments(String siteid, String userUid);

	/**
	 * @return
	 *	a CourseSection list for each group that the current user
	 *  belongs to.
	 */
	public List getAvailableSections(String siteid, String userUid);

	/**
	 * The section enrollment list will not be returned unless the user
	 * has access to it.
	 *
	 * @return
	 *  an EnrollmentRecord list for all the students in the given group.
	 */
	public List getSectionEnrollments(String siteid, String sectionUid, String userUid);

	public List getSectionEnrollmentsTrusted(String sectionUid) ;
	
	/**
	 * @param searchString
	 *  a substring search for student name or display UID; the exact rules are
	 *  up to the implementation
	 *
	 * @param optionalSectionUid
	 *  null if the search should be made across all sections
	 *
	 * @return
	 *  an EnrollmentRecord list for all matching available students.
	 */
	public List findMatchingEnrollments(String siteid, String searchString, String optionalSectionUid, String userUid);

 /**
  * @param sectionId
  *
  * @param studentId
  *
  * @param  Role 
  * @return
  *  whether a member belongs to a section under a certain role
  */
	public boolean isSectionMemberInRoleStudent(String sectionId, String studentId);

	
	/**
	 * added by gopalrc - Jan 2008
	 * @param siteid
	 * @param userUid
	 * @return
	 */
	public List getGroupReleaseEnrollments(String siteid, String userUid, String publishedAssessmentId);
	
}

