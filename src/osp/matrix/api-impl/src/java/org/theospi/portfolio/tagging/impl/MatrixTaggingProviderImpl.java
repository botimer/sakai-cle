package org.theospi.portfolio.tagging.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.metaobj.shared.mgt.AgentManager;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.shared.model.Agent;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.siteassociation.api.SiteAssocManager;
import org.sakaiproject.taggable.api.Evaluation;
import org.sakaiproject.taggable.api.EvaluationContainer;
import org.sakaiproject.taggable.api.Link;
import org.sakaiproject.taggable.api.LinkManager;
import org.sakaiproject.taggable.api.Tag;
import org.sakaiproject.taggable.api.TagColumn;
import org.sakaiproject.taggable.api.TagList;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableItem;
import org.sakaiproject.taggable.api.TaggingHelperInfo;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.URLBuilder;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.theospi.portfolio.matrix.MatrixFunctionConstants;
import org.theospi.portfolio.matrix.MatrixManager;
import org.theospi.portfolio.matrix.model.Scaffolding;
import org.theospi.portfolio.matrix.model.ScaffoldingCell;
import org.theospi.portfolio.matrix.model.WizardPage;
import org.theospi.portfolio.matrix.model.WizardPageDefinition;
import org.theospi.portfolio.matrix.model.impl.MatrixContentEntityProducer;
import org.theospi.portfolio.review.mgt.ReviewManager;
import org.theospi.portfolio.review.model.Review;
import org.theospi.portfolio.security.AuthorizationFacade;
import org.theospi.portfolio.shared.model.Node;
import org.theospi.portfolio.tagging.api.MatrixTaggingProvider;
import org.theospi.portfolio.wizard.WizardFunctionConstants;

public class MatrixTaggingProviderImpl implements MatrixTaggingProvider {

	private static final Log logger = LogFactory.getLog(MatrixTaggingProviderImpl.class);
	
	private static ResourceLoader messages = new ResourceLoader("org.theospi.portfolio.matrix.bundle.Messages");
	
	protected TaggingManager taggingManager;
	protected LinkManager linkManager;
	protected SiteAssocManager siteAssocManager;
	private IdManager idManager = null;
	private AuthorizationFacade authzManager = null;
	private AgentManager agentManager = null;
	private MatrixManager matrixManager = null;
	private EntityManager entityManager = null;
	private ReviewManager reviewManager = null;
	private SiteService siteService = null;
	private ServerConfigurationService serverConfigurationService = null;
	private UserDirectoryService userDirectoryService = null;
	
	protected static final String LINK_HELPER = "osp.matrix.link";
	
	
	
	public void init() {
		logger.info("init()");

		// register as a tagging provider
		getTaggingManager().registerProvider(this);
	}
	
	public String getId() {
		return MatrixTaggingProvider.PROVIDER_ID;
	}

	public String getName() {
		return messages.getString("provider_name");
	}
	
	public String getSimpleTextLabel() {
		return messages.getString("provider_text_label");
	}
	
	public String getHelpLabel() {
		return messages.getString("provider_help_label");
	}

	public String getHelpDescription() {
		return messages.getString("provider_help_desc");
	}
	
	public boolean allowViewTags(String context) {
		boolean allow = false;
		List<String> associations = siteAssocManager.getAssociatedFrom(context);
		if (associations != null && associations.size() > 0) {
			allow = true;
		}
		return allow;
	}
	
	public boolean allowGetActivity(String activityRef, String userId, String taggedItem) {
		Agent currentUser = getAgentManager().getAgent(userId);
		Reference ref = getEntityManager().newReference(taggedItem);
		Id pageDefId = getIdManager().getId(ref.getId());
		ScaffoldingCell sCell = getMatrixManager().getScaffoldingCellByWizardPageDef(pageDefId);
		Id scaffoldingId = sCell.getScaffolding().getId();
		Scaffolding scaff = getMatrixManager().getScaffolding(scaffoldingId);
		boolean result = getAuthzManager().isAuthorized(currentUser, MatrixFunctionConstants.REVISE_SCAFFOLDING_ANY, getIdManager().getId(ref.getContext())) ||
		   (scaff.getOwner().equals(currentUser) 
		         && getAuthzManager().isAuthorized(currentUser, MatrixFunctionConstants.REVISE_SCAFFOLDING_OWN, getIdManager().getId(ref.getContext())) ||
			canViewCellContents(activityRef, new String[] {}, userId, taggedItem));
		
		return result;
	}
	
