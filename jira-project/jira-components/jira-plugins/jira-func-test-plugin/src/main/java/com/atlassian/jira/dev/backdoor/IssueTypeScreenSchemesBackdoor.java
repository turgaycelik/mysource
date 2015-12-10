package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Use this backdoor to manipulate Issue Type Screen Schemes as part of setup for tests.
 *
 * This class should only be called by the
 * {@link com.atlassian.jira.functest.framework.backdoor.IssueTypeScreenSchemesControl}.
 *
 * @since v5.0
 */
@Path ("issueTypeScreenSchemes")
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueTypeScreenSchemesBackdoor
{
    private final IssueTypeScreenSchemeManager schemeManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final ConstantsManager constantsManager;

    public IssueTypeScreenSchemesBackdoor(IssueTypeScreenSchemeManager schemeManager, FieldScreenSchemeManager fieldScreenSchemeManager,
            ConstantsManager constantsManager)
    {
        this.schemeManager = schemeManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.constantsManager = constantsManager;
    }

    @GET
    @AnonymousAllowed
    @Path("create")
    public Response create(@QueryParam ("name") String name, @QueryParam ("description") String description, @QueryParam ("fieldScreenSchemeId") Long fieldScreenSchemeId)
    {
        IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(schemeManager, null);
        issueTypeScreenScheme.setName(name);
        issueTypeScreenScheme.setDescription(description);
        issueTypeScreenScheme.store();
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(schemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
        issueTypeScreenSchemeEntity.setIssueTypeId(null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(fieldScreenSchemeId));
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

        return Response.ok(issueTypeScreenScheme.getId()).build();
    }

    @GET
    @AnonymousAllowed
    @Path("remove")
    public Response remove(@QueryParam ("id") long id)
    {
        schemeManager.getIssueTypeScreenScheme(id).remove();

        return Response.ok(null).build();
    }
}
