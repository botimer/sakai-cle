/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/author/ChooseExportTypeListener.java $
 * $Id: ChooseExportTypeListener.java 106463 2012-04-02 12:20:09Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id: ChooseExportTypeListener.java 106463 2012-04-02 12:20:09Z david.horwitz@uct.ac.za $
 */

public class ChooseExportTypeListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ExportAssessmentListener.class);

  public ChooseExportTypeListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    String assessmentId = (String) ContextUtil.lookupParam("assessmentId");
    log.info("ExportAssessmentListener assessmentId="+assessmentId);
    
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
    "assessmentBean");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getBasicInfoOfAnAssessment(assessmentId);
    assessmentBean.setAssessmentId(assessment.getAssessmentBaseId().toString());
    assessmentBean.setTitle(assessment.getTitle());
  }

}
