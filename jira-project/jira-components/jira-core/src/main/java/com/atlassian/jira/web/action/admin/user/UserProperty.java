package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;

/**
 * This represents a key:value pair property for a user.
 * For example phonenumber:+61 2 9288 5467
 * Key will be stored through with a META_PROPERTY_PREFIX
 * User: donna
 * Date: Jun 14, 2006
 * Time: 11:52:00 AM
 */
public abstract class UserProperty extends ViewUser
{
    protected String key;
    protected String value;
    protected static final String KEY_PARAM_NAME = "key";
    protected static final String VALUE_PARAM_NAME = "value";

    public UserProperty(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, UserPropertyManager userPropertyManager, UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
    }

    public String getTrueKey()
    {
        return UserUtil.META_PROPERTY_PREFIX + key;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
