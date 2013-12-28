/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/osp/trunk/wizard/api/src/java/org/theospi/portfolio/wizard/taggable/api/WizardActivityProducer.java $
 * $Id: WizardActivityProducer.java 131591 2013-11-15 20:24:05Z dsobiera@indiana.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.theospi.portfolio.wizard.taggable.api;

import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableActivityProducer;
import org.sakaiproject.taggable.api.TaggableItem;
import org.theospi.portfolio.matrix.model.WizardPage;
import org.theospi.portfolio.matrix.model.WizardPageDefinition;

/**
 * A producer of wizard pages as taggable activities.
 * 
 * @author The Sakai Foundation.
 */
public interface WizardActivityProducer extends TaggableActivityProducer {

	/**
	 * The identifier of this producer.
	 */
	public static final String PRODUCER_ID = WizardActivityProducer.class
			.getName();
	
	/**
     * The key used to look up a value in the ServerConfigurationService if this producer should be enabled
     */
    public static final String PRODUCER_ENABLED_KEY = "wizard.activityProducer.enabled";

	/**
	 * Method to wrap the given wizard page definition as a taggable activity.
	 * 
	 * @param wizardPageDef
	 *            The wizard page definition.
	 * @return The wizard page definition represented as a taggable activity.
	 */
	public TaggableActivity getActivity(WizardPageDefinition wizardPageDef);

	/**
	 * Method to wrap the given wizard page as a taggable item.
	 * 
	 * @param wizardPage
	 *            The wizard page.
	 * @return The wizard page represented as a taggable item.
	 */
	public TaggableItem getItem(WizardPage wizardPage);
}
