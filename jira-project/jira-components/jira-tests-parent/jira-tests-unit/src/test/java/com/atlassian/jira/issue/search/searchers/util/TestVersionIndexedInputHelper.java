package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.operand.SingleValueOperand;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestVersionIndexedInputHelper extends MockControllerTestCase
{
    private NameResolver<Version> versionResolver;
    private VersionIndexedInputHelper helper;

    @Before
    public void setUp() throws Exception
    {
        versionResolver = getMock(NameResolver.class);
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsntNumber() throws Exception
    {
        helper = instantiate(VersionIndexedInputHelper.class);
        
        assertEquals(new SingleValueOperand("test"), helper.createSingleValueOperandFromId("test"));
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsNotAVersionNumber() throws Exception
    {
        expect(versionResolver.get(123l))
                .andReturn(null);

        helper = instantiate(VersionIndexedInputHelper.class);

        assertEquals(new SingleValueOperand(123l), helper.createSingleValueOperandFromId("123"));
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsAVersion() throws Exception
    {
        expect(versionResolver.get(123l))
                .andReturn(new MockVersion(123l,"Version 1"));

        helper = instantiate(VersionIndexedInputHelper.class);

        assertEquals(new SingleValueOperand("Version 1"), helper.createSingleValueOperandFromId("123"));
    }

}
