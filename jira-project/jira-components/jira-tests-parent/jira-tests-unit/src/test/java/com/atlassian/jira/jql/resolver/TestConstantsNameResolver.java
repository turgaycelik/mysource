package com.atlassian.jira.jql.resolver;

import java.util.Collection;
import java.util.List;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestConstantsNameResolver
{
    @Test
    public void testGetIdsFromNameNoConstant() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantByNameIgnoreCase("TestConstant", "blah");
        mockConstantsManagerControl.setReturnValue(null);
        mockConstantsManagerControl.replay();

        ConstantsNameResolver<Object> constantsNameResolver = new ConstantsNameResolver<Object>(mockConstantsManager, "TestConstant")
        {
            public Collection<Object> getAll()
            {
                return null;
            }
        };

        assertTrue(constantsNameResolver.getIdsFromName("blah").isEmpty());

        mockConstantsManagerControl.verify();
    }

    @Test
    public void testGetIdsFromNameHappyPath() throws Exception
    {
        final MockGenericValue genericValue = new MockGenericValue("TestConst", EasyMap.build("id", "10000"));

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantByNameIgnoreCase("TestConstant", "blah");
        mockConstantsManagerControl.setReturnValue(new StatusImpl(genericValue, null, null, null, null));
        mockConstantsManagerControl.replay();

        ConstantsNameResolver<Object> constantsNameResolver = new ConstantsNameResolver<Object>(mockConstantsManager, "TestConstant")
        {
            public Collection<Object> getAll()
            {
                return null;
            }
        };

        final List<String> ids = constantsNameResolver.getIdsFromName("blah");
        assertEquals(1, ids.size());
        assertEquals("10000", ids.get(0));

        mockConstantsManagerControl.verify();
    }
    
    @Test
    public void testNameExists() throws Exception
    {
        final MockGenericValue genericValue = new MockGenericValue("TestConst", EasyMap.build("id", "10000"));

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantByNameIgnoreCase("TestConstant", "blah");
        mockConstantsManagerControl.setReturnValue(new StatusImpl(genericValue, null, null, null, null));
        mockConstantsManagerControl.replay();

        ConstantsNameResolver<Object> constantsNameResolver = new ConstantsNameResolver<Object>(mockConstantsManager, "TestConstant")
        {
            public Collection<Object> getAll()
            {
                return null;
            }
        };

        assertTrue(constantsNameResolver.nameExists("blah"));

        mockConstantsManagerControl.verify();
    }

    @Test
    public void testNameDoesNotExist() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantByNameIgnoreCase("TestConstant", "blah");
        mockConstantsManagerControl.setReturnValue(null);
        mockConstantsManagerControl.replay();

        ConstantsNameResolver<Object> constantsNameResolver = new ConstantsNameResolver<Object>(mockConstantsManager, "TestConstant")
        {
            public Collection<Object> getAll()
            {
                return null;
            }
        };

        assertFalse(constantsNameResolver.nameExists("blah"));

        mockConstantsManagerControl.verify();
    }

    @Test
    public void testGet() throws Exception
    {
        final MockControl mockIssueConstantControl = MockControl.createStrictControl(IssueConstant.class);
        final IssueConstant mockIssueConstant = (IssueConstant) mockIssueConstantControl.getMock();
        mockIssueConstantControl.replay();

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantObject("TestConstant", "123");
        mockConstantsManagerControl.setReturnValue(mockIssueConstant);
        mockConstantsManagerControl.replay();

        ConstantsNameResolver<Object> constantsNameResolver = new ConstantsNameResolver<Object>(mockConstantsManager, "TestConstant")
        {
            public Collection<Object> getAll()
            {
                return null;
            }
        };

        final Object o = constantsNameResolver.get(123L);
        assertEquals(mockIssueConstant, o);

        mockIssueConstantControl.verify();
        mockConstantsManagerControl.verify();
    }

    @Test
    public void testIdExists() throws Exception
    {
        ConstantsNameResolver<Object> constantsNameResolver = new ConstantsNameResolver<Object>(null, "TestConstant")
        {
            public Collection<Object> getAll()
            {
                return null;
            }

            public Object get(final Long id)
            {
                return new Object();
            }
        };

        assertTrue(constantsNameResolver.idExists(12L));
    }

}
