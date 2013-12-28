package org.theospi.portfolio.tagging.api;

import org.sakaiproject.taggable.api.EvaluationProvider;
import org.sakaiproject.taggable.api.TaggingProvider;

public interface MatrixTaggingProvider extends TaggingProvider, EvaluationProvider {

	
	/**
	 * The identifier of this provider.
	 */
	public static final String PROVIDER_ID = MatrixTaggingProvider.class.getName();
	
	public static final String ACTIVITY_REF = "activityRef";
	
}
