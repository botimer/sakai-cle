/**********************************************************************************
* $URL:https://source.sakaiproject.org/svn/osp/trunk/presentation/tool/src/java/org/theospi/portfolio/presentation/control/AbstractPresentationController.java $
* $Id:AbstractPresentationController.java 9134 2006-05-08 20:28:42Z chmaurer@iupui.edu $
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.theospi.portfolio.presentation.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.metaobj.security.AuthenticationManager;
import org.sakaiproject.metaobj.shared.mgt.AgentManager;
import org.sakaiproject.metaobj.shared.mgt.HomeFactory;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.shared.model.Agent;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.metaobj.utils.mvc.intf.Controller;
import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.theospi.portfolio.presentation.PresentationManager;
import org.theospi.portfolio.presentation.model.PresentationTemplate;
import org.theospi.portfolio.security.AuthorizationFacade;
import org.theospi.portfolio.shared.model.Node;
import org.theospi.utils.mvc.impl.servlet.AbstractFormController;

abstract public class AbstractPresentationController extends AbstractFormController implements Controller {
   private AgentManager agentManager;
   private PresentationManager presentationManager;
   private AuthenticationManager authManager;
   private HomeFactory homeFactory;
   protected final Log logger = LogFactory.getLog(getClass());
   //private FileArtifactFinder fileArtifactFinder;
   private IdManager idManager;
   private Collection mimeTypes;
   private AuthorizationFacade authzManager;
   private WorksiteManager worksiteManager;
   private SiteService siteService;
   private AddTemplateController addTemplateController;

   protected GroupComparator groupComparator = new GroupComparator();

   protected PresentationTemplate getActiveTemplate(Map session){
      //AddTemplateController addTemplateController = (AddTemplateController)ComponentManager.getInstance().get("addTemplateController");
      return (PresentationTemplate) session.get(getAddTemplateController().getFormAttributeName());
   }
   
   /**
    *
    * @return true is current agent is a maintainer in the current site
    */
   protected Boolean isMaintainer(){
      return Boolean.valueOf(getAuthzManager().isAuthorized(WorksiteManager.WORKSITE_MAINTAIN,
            getIdManager().getId(ToolManager.getCurrentPlacement().getContext())));
   }

   protected Node loadNode(Id nodeId) {
      return getPresentationManager().getNode(nodeId);
   }

	/** Return list of all groups associated with the given site
	 **/   
	protected List<GroupWrapper> getGroupList( String siteId, Map request ) {
		List<GroupWrapper> groupsList = new ArrayList<GroupWrapper>();
		Site site;

		try {
			site = getSiteService().getSite(siteId);
		}
		catch ( Exception e ) {
			logger.warn(e.toString());
			return groupsList;
		}

		Collection<Group> groups = site.getGroups();
		String selectedGroup = (String)request.get("groups");
		for (Group group : groups) {
			boolean checked = false;
			if ( selectedGroup == null )
				selectedGroup = group.getId();
			if ( selectedGroup.equals(group.getId()) )
				checked = true;
			groupsList.add(new GroupWrapper( group, checked ));
		}

		Collections.sort(groupsList, groupComparator);
		return groupsList;
	}

	/** Return list of site users (not yet shared) users, optionally filtered by specified group
	 **/
	protected List<Agent> getFilteredMembersList( String siteId, List<GroupWrapper> groupList ) {
		Site site;
		Set<Member> members = new HashSet<Member>();
		List<Agent> memberList = new ArrayList<Agent>();

		try {
			site = getSiteService().getSite(siteId);
		} 
		catch (Exception e) {
			logger.warn(e.toString());
			return memberList;
		}

		// Find members of selected groups
		if ( groupList != null ) {
			for ( GroupWrapper group : groupList ) {
				if ( group.getChecked() )
					members.addAll( site.getGroup( group.getId()).getMembers() );
			}
		}

		// If no groups are available or selected
		if ( members.size() == 0 ) 
			members = site.getMembers(); 

		for (Member member : members) {
			String userId = member.getUserId();

			// Check for a null agent since the site.getMembers() will return member records for deleted users
			Agent agent = getAgentManager().getAgent(userId);
			if (agent != null && agent.getId() != null) 
				memberList.add(agent);
		}

		return memberList;
	}

	/**
	 ** Check if presentation's worksite has groups defined and return true/false
	 **/
	protected Boolean getHasGroups( String siteId ) {
		try {
			Site site = getSiteService().getSite(siteId);
			return new Boolean( site.hasGroups() );
		}
		catch (Exception e) {
			logger.warn(e.toString());
      }
		return new Boolean(false);
	}

   public WorksiteManager getWorksiteManager() {
      return worksiteManager;
   }

   public void setWorksiteManager(WorksiteManager worksiteManager) {
      this.worksiteManager = worksiteManager;
   }

   public PresentationManager getPresentationManager() {
      return presentationManager;
   }

   protected Collection getHomes() {
      return getHomeFactory().getHomes().entrySet();
   }

   public void setPresentationManager(PresentationManager presentationManager) {
      this.presentationManager = presentationManager;
   }

   public AuthenticationManager getAuthManager() {
      return authManager;
   }

   public void setAuthManager(AuthenticationManager authManager) {
      this.authManager = authManager;
   }

   public AgentManager getAgentManager() {
      return agentManager;
   }

   public void setAgentManager(AgentManager agentManager) {
      this.agentManager = agentManager;
   }

   public HomeFactory getHomeFactory() {
      return homeFactory;
   }

   public void setHomeFactory(HomeFactory homeFactory) {
      this.homeFactory = homeFactory;
   }

   //public FileArtifactFinder getFileArtifactFinder() {
   //   return fileArtifactFinder;
   //}

   //public void setFileArtifactFinder(FileArtifactFinder fileFinder) {
   //   this.fileArtifactFinder = fileFinder;
   //}

   public IdManager getIdManager() {
      return idManager;
   }

   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
   }

   public Collection getMimeTypes() {
      return mimeTypes;
   }

   public void setMimeTypes(Collection mimeTypes) {
      this.mimeTypes = mimeTypes;
   }

   public AuthorizationFacade getAuthzManager() {
      return authzManager;
   }

   public void setAuthzManager(AuthorizationFacade authzManager) {
      this.authzManager = authzManager;
   }

   public AddTemplateController getAddTemplateController() {
      return addTemplateController;
   }

   public void setAddTemplateController(AddTemplateController addTemplateController) {
      this.addTemplateController = addTemplateController;
   }

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService( SiteService siteService) {
		this.siteService = siteService;
	}

	/** Comparator for sorting GroupWrapper objects by title
	 **/
	public class GroupComparator implements Comparator<GroupWrapper> {
		public int compare(GroupWrapper o1, GroupWrapper o2) {
			return o1.getTitle().compareTo( o2.getTitle() );
		}
	}


	/** Wrap Group class to support getChecked() method
	 **/
	public class GroupWrapper {
		private Group group;
		private boolean checked;

		public GroupWrapper( Group group, boolean checked ) {
			this.group = group;
			this.checked = checked;
		}
		public void setChecked( boolean checked ) {
			this.checked = checked;
		}
		public boolean getChecked() {
			return checked;
		}
		public String getId() {
			return group.getId();
		}
		public String getTitle() {
			return group.getTitle();
		}
	}
}
