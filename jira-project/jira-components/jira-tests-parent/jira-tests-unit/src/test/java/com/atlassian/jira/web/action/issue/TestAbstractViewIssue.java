/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestAbstractViewIssue
{
    private AbstractViewIssue abstractViewIssue;

    @Mock
    @AvailableInContainer
    private AttachmentManager attachmentManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @AvailableInContainer(instantiateMe = true)
    private MockI18nHelper i18nHelper;

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Mock
    private MutableIssue issue;

    @Before
    public void setUp(){
        abstractViewIssue = new AbstractViewIssue(null){
            @Nonnull
            @Override
            public MutableIssue getIssueObject() throws IssueNotFoundException, IssuePermissionException
            {
                return issue;
            }
        };

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        i18nHelper.setLocale(Locale.getDefault());
    }

    @Test
    public void getAttachmentsShouldCacheItsValue() throws Exception{


        List<Attachment> attachments = ImmutableList.of(mock(Attachment.class));

        when(attachmentManager.getAttachments(same(issue), any(Comparator.class))).thenReturn(attachments);
        assertSame(attachments, abstractViewIssue.getAttachments());
        verify(attachmentManager).getAttachments(same(issue), any(Comparator.class));

        assertSame(attachments, abstractViewIssue.getAttachments());
        verifyNoMoreInteractions(attachmentManager);
    }



}
