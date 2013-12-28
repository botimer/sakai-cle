package org.theospi.portfolio.presentation.control;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.metaobj.security.AuthenticationManager;
import org.sakaiproject.metaobj.shared.model.Id;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.theospi.portfolio.presentation.PresentationFunctionConstants;
import org.theospi.portfolio.presentation.model.Presentation;
import org.theospi.portfolio.presentation.support.PresentationService;
import org.theospi.portfolio.presentation.support.UpdatePresentationValidator;
import org.theospi.portfolio.security.Authorization;
import org.theospi.portfolio.security.AuthorizationFacade;
import org.theospi.portfolio.security.AuthorizationFailedException;

public class UpdatePresentationController extends AbstractCommandController {
	private PresentationService presentationService;
	private AuthorizationFacade authzManager;
	private AuthenticationManager authManager;

	
	public UpdatePresentationController() {
		setCommandClass(Presentation.class);
		setValidator(new UpdatePresentationValidator());
	}
	
	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		if (errors.hasErrors()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You have submitted bad input -- check the API");
			// This call should return a MaV that contains the error information. 
			// return new ModelAndView("editPresentation", errors.getModel());
			return null;
		}

		Boolean requestAccess = null;
		if (request.getParameter("requestAccess") != null) {
			requestAccess = Boolean.valueOf(request.getParameter("requestAccess"));
		}

		Boolean active = null;
		if (request.getParameter("active") != null)
			active = Boolean.valueOf(request.getParameter("active"));
		
		Boolean allowComments = null;
		if (request.getParameter("allowComments") != null)
			allowComments = Boolean.valueOf(request.getParameter("allowComments"));
		
		Boolean searchable = null;
		if (request.getParameter("not_searchable") == null) {
			searchable = Boolean.TRUE;
		}
		else {
			searchable = Boolean.FALSE;
		}
		
		Presentation presentation = (Presentation) command;
		try {
			if (requestAccess != null) {
				Collection<Authorization> viewerAuthzs = getAuthzManager().getAuthorizations(null,
						PresentationFunctionConstants.VIEW_PRESENTATION, presentation.getId());

				if (viewerAuthzs == null) {  // user already has access to the presentation
					return null;
				}

				Collection<Authorization> requestAuthzs = getAuthzManager().getAuthorizations(null,
						PresentationFunctionConstants.REQUEST_VIEW_PRESENTATION, presentation.getId());

				if (requestAuthzs == null) {  // user already has a request in for this presentation
					return null;
				}

				if (Boolean.TRUE.equals(requestAccess)) {
					// set the Request authz
					getAuthzManager().createAuthorization(getAuthManager().getAgent(), 
							PresentationFunctionConstants.REQUEST_VIEW_PRESENTATION, 
							presentation.getId());
				}
				else { // requestAccess = false
					// remove the Request authz
					getAuthzManager().deleteAuthorization(getAuthManager().getAgent(), 
							PresentationFunctionConstants.REQUEST_VIEW_PRESENTATION, 
							presentation.getId());
				}

			}
			else {
				if (!presentationService.updatePresentation(presentation.getId().getValue(), presentation.getName(), presentation.getDescription(), active, allowComments, searchable)) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}
		catch (AuthorizationFailedException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
		return null;
	}
	
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		binder.registerCustomEditor(Id.class, presentationService.getIdCustomEditor());
		binder.setAllowedFields(new String[] {"id", "name", "description"});
	}

	public void setPresentationService(PresentationService presentationService) {
		this.presentationService = presentationService;
	}

	public AuthorizationFacade getAuthzManager() {
		return authzManager;
	}

	public void setAuthzManager(AuthorizationFacade authzManager) {
		this.authzManager = authzManager;
	}

	public AuthenticationManager getAuthManager() {
		return authManager;
	}

	public void setAuthManager(AuthenticationManager authManager) {
		this.authManager = authManager;
	}

}
