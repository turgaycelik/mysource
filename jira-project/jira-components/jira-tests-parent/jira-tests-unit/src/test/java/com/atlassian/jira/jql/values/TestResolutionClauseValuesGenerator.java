package com.atlassian.jira.jql.values;

import java.util.List;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.resolution.MockResolution;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.values.ResolutionClauseValuesGenerator.quoteName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestResolutionClauseValuesGenerator extends MockControllerTestCase
{
    
    private ConstantsManager constantsManager;
    private ResolutionClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        this.constantsManager = mockController.getMock(ConstantsManager.class);
        this.valuesGenerator = new ResolutionClauseValuesGenerator(constantsManager);
    }

    @Test
    public void testGetAllConstants() throws Exception
    {
        final MockResolution type1 = new MockResolution("1", "Aa it");
        final MockResolution type2 = new MockResolution("2", "A it");
        final MockResolution type3 = new MockResolution("3", "B it");
        final MockResolution type4 = new MockResolution("4", "C it");

        constantsManager.getResolutionObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        valuesGenerator.getAllConstants();
        mockController.verify();
    }

    @Test
    public void testGetAllConstantNames() throws Exception
    {
        final MockResolution type1 = new MockResolution("1", "Aa it");
        final MockResolution type2 = new MockResolution("2", "A it");
        final MockResolution type3 = new MockResolution("3", "B it");
        final MockResolution type4 = new MockResolution("4", "C it");

        final List<Resolution> resolutions = CollectionBuilder.<Resolution>newBuilder(type1, type2, type3, type4).asList();
        EasyMock.expect(constantsManager.getResolutionObjects())
                .andReturn(resolutions);

        replay();

        final List<String> result = valuesGenerator.getAllConstantNames();
        assertEquals(5, result.size());
        assertTrue(result.contains("Aa it"));
        assertTrue(result.contains("A it"));
        assertTrue(result.contains("B it"));
        assertTrue(result.contains("C it"));
        assertTrue(result.contains("Unresolved"));
    }

    @Test
    public void testQuoteName() throws Exception
    {
        replay();

        assertEquals("\"unresolved\"", quoteName("unresolved"));
        assertEquals("\"'unresolved\"", quoteName("'unresolved"));
        assertEquals("\"\"unresolved\"", quoteName("\"unresolved"));
        assertEquals("\"unresolved'\"", quoteName("unresolved'"));
        assertEquals("\"unresolved\"\"", quoteName("unresolved\""));
        assertEquals("\"'unresolved'\"", quoteName("'unresolved'"));
        assertEquals("\"\"unresolved\"\"", quoteName("\"unresolved\""));
    }
}
