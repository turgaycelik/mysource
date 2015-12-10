package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class DefaultIssueTypeScreenSchemeStore implements IssueTypeScreenSchemeStore
{
    private final OfBizDelegator ofBizDelegator;
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;

    public DefaultIssueTypeScreenSchemeStore(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public Collection getIssueTypeScreenSchemes()
    {
        List<GenericValue> issueTypeScreenSchemeGVs = ofBizDelegator.findAll(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, EasyList.build("name"));
        return buildIssueTypeScreenSchemes(issueTypeScreenSchemeGVs);
    }

    private Collection buildIssueTypeScreenSchemes(List<GenericValue> issueTypeScreenSchemeGVs)
    {
        List issueTypeScreenSchemes = new LinkedList();
        for (final GenericValue issueTypeScreenSchemeGV : issueTypeScreenSchemeGVs)
        {
            issueTypeScreenSchemes.add(buildIssueTypeScreenScheme(issueTypeScreenSchemeGV));
        }

        return issueTypeScreenSchemes;
    }

    private IssueTypeScreenScheme buildIssueTypeScreenScheme(GenericValue genericValue)
    {
        return new IssueTypeScreenSchemeImpl(getIssueTypeScreenSchemeManager(), genericValue);
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme(Long id)
    {
        GenericValue issueTypeScreenSchemeGV = ofBizDelegator.findById(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, id);
        return buildIssueTypeScreenScheme(issueTypeScreenSchemeGV);
    }

    public IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
    {
        return issueTypeScreenSchemeManager;
    }

    public void setIssueTypeScreenSchemeManager(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }

}
