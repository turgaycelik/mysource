package com.atlassian.jira.permission;

import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.security.type.SingleUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class WorkflowPermissionFactory
{
    public static final String PREFIX = "jira.permission.";
    public static final String PREFIX_PARENT = PREFIX + "subtasks.";

    private static final Logger log = Logger.getLogger(WorkflowPermissionFactory.class);
    private PermissionTypeManager permTypeManager;
    private UserManager userManager;

    public WorkflowPermissionFactory(PermissionTypeManager permTypeManager, UserManager userManager)
    {
        this.permTypeManager = permTypeManager;
        this.userManager = userManager;
    }

    public List<WorkflowPermission> getWorkflowPermissions(PermissionContext ctx, int permissionId, boolean isParent)
    {
        StepDescriptor relevantStepDescriptor = ctx.getRelevantStepDescriptor();

        if (relevantStepDescriptor == null)
        {
            return Collections.emptyList();
        }

        Map<String, String> metaAttributes = relevantStepDescriptor.getMetaAttributes();
        List<WorkflowPermission> perms = new ArrayList<WorkflowPermission>(metaAttributes.size());
        for (Map.Entry<String, String> metaEntry : metaAttributes.entrySet())
        {
            WorkflowPermission perm = createWorkflowPermission(permissionId, isParent, metaEntry.getKey(), metaEntry.getValue());
            if (perm != null) perms.add(perm);
        }
        return perms;
    }

    /**
     * Create a {@link DefaultWorkflowPermission} from a meta attribute key:value pair if it grants a certain permission
     *
     * @param relevantPermission Permission we're interested in
     * @param isParent
     * @param metaKey Key, eg. 'jira.permission.comment.group'
     * @param metaValue Value, eg. 'jira-users'
     * @return A DefaultWorkflowPermission, or null if the meta key doesn't match relevantPermission.
     * @throws IllegalArgumentException If metaKey does not correctly specify a permission.
     */
    public final WorkflowPermission createWorkflowPermission(final int relevantPermission, final boolean isParent, final String metaKey, final String metaValue)
    {
        String relevantPermName = Permissions.getShortName(relevantPermission);
        String prefix = (isParent ? PREFIX_PARENT : PREFIX);
        if (!metaKey.startsWith(prefix)) return null;
        try
        {
            StringTokenizer tok = new StringTokenizer(metaKey.substring(prefix.length()), ".", false);
            String permName = tok.nextToken();
            if (!relevantPermName.equals(permName)) return null;
            int permission = Permissions.getType(permName);
            String grantTypeName = tok.nextToken();
            if ("denied".equals(grantTypeName))
            {
                return new DenyWorkflowPermission(permission);
            }

            Map permTypes = permTypeManager.getTypes();
            SecurityType grantType = (SecurityType) permTypes.get(grantTypeName);
            if (grantType == null) throw new RuntimeException("Unknown type '"+grantTypeName+"' in meta attribute '" + metaKey + "'. Valid permission types are defined in permission-types.xml");

            //JRADEV-20252: when we have username fetched from workflow description we need to swap it with a key
            if(SingleUser.DESC.equals(grantTypeName)){
                ApplicationUser user = userManager.getUserByName(metaValue);
                if(user == null) {
                    return new DenyWorkflowPermission(permission);
                }
                return new DefaultWorkflowPermission(permission, grantType, user.getKey(), isParent);
            } else {
                return new DefaultWorkflowPermission(permission, grantType, metaValue, isParent);
            }



        } catch (NoSuchElementException e)
        {
            String errMsg = "Error parsing meta attribute <meta name='" + metaKey + "'>" + metaValue + "</meta>. Name format is 'jira.permission.[subtasks].<permission>.[group|user|assignee|...]'";
            log.error(errMsg);
            throw new RuntimeException(errMsg);
        }
    }
}
