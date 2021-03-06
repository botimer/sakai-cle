/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/rwiki/trunk/rwiki-impl/impl/src/java/uk/ac/cam/caret/sakai/rwiki/component/macros/AnchorMacro.java $
 * $Id: AnchorMacro.java 29047 2007-04-18 07:40:45Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseMacro;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;

public class AnchorMacro extends BaseMacro
{


	public String[] getParamDescription()
	{
		return new String[] { Messages.getString("AnchorMacro.0") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("AnchorMacro.1"); //$NON-NLS-1$ion;
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		writer.write("<a name='"); //$NON-NLS-1$

		char[] nameChars = params.get(0).toCharArray();
		int end = 0;
		for (int i = 0; i < nameChars.length; i++)
		{
			if (Character.isLetterOrDigit(nameChars[i]))
			{
				nameChars[end++] = nameChars[i];
			}
		}
		if (end > 0)
		{
			writer.write(nameChars, 0, end);
		}
		writer.write("' class='anchorpoint'>"); //$NON-NLS-1$
		if (params.getContent() != null)
		{
			writer.write(params.getContent());
		}
		writer.write("</a>"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "anchor"; //$NON-NLS-1$
	}

}
