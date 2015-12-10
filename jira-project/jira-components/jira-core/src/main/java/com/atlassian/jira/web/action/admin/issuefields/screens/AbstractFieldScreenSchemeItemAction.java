package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AbstractFieldScreenSchemeItemAction extends AbstractFieldScreenSchemeAction
{
    private final FieldScreenManager fieldScreenManager;
    private Long issueOperationId;
    private Long fieldScreenId;
    private Collection<FieldScreen> fieldScreens;
    private List<ScreenableIssueOperation> addableIssueOperations;

    public AbstractFieldScreenSchemeItemAction(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenSchemeManager);
        this.fieldScreenManager = fieldScreenManager;
    }

    protected String redirectToView()
    {
        return returnComplete("ConfigureFieldScreenScheme.jspa?id=" + getId());
    }

    protected void validateIssueOperationId()
    {
        // Null operation id represents the default entry
        if (issueOperationId != null && IssueOperations.getIssueOperation(issueOperationId) == null)
        {
            addError("issueOperationId", getText("admin.errors.screens.invalid.issue.operation.id"));
        }
    }

    protected void validateFieldScreenId()
    {
        if (getFieldScreenId() == null)
        {
            addError("fieldScreenId", getText("admin.errors.screens.please.select.screen"));
        }
        else if (getFieldScreenManager().getFieldScreen(getFieldScreenId()) == null)
        {
            addError("fieldScreenId", getText("admin.errors.screens.invalid.id"));
        }
    }

    public Long getIssueOperationId()
    {
        return issueOperationId;
    }

    public void setIssueOperationId(Long issueOperationId)
    {
        this.issueOperationId = issueOperationId;
    }

    public Long getFieldScreenId()
    {
        return fieldScreenId;
    }

    public void setFieldScreenId(Long fieldScreenId)
    {
        this.fieldScreenId = fieldScreenId;
    }

    public Collection<FieldScreen> getFieldScreens()
    {
        if (fieldScreens == null)
        {
            fieldScreens = fieldScreenManager.getFieldScreens();
        }

        return fieldScreens;
    }

    protected FieldScreenManager getFieldScreenManager()
    {
        return fieldScreenManager;
    }

    public IssueOperation getIssueOperation()
    {
        if (getIssueOperationId() != null)
            return IssueOperations.getIssueOperation(getIssueOperationId());
        else
            return null;
    }

    public Collection<ScreenableIssueOperation> getAddableIssueOperations()
    {
        if (addableIssueOperations == null)
        {
            final DefaultIssueOperation defaultIssueOperation = new DefaultIssueOperation();

            addableIssueOperations = Lists.newLinkedList();
            FieldScreenScheme fieldScreenScheme = getFieldScreenScheme();
            if (fieldScreenScheme != null)
            {
                addableIssueOperations.add(defaultIssueOperation);
                addableIssueOperations.addAll(IssueOperations.getIssueOperations());

                for (FieldScreenSchemeItem fieldScreenSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems())
                {
                    ScreenableIssueOperation issueOperation = fieldScreenSchemeItem.getIssueOperation();
                    if (issueOperation != null)
                    {
                        addableIssueOperations.remove(issueOperation);
                    }
                    else
                    {
                        addableIssueOperations.remove(defaultIssueOperation);
                    }
                }
            }
        }

        return addableIssueOperations;
    }

    private static class DefaultIssueOperation implements ScreenableIssueOperation
    {
        private String nameKey = "admin.common.words.default";
        private String description = "";

        public Long getId()
        {
            return null;
        }

        public String getNameKey()
        {
            return nameKey;
        }

        public String getDescriptionKey()
        {
            return description;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof IssueOperation)) return false;

            final ScreenableIssueOperation issueOperation = (ScreenableIssueOperation) o;

            if (description != null ? !description.equals(issueOperation.getDescriptionKey()) : issueOperation.getDescriptionKey() != null) return false;
            if (issueOperation.getId() != null) return false;
            if (nameKey != null ? !nameKey.equals(issueOperation.getNameKey()) : issueOperation.getNameKey() != null) return false;

            return true;
        }

        public int hashCode()
        {
            int result = 0;
            result = 29 * result + (nameKey != null ? nameKey.hashCode() : 0);
            result = 29 * result + (description != null ? description.hashCode() : 0);
            return result;
        }
    }
}
