package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.config.StatusService;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.SimpleStatusImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class DeleteStatus extends AbstractDeleteConstant
{
    private final StatusService statusService;

    public DeleteStatus(final StatusService statusService)
    {
        this.statusService = statusService;
    }

    protected String getConstantEntityName()
    {
        return "Status";
    }

    protected String getNiceConstantName()
    {
        return "status";
    }

    protected String getIssueConstantField()
    {
        return "status";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getStatus(id);
    }

    protected String getRedirectPage()
    {
        return "ViewStatuses.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getStatuses();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshStatuses();
    }

    protected void doValidation()
    {
        ServiceResult validationResult = statusService.validateRemoveStatus(getLoggedInApplicationUser(), getStatusObject());
        if(!validationResult.isValid()){
            addErrorCollection(validationResult.getErrorCollection());
        }
    }

    private Status getStatusObject() {
        return statusService.getStatusById(getLoggedInApplicationUser(), id);
    }

    @ActionViewData (key = "status")
    public SimpleStatus getStatusFormValues()
    {
        return new SimpleStatusImpl(getStatusObject());
    }

    @ActionViewData
    public Collection<String> getErrorMessages()
    {
        return super.getErrorMessages();
    }

    @ActionViewData
    public String getToken()
    {
        return super.getXsrfToken();
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            statusService.removeStatus(getLoggedInApplicationUser(), getStatusObject());
        }
        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return returnCompleteWithInlineRedirect(getRedirectPage());
        }
    }
}