	public boolean allowGetItem(String activityRef, String itemRef, String userId, String taggedItem) {
		String[] itemRefs = {itemRef};
		return allowGetItems(activityRef, itemRefs, userId, taggedItem);
	}
	
	public boolean allowGetItems(String activityRef, String[] itemRefs, String userId, String taggedItem) {
	   return canViewCellContents(activityRef, itemRefs, userId, taggedItem);
	}
	
	private boolean canViewCellContents(String activityRef, String[] itemRefs, String userId, String taggedItem) {
		//make sure item is properly linked and then do perm check

		try {
			Link link = getLinkManager().getLink(activityRef, taggedItem);

			if (link != null) {
				Agent agent = getAgentManager().getAgent(userId);
				Reference ref = getEntityManager().newReference(taggedItem);
				Id pageDefId = getIdManager().getId(ref.getId());
				ScaffoldingCell sCell = getMatrixManager().getScaffoldingCellByWizardPageDef(pageDefId);
				if(getUserDirectoryService().getCurrentUser().getId().equals(userId) ||
						(sCell.isDefaultEvaluators() && getAuthzManager().isAuthorized(agent, MatrixFunctionConstants.EVALUATE_MATRIX, sCell.getScaffolding().getId())) ||
						(!sCell.isDefaultEvaluators() && getAuthzManager().isAuthorized(agent, MatrixFunctionConstants.EVALUATE_MATRIX, sCell.getId())) ||
						(sCell.isDefaultReviewers() && getAuthzManager().isAuthorized(agent, MatrixFunctionConstants.EVALUATE_MATRIX, sCell.getScaffolding().getId())) ||
						(!sCell.isDefaultReviewers() && getAuthzManager().isAuthorized(agent, MatrixFunctionConstants.EVALUATE_MATRIX, sCell.getId())) ||
						(getAuthzManager().isAuthorized(agent, MatrixFunctionConstants.ACCESS_ALL_CELLS, getIdManager().getId(sCell.getScaffolding().getReference())))){
					//SecurityService.pushAdvisor(new MySecurityAdvisor(userId, Arrays.asList(functions), Arrays.asList(itemRefs)));  
					return Boolean.valueOf(true);
				}
			}
		} catch (PermissionException e) {
			logger.warn("Unable to get the link for activity: " + activityRef + " and tagCriteriaRef: " + taggedItem, e);
		}
		return false;
	}

	/**
	 * If there are any associations, allow it
	 * @param activityContext
	 * @return
	 */
	protected boolean allowTagActivities(String activityContext) {
		boolean allow = false;
		List<String> associations = siteAssocManager.getAssociatedFrom(activityContext);
		if (associations != null && associations.size() > 0) {
			allow = true;
		}
		return allow;
	}

	public TaggingHelperInfo getActivityHelperInfo(String activityRef) {
		TaggingHelperInfo helperInfo = null;
		String context = taggingManager.getContext(activityRef);
		if (context != null && allowTagActivities(context)
				&& (taggingManager.getActivity(activityRef, this, null) != null)) {
			Map<String, String> parameterMap = new HashMap<String, String>();
			parameterMap.put(ACTIVITY_REF, activityRef);
			String text = messages.getString("act_helper_text");
			String title = messages.getString("act_helper_title");
			helperInfo = taggingManager.createTaggingHelperInfoObject(LINK_HELPER, text, title,
					parameterMap, this);
		}
		return helperInfo;
	}
	
	public Map<String, TaggingHelperInfo> getActivityHelperInfo(String context, List<String> activityRefs) {
		TaggingHelperInfo helperInfo = null;
		Map<String, TaggingHelperInfo> returnMap = new HashMap<String, TaggingHelperInfo>();
		if (allowTagActivities(context)) {
			
			for (String activityRef : activityRefs) {
				TaggableActivity activity = taggingManager.getActivity(activityRef, this, null);
				if (activity != null && context.equals(activity.getContext())) {
					Map<String, String> parameterMap = new HashMap<String, String>();
					parameterMap.put(ACTIVITY_REF, activityRef);
					String text = messages.getString("act_helper_text");
					String title = messages.getString("act_helper_title");
					helperInfo = taggingManager.createTaggingHelperInfoObject(LINK_HELPER, text, title,
							parameterMap, this);
					returnMap.put(activityRef, helperInfo);
				}
			}
		}
		return returnMap;
	}


	public TaggingHelperInfo getItemHelperInfo(String itemRef) {
		// TODO Auto-generated method stub
		return null;
	}

