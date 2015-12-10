package com.atlassian.jira.web.dispatcher;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;
import webwork.config.util.ActionInfo;
import webwork.dispatcher.ActionResult;

import java.util.Collections;
import java.util.Map;

/**
 * This class an action to be annotated with 'ActionViewData' to provide view data or it can specified a
 * "context-provider" attribute on the view and we will instantiate one of those to get context data
 */
class ActionViewDataSupport
{
    private final ActionViewDataAnnotationSupport viewDataAnnotationSupport = new ActionViewDataAnnotationSupport();

    public Map<String, Object> getData(final ActionResult actionResult, final ActionInfo.ViewInfo viewInfo)
    {
        Map<String, Object> data = Maps.newHashMap();
        data.putAll(extractAnnotations(actionResult));
        return Collections.unmodifiableMap(data);
    }

    /**
     * Called to examine the action object for special annotations that indicate that variables should go into the data
     * map for the view
     *
     * @param actionResult the result of the action invocation
     * @return a map of data they want
     */
    private Map<String, Object> extractAnnotations(final ActionResult actionResult)
    {
        return viewDataAnnotationSupport.getData(actionResult.getResult(), getAction(actionResult));
    }

    @VisibleForTesting
    Action getAction(final ActionResult actionResult)
    {
        return actionResult.getFirstAction();
    }
}
