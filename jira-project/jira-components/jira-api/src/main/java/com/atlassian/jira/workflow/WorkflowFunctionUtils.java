package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Useful methods for JIRA OSWorkflow conditions and functions. Use the subclasses for real implementations.
 */
public class WorkflowFunctionUtils
{
    public static final String ORIGINAL_ISSUE_KEY = "originalissueobject";
    /**
     * @deprecated - typo in name, please use ORIGINAL_ISSUE_KEY. Deprecated since v4.0
     */
    public static final String ORIGNAL_ISSUE_KEY = "originalissueobject";
    private static final Logger log = Logger.getLogger(WorkflowFunctionUtils.class);

    /**
     * Get the name of the user executing this condition.
     *
     * @param transientVars workflow context - parameters passed to workflow engine
     * @param args workflow's function arguments
     * @return Username, or null if run anonymously.
     * @deprecated Use {@link #getCallerUser(java.util.Map, java.util.Map)} instead. Since v6.0.
     */
    protected String getCallerName(Map transientVars, Map args)
    {
        ApplicationUser user = getCallerUser(transientVars, args);
        return user != null ? user.getUsername() : null;
    }

    /**
     * Get the {@link User} executing this condition.
     *
     * @param transientVars workflow context - parameters passed to workflow engine
     * @param args workflow's function arguments
     * @return The User, or null if run anonymously.
     * @deprecated Use {@link #getCallerUser(java.util.Map, java.util.Map)} instead. Since v6.0.
     */
    protected User getCaller(Map transientVars, Map args)
    {
        return ApplicationUsers.toDirectoryUser(getCallerUser(transientVars, args));
    }

    /**
     * Get the {@link ApplicationUser} executing this condition.
     *
     * @param transientVars workflow context - parameters passed to workflow engine
     * @param args workflow's function arguments
     * @return The ApplicationUser, or null if run anonymously.
     */
    protected ApplicationUser getCallerUser(Map transientVars, Map args)
    {
        return ApplicationUsers.byKey(getCallerKey(transientVars, args));
    }

    /**
     * Get the {@link ApplicationUser} executing this condition.
     *
     * @param transientVars workflow context - parameters passed to workflow engine
     * @param args workflow's function arguments
     * @return The ApplicationUser, or null if run anonymously.
     */
    public static ApplicationUser getCallerUserFromArgs(Map transientVars, Map args)
    {
        return ApplicationUsers.byKey(getCallerKey(transientVars, args));
    }

    /**
     * Get the Key of user executing this condition.
     *
     * @param transientVars workflow context - parameters passed to workflow engine
     * @param args workflow's function arguments
     * @return The user's Key, or null if run anonymously.
     */
    public static String getCallerKey(Map transientVars, Map args)
    {
        //at first, try to fetch userKey from args
        if(args != null){
            String userKey = (String) args.get("userKey");
            if(TextUtils.stringSet(userKey)){
                return userKey;
            }
        }

        //then try to pull it from context
        return WorkflowUtil.getCallerKey(transientVars);

    }

    /**
     * Populate given map of parameters with data which can identify user, in order to be able retrieve {@link
     * ApplicationUser} later via {@link #getCallerUserFromArgs(java.util.Map, java.util.Map)}
     * <p/>
     * If you want to pass parameters explicitly, please use {@link #populateParamsWithUser(java.util.Map, String)}
     *
     * @param params mutable map, which will be populated with user's identifier
     * @param user user, which later should be identified by {@link #getCallerUserFromArgs(java.util.Map,
     * java.util.Map)}
     * @see #getCallerUserFromArgs(java.util.Map, java.util.Map)
     * @since 6.0
     */
    public static void populateParamsWithUser(Map<String, Object>  params, ApplicationUser user)
    {
        populateParamsWithUser(params, ApplicationUsers.getKeyFor(user));
    }

    /**
     * Populate given map of parameters with data which can identify user, in order to be able retrieve {@link
     * ApplicationUser} later via {@link #getCallerUserFromArgs(java.util.Map, java.util.Map)}
     *
     * @param params mutable map to be populated with user's identifier
     * @param userKey user's key to be inserted into params
     * @see #getCallerUserFromArgs(java.util.Map, java.util.Map)
     * @since 6.0
     */
    public static void populateParamsWithUser(Map<String, Object> params, String userKey)
    {
        params.put("userKey", userKey);
    }

}
