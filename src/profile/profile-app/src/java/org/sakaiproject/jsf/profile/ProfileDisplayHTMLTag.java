/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/profile/trunk/profile-app/src/java/org/sakaiproject/jsf/profile/ProfileDisplayHTMLTag.java $
 * $Id: ProfileDisplayHTMLTag.java 105078 2012-02-24 23:00:38Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.profile;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

public class ProfileDisplayHTMLTag extends UIComponentTag
{
	private String value;

	public void setvalue(String value)
	{
		this.value = value;
	}

	public String getvalue()
	{
		return value;
	}

	public String getComponentType()
	{
		return "ProfileDisplayHTML";
	}

	public String getRendererType()
	{
		return "ProfileDisplayHTMLRender";
	}

	protected void setProperties(UIComponent component)
	{
		super.setProperties(component);
		setString(component, "value", value);
	}

	public void release()
	{
		super.release();

		value = null;
	}

	public static void setString(UIComponent component, String attributeName, String attributeValue)
	{
		if (attributeValue == null)
		{
			return;
		}

		if (UIComponentTag.isValueReference(attributeValue))
		{
			setValueBinding(component, attributeName, attributeValue);
		}
		else
		{
			component.getAttributes().put(attributeName, attributeValue);
		}
	}

	public static void setValueBinding(UIComponent component, String attributeName, String attributeValue)
	{
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		ValueBinding vb = app.createValueBinding(attributeValue);
		component.setValueBinding(attributeName, vb);
	}
}