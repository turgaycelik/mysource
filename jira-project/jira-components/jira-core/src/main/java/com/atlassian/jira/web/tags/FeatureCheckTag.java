package com.atlassian.jira.web.tags;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import webwork.view.taglib.WebWorkTagSupport;

import javax.servlet.jsp.JspException;

/**
 * Represents a JSP {@link javax.servlet.jsp.tagext.Tag} that is able to include its content if the
 * flag specified in the attribute is enabled.
 *
 * @since v5.2
 */
public class FeatureCheckTag extends WebWorkTagSupport
{
    private String featureKey;
    private Boolean enabled;

    public int doStartTag() throws JspException
    {
        if (!isFeature(isEnabled()))
        {
            return SKIP_BODY;
        }
        else
        {
            return EVAL_BODY_INCLUDE ;
        }
    }

    private boolean isFeature(Boolean enabled)
    {
        return enabled.equals(getFeatureManager().isEnabled(getFeatureKey()));
    }

    private FeatureManager getFeatureManager()
    {
        return ComponentAccessor.getComponent(FeatureManager.class);
    }

    public String getFeatureKey()
    {
        return featureKey;
    }

    public void setFeatureKey(String featureKey)
    {
        this.featureKey = featureKey;
    }

    private Boolean isEnabled()
    {
        if (enabled == null)
        {
            return Boolean.TRUE;
        }
        return enabled;
    }

    public String getEnabled()
    {

        return isEnabled().toString();
    }

    public void setEnabled(String enabled)
    {
        this.enabled = Boolean.valueOf(enabled);
    }
}
