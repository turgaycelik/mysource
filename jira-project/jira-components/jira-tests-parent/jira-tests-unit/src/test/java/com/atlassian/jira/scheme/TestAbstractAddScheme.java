/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.google.common.collect.ImmutableList;
import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestAbstractAddScheme
{

    @Mock
    SchemeManager arbitrarySchemeManager;

    @AvailableInContainer
    RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    AbstractAddScheme abstractAddScheme;
    boolean wasNameValidated;

    @Before
    public void init()
    {
        wasNameValidated = false;
        abstractAddScheme = new AbstractAddScheme()
        {
            @Override
            public SchemeManager getSchemeManager()
            {
                return arbitrarySchemeManager;
            }

            @Override
            public String getRedirectURL() throws GenericEntityException
            {
                return "DummyRedirectUrl.jspa?id=";
            }

            @Override
            public void doNameValidation(final String name, final String mode)
            {
                wasNameValidated = true;
            }
        };
    }

    @Test
    public void doExecuteShouldCreateNewSchemeBasingOnNameAndDescriptionAndRedirect() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("DummyRedirectUrl.jspa?id=341");

        abstractAddScheme.setName("interestingName");
        abstractAddScheme.setDescription("amazingDescription");

        final Scheme value = new Scheme(341L, null, null, ImmutableList.<SchemeEntity>of());
        when(arbitrarySchemeManager.createSchemeObject("interestingName", "amazingDescription")).thenReturn(value);

        abstractAddScheme.doExecute();

        response.verify();
    }

    @Test
    public void shouldValidateNameWhenOnValidation()
    {
        abstractAddScheme.doValidation();
        assertTrue(wasNameValidated);
    }


}
