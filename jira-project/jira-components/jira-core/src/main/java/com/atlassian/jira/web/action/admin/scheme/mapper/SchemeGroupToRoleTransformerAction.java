package com.atlassian.jira.web.action.admin.scheme.mapper;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.scheme.mapper.SchemeGroupsToRoleTransformerService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.mapper.GroupToRoleMapping;
import com.atlassian.jira.scheme.mapper.SchemeTransformResults;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This is portion of the GroupToRoleMapping tool that actually transforms the schemes. All the input has been
 * collected once this action is invoked.
 */
@WebSudoRequired
public class SchemeGroupToRoleTransformerAction extends AbstractGroupToRoleAction
{
    public static final String UNMAPPED_PROJECT_ROLE_VALUE = "-1";

    private SchemeGroupsToRoleTransformerService schemeGroupsToRolesTransformerService;
    private List groupToRoleMappings;

    private Predicate mapsGlobalUsePermission = new Predicate()
    {
        public boolean evaluate(Object object)
        {
            GroupToRoleMapping mapping = (GroupToRoleMapping) object;
            return schemeGroupsToRolesTransformerService.isGroupGrantedGlobalUsePermission(mapping.getGroupName());
        }
    };

    private Predicate doesNotMapGlobalUsePermission = PredicateUtils.notPredicate(mapsGlobalUsePermission);

    public SchemeGroupToRoleTransformerAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, SchemeGroupsToRoleTransformerService schemeGroupsToRolesTransformer, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
        this.schemeGroupsToRolesTransformerService = schemeGroupsToRolesTransformer;
    }

    public String doDefault() throws Exception
    {
        Set roleMappings = (getGroupToRoleMappings() == null ? Collections.EMPTY_SET : new HashSet(getGroupToRoleMappings()));
        SchemeTransformResults schemeTransformResults = schemeGroupsToRolesTransformerService.doTransform(getLoggedInUser(), getSchemeObjs(), roleMappings, this);
        // Store the results of the transform so that if they want to commit the operation we have the goods
        ActionContext.getSession().put(TRANSFORM_RESULTS_KEY, schemeTransformResults);

        return INPUT;
    }

    protected void doValidation()
    {
        if (!isHasSelectedSchemeIds())
        {
            addErrorMessage(getText("admin.scheme.group.role.preview.no.schemes.selected"));
        }
        else if (getGroupToRoleMappings().isEmpty())
        {
            addErrorMessage(getText("admin.scheme.group.role.preview.no.groups.selected"));
        }
    }

    protected String doExecute() throws Exception
    {
        SchemeTransformResults schemeTransformResults = getSchemeTransformResults();

        // This will unpack all the users from the old groups to the project roles that they have been mapped to. It
        // will also save the newly transformed schemes and rename the original schemes to a 'Backup of ...'
        schemeGroupsToRolesTransformerService.persistTransformationResults(getLoggedInUser(), schemeTransformResults, this);

        return forceRedirect("SchemeGroupToRoleResult!default.jspa");
    }

    public List getGroupToRoleMappings()
    {
        if (groupToRoleMappings == null)
        {
            groupToRoleMappings = (List) ActionContext.getSession().get(AbstractGroupToRoleAction.GROUP_TO_ROLE_MAP_SESSION_KEY);
        }

        return groupToRoleMappings;
    }

    public boolean isAnyGroupGrantedGlobalUsePermission()
    {
        Collection groupNames = CollectionUtils.transformedCollection(new ArrayList(), GroupToRoleMapping.MAPPING_TO_GROUPNAME_TRANSFORMER);
        groupNames.addAll(getGroupToRoleMappings());
        return schemeGroupsToRolesTransformerService.isAnyGroupGrantedGlobalUsePermission(groupNames);
    }

    public boolean isGroupGrantedGlobalUsePermission(String groupName)
    {
        return schemeGroupsToRolesTransformerService.isGroupGrantedGlobalUsePermission(groupName);
    }

    public Iterator getMappingsWithoutGlobalUsePermission()
    {
        return IteratorUtils.filteredIterator(getGroupToRoleMappings().iterator(), doesNotMapGlobalUsePermission);
    }

    public Iterator getMappingsWithGlobalUsePermission()
    {
        return IteratorUtils.filteredIterator(getGroupToRoleMappings().iterator(), mapsGlobalUsePermission);
    }

    public String getFullNameForUser(String username)
    {
        User user = UserUtils.getUser(username);
        if (user != null)
        {
            return user.getDisplayName();
        }
        return username;
    }
}
