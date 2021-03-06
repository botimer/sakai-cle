/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/metaobj/trunk/metaobj-util/tool-lib/src/java/org/sakaiproject/metaobj/utils/mvc/intf/CancelableController.java $
 * $Id: CancelableController.java 105079 2012-02-24 23:08:11Z ottenhoff@longsight.com $
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

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Dec 15, 2004
 * Time: 9:36:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CancelableController {
   public boolean isCancel(Map request);

   public ModelAndView processCancel(Map request, Map session, Map application,
                                     Object command, Errors errors) throws Exception;
}