	public TaggingHelperInfo getItemsHelperInfo(String activityRef) {
		// TODO Auto-generated method stub
		return null;
	}

	public TagList getTags(TaggableActivity activity) {
		List<TagColumn> columns = new ArrayList<TagColumn>();
		columns.add(taggingManager.createTagColumn(TagList.CRITERIA, messages.getString("column_criteria"), messages.getString("column_criteria"), true));
		columns.add(taggingManager.createTagColumn(TagList.PARENT, messages.getString("column_parent"), messages.getString("column_parent_desc"), true));
		columns.add(taggingManager.createTagColumn(TagList.WORKSITE, messages.getString("column_worksite"), messages.getString("column_worksite_desc"), true));
		TagList tagList = taggingManager.createTagList(columns);
		String activityContext = activity.getContext();
		for (String toContext : getSiteAssocManager().getAssociatedFrom(activityContext)) {
			try {
				for (Link link : linkManager.getLinks(activity
						.getReference(), true, toContext)) {
					Tag tag = taggingManager.createTag(link);
					tagList.add(tag);
				}
			} catch (PermissionException pe) {
				logger.error(pe.getMessage(), pe);
			}
		}
		return tagList;
	}

	public void removeTags(TaggableActivity activity)
			throws PermissionException {

		getTaggingManager().removeLinks(activity);
		
	}

	public void removeTags(TaggableItem item) throws PermissionException {
		// TODO Auto-generated method stub
		
	}

	public void transferCopyTags(TaggableActivity fromActivity,
			TaggableActivity toActivity) throws PermissionException {
		// TODO Auto-generated method stub
		
	}
	
