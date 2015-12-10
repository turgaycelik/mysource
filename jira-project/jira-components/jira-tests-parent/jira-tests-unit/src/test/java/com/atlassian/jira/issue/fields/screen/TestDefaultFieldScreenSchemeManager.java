package com.atlassian.jira.issue.fields.screen;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class TestDefaultFieldScreenSchemeManager
{
    @Mock
    private OfBizDelegator ofBizDelegator;
    @Mock
    private FieldScreenManager fieldScreenManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @org.junit.Test
    public void getFieldScreenSchemeShouldReturnNullWhenScreenSchemeDoesNotExist() throws Exception
    {
        long id = 20L;
        when(ofBizDelegator.findById(FieldScreenSchemeManager.FIELD_SCREEN_SCHEME_ENTITY_NAME, id)).thenReturn(null);

        DefaultFieldScreenSchemeManager manager = new DefaultFieldScreenSchemeManager(ofBizDelegator, fieldScreenManager, new MemoryCacheManager());
        assertNull(manager.getFieldScreenScheme(id));
    }
}
