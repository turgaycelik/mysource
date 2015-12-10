package com.atlassian.jira.scheme;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.JiraAuthenticationContext;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class TestAbstractSchemeAwareAction
{
    @Mock
    SchemeManager arbitrarySchemeManager;


    @Mock
    @AvailableInContainer
    JiraAuthenticationContext jiraAuthenticationContext;

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    AbstractSchemeAwareAction abstractSchemeAwareAction;

    @Before
    public void init(){
        abstractSchemeAwareAction = new AbstractSchemeAwareAction()
        {

            @Override
            public SchemeManager getSchemeManager()
            {
                return arbitrarySchemeManager;
            }

            @Override
            public String getRedirectURL() throws GenericEntityException
            {
                return "DummyUrl";
            }
        };

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());
    }

    @Test
    public void shouldGetAndCacheSchemeFromManagerWhenGetSchemeWasCalled() throws Exception{
        final Scheme scheme = new Scheme(341L, null, null, ImmutableList.<SchemeEntity>of());
        final GenericValue gv = new MockGenericValue("AbstractScheme");

        abstractSchemeAwareAction.setSchemeId(783L);
        when(arbitrarySchemeManager.getSchemeObject(783L)).thenReturn(scheme);
        when(arbitrarySchemeManager.getScheme(783L)).thenReturn(gv);

        assertSame(scheme, abstractSchemeAwareAction.getSchemeObject());
        assertSame(gv, abstractSchemeAwareAction.getScheme());

        verify(arbitrarySchemeManager).getSchemeObject(783L);
        verify(arbitrarySchemeManager).getScheme(783L);

        abstractSchemeAwareAction.getSchemeObject();
        abstractSchemeAwareAction.getScheme();

        //values are cached, there should be no more interactions
        verifyNoMoreInteractions(arbitrarySchemeManager);
    }

    @Test
    public void shouldCheckNameDuplicationOnValidation(){

        abstractSchemeAwareAction.setSchemeId(12L);
        final Scheme alreadyExistingScheme = new Scheme(341L, null, null, ImmutableList.<SchemeEntity>of());
        when(arbitrarySchemeManager.getSchemeObject("reallyStrangeName")).thenReturn(alreadyExistingScheme);
        abstractSchemeAwareAction.doNameValidation("  reallyStrangeName\t", null);

        assertEquals("admin.errors.a.scheme.with.this.name.exists", abstractSchemeAwareAction.getErrors().get("name"));

    }


}