	private boolean canHaveEvaluations(WizardPageDefinition wpd, Scaffolding scaffolding) {
		if (wpd.isDefaultItemLevelEval()) {
			if (scaffolding.isItemLevelEvals() && scaffolding.isEnableItemLevelEvalsInLinkedTools())
				return true;
		}
		else {
			if (wpd.isItemLevelEvals() && wpd.isEnableItemLevelEvalsInLinkedTools())
				return true;
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public EvaluationContainer getEvaluationContainer(String itemRef, String tagRef, String currentUserId, String itemRefOwner) {
		List<Evaluation> list = new ArrayList<Evaluation>();
		EvaluationContainer evalContainer = taggingManager.createEvaluationContainer();
		
		Map<String, String> addParams = new HashMap<String, String>();
		
		logger.debug("ItemRef: " + itemRef);
		logger.debug("TagRef: " + tagRef);
		
		Reference ref = getEntityManager().newReference(tagRef);
		String siteId = ref.getContext();
		
		String pageDefId = ref.getId();
		WizardPageDefinition wpd = getMatrixManager().getWizardPageDefinition(getIdManager().getId(pageDefId));
		
		WizardPage page = getMatrixManager().getPageByPageDefAndUser(getIdManager().getId(pageDefId), getAgentManager().getAgent(itemRefOwner));
		if (page == null)
			return evalContainer;
		
		List<Review> reviews = getReviewManager().getReviewsByItemAndType(itemRef, Review.ITEM_LEVEL_EVAL_TYPE, 
				page.getId().getValue(), siteId, MatrixContentEntityProducer.MATRIX_PRODUCER);
		
		ScaffoldingCell sc = getMatrixManager().getScaffoldingCellByWizardPageDef(wpd.getId());
		
		//Make sure scaffolding is loaded up and not lazy
		Scaffolding scaffolding = getMatrixManager().getScaffolding(sc.getScaffolding().getId());
		
		//TODO Is it okay to use false here for now?
		boolean isWizard = false;
		
		boolean wizardCanEval = getAuthzManager().isAuthorized(WizardFunctionConstants.EVALUATE_WIZARD, 
				getIdManager().getId(siteId));

		boolean matrixCanEvaluate = getMatrixManager().hasPermission(
				sc.isDefaultEvaluators() ? 
						scaffolding.getId() : 
							wpd.getId(),
							getIdManager().getId(siteId),
							MatrixFunctionConstants.EVALUATE_MATRIX);
		
		Site site = null;
		String siteTitle = "";
		try {
			site = getSiteService().getSite(siteId);
			siteTitle = site.getTitle();
		} catch (IdUnusedException e) {
			logger.debug("Unable to find site: " + e.getId());
		}
		
		String placement = site.getToolForCommonId("osp.matrix").getId();
		String urlBase = getServerConfigurationService().getToolUrl() + Entity.SEPARATOR + placement + Entity.SEPARATOR;

		String addEditView = "osp.review.processor.helper/reviewHelper.osp";
		
		Map<String, String> commonParams = new HashMap<String, String>();
		commonParams.put("1", "1");
		commonParams.put("page_id", page.getId().getValue());
		commonParams.put("org_theospi_portfolio_review_type", String.valueOf(Review.ITEM_LEVEL_EVAL_TYPE));
		commonParams.put("process_type_key", "page_id");
		if (wpd.isDefaultItemLevelEval())
			commonParams.put("scaffoldingId", scaffolding.getId().getValue());

		if (!page.getOwner().getId().getValue().equals(currentUserId) || 
				(page.getOwner().getId().getValue().equals(currentUserId) && 
						(!wpd.isHideItemLevelEvals() && !wpd.isDefaultItemLevelEval() || 
								(!scaffolding.isHideItemLevelEvals() && wpd.isDefaultItemLevelEval())
						) 
				)) {

			for (Review review : reviews) {
				if (!page.getId().getValue().equals(review.getParent())) {
					logger.debug("Skipping review with wrong parent: " + review.getParent());
				}
				else {

					Node node = review.getReviewContentNode();
					Date lastMod = node.getTechnicalMetadata().getLastModified();
					String evalTitle = node.getDisplayName();
					Agent owner = node.getTechnicalMetadata().getOwner();

					Map<String, String> editParams = new HashMap<String, String>();
					Map<String, String> removeParams = new HashMap<String, String>();

					editParams.putAll(commonParams);
					editParams.put("current_review_id", review.getReviewContentNode().getResource().getId());
					editParams.put("review_id", review.getId().getValue());

					removeParams.putAll(commonParams);
					if (wpd.isDefaultItemLevelEval())
						removeParams.put("formDefId", scaffolding.getItemLevelEvaluationDevice().getValue());
					else
						removeParams.put("formDefId", wpd.getItemLevelEvaluationDevice().getValue());
					removeParams.put("current_form_id", review.getReviewContentNode().getResource().getId());
					removeParams.put("review_id", review.getId().getValue());
					removeParams.put("submit", "deleteReview");

					URLBuilder editUrl = getTaggingManager().createURLBuilder(urlBase, addEditView, editParams);

					String removeView = "osp.wizard.page.contents.helper/formDelete.osp";
					URLBuilder removeUrl = getTaggingManager().createURLBuilder(urlBase, removeView, removeParams);

					Evaluation eval = getTaggingManager().createEvaluation(editUrl, removeUrl);

					eval.setSiteId(review.getSiteId());
					eval.setSiteTitle(siteTitle);
					eval.setLastModDate(lastMod);
					eval.setCreatedById(owner.getId().getValue());
					eval.setCreatedByName(owner.getDisplayName());
					eval.setEvalItemTitle(evalTitle);
					eval.setEvalItemURL(review.getReviewContentNode().getExternalUri());

					boolean canModEvals = getCanModEvals(matrixCanEvaluate, wizardCanEval, isWizard, siteId, owner, currentUserId);
					eval.setCanModifyEvaluation(canModEvals);
					eval.setCanRemoveEvaluation(canModEvals);

					boolean canViewEvals = canViewEvaluation(scaffolding.getReference(), page, wizardCanEval, isWizard, owner, currentUserId);
					eval.setCanViewEvaluation(canViewEvals);
					
					logger.debug("Adding review with parent: " + review.getParent());
					list.add(eval);
				}
			}
		}
		
		boolean canHaveEvaluations = canHaveEvaluations(wpd, scaffolding);
		evalContainer.setCanHaveEvaluations(canHaveEvaluations);
		
		boolean canAddEvals = getCanAddEvals(wpd, matrixCanEvaluate, isWizard, scaffolding);
		evalContainer.setCanAddEvaluation(canAddEvals);
		
		boolean isHideItemLevelEvaluations = IsHideItemLevelEvaluations(wpd, scaffolding, page, currentUserId);
		evalContainer.setIsHideItemLevelEvaluations(isHideItemLevelEvaluations);
		
		addParams.putAll(commonParams);		
		addParams.put("isWizard", Boolean.valueOf(isWizard).toString());

		//Scaffolding id and name (possibly wizard if we can be there)
		addParams.put("objectId", scaffolding.getId().getValue());
		addParams.put("objectTitle", scaffolding.getTitle());
		addParams.put("itemId", itemRef);
		
		URLBuilder addUrlBuilder = getTaggingManager().createURLBuilder(urlBase, addEditView, addParams);
		evalContainer.setAddURLBuilder(addUrlBuilder);
		evalContainer.setEvaluations(list);
		
		return evalContainer;
	}
	
	/**
	 * Convenience method so it doesn't clutter up the getEvaluations method quite so much
	 * @param cell
	 * @param matrixCanEvaluate
	 * @param isWizard
	 * @param scaffolding
	 * @return
	 */
	private boolean getCanAddEvals(WizardPageDefinition wpd, boolean matrixCanEvaluate, boolean isWizard, Scaffolding scaffolding) {
		boolean canAddEvals = false;
		canAddEvals=(!isWizard && matrixCanEvaluate) && 
			((wpd.getItemLevelEvaluationDevice() != null && 
					!wpd.isDefaultItemLevelEval()) 
			|| (scaffolding.getItemLevelEvaluationDevice() != null && wpd.isDefaultItemLevelEval()));

			return canAddEvals;
	}
	
	/**
	 * Convenience method so it doesn't clutter up the getEvaluations method quite so much
	 * @param cell
	 * @param matrixCanEvaluate
	 * @param wizardCanEval
	 * @param isWizard
	 * @param siteId
	 * @param owner
	 * @param currentUserId
	 * @return
	 */
	private boolean getCanModEvals(boolean matrixCanEvaluate, boolean wizardCanEval, boolean isWizard, String siteId, Agent owner, String currentUserId) {
		boolean canModEvals = false;
		
		canModEvals = ((!isWizard && matrixCanEvaluate) || (isWizard && wizardCanEval)) && owner.getId().getValue().equals(currentUserId);
		return canModEvals;
	}
	
	/**
	 * Convenience method so it doesn't clutter up the getEvaluations method quite so much
	 * @param scaffoldingRef
	 * @param cell
	 * @param wizardCanEval
	 * @param isWizard
	 * @param reviewOwner
	 * @param currentUserID
	 * @return
	 */
	private boolean canViewEvaluation(String scaffoldingRef, WizardPage page, boolean wizardCanEval, boolean isWizard, Agent reviewOwner, String currentUserID) {
		boolean canView = false;
		boolean canViewOtherEvals = getAuthzManager().isAuthorized(MatrixFunctionConstants.VIEW_EVAL_OTHER, 
				getIdManager().getId(scaffoldingRef));

		canView = reviewOwner.getId().getValue().equals(currentUserID) || 
			(canViewOtherEvals && !isWizard) ||
			(wizardCanEval && isWizard) ||
			(page.getOwner().getId().getValue().equals(currentUserID));

		return canView;
	}

    private boolean IsHideItemLevelEvaluations(WizardPageDefinition wpd, Scaffolding scaffolding, WizardPage page, String currentUserId) {
        if (wpd.isDefaultItemLevelEval()) {
            if (scaffolding.isItemLevelEvals() && scaffolding.isEnableItemLevelEvalsInLinkedTools() 
                    && scaffolding.isHideItemLevelEvals() && page.getOwner().getId().getValue().equals(currentUserId))
                return true;
        }
        else {
            if (wpd.isItemLevelEvals() && wpd.isHideItemLevelEvals() && page.getOwner().getId().getValue().equals(currentUserId))
                return true;
        }
        return false;
    }

    public TaggingManager getTaggingManager() {
		return taggingManager;
	}

	public void setTaggingManager(TaggingManager taggingManager) {
		this.taggingManager = taggingManager;
	}

	public LinkManager getLinkManager()
	{
		return linkManager;
	}

	public void setLinkManager(LinkManager linkManager)
	{
		this.linkManager = linkManager;
	}

	public SiteAssocManager getSiteAssocManager()
	{
		return siteAssocManager;
	}

	public void setSiteAssocManager(SiteAssocManager siteAssocManager)
	{
		this.siteAssocManager = siteAssocManager;
	}

	public IdManager getIdManager() {
		return idManager;
	}

	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}

	public AuthorizationFacade getAuthzManager() {
		return authzManager;
	}

	public void setAuthzManager(AuthorizationFacade authzManager) {
		this.authzManager = authzManager;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setMatrixManager(MatrixManager matrixManager) {
		this.matrixManager = matrixManager;
	}

	public MatrixManager getMatrixManager() {
		return matrixManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public ReviewManager getReviewManager() {
		return reviewManager;
	}

	public void setReviewManager(ReviewManager reviewManager) {
		this.reviewManager = reviewManager;
	}

	/**
	 * @param siteService the siteService to set
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * @return the siteService
	 */
	public SiteService getSiteService() {
		return siteService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}
}
