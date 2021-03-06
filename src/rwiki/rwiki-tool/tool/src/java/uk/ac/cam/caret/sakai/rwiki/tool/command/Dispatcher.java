/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/rwiki/trunk/rwiki-tool/tool/src/java/uk/ac/cam/caret/sakai/rwiki/tool/command/Dispatcher.java $
 * $Id: Dispatcher.java 21065 2007-02-07 00:15:30Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ieb
 *
 */
public interface Dispatcher
{

	/**
	 * @param path
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	void dispatch(String path, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
