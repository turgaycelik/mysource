package com.atlassian.jira.upgrade.util;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

/**
 * @since v6.2.3
 */
public class TestUpgradeEntityUtil
{
    @Rule
    public TestRule initMock = new InitMockitoMocks(this);

    private MockOfBizDelegator ofBizDelegator;
    private UpgradeEntityUtil entityRemover;
    private Multimap<Level, String> loggedMessages;
    public static final String ENTITY_NAME = "someEntity";
    public static final String TASK_NAME = "testTask";

    @Before
    public void setUp() throws Exception
    {
        ofBizDelegator = new MockOfBizDelegator();
        final EntityEngine entityEngine = new EntityEngineImpl(ofBizDelegator);
        loggedMessages = HashMultimap.create();
        entityRemover = new UpgradeEntityUtil(TASK_NAME, entityEngine)
        {
            @Override
            void doLogInt(final Level level, final String message)
            {
                loggedMessages.put(level, message);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldLogAllDeletedValues()
    {
        //having
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of("id", 1L, "value", "toDelVal1"));
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of("id", 2L, "value", "toDelVal1"));
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of("id", 3L, "value", "stayVal2"));
        //when
        entityRemover.deleteEntityByCondition(ENTITY_NAME, new EntityExpr("value", EntityOperator.EQUALS, "toDelVal1"));
        //then
        assertThat(loggedMessages.keys().elementSet(), Matchers.containsInAnyOrder(Level.WARN));
        final Collection<String> warnMessages = loggedMessages.get(Level.WARN);
        assertThat((Iterable)warnMessages, Matchers.containsInAnyOrder((Collection)Arrays.asList(
                Matchers.allOf(Matchers.containsString("\"id\":\"1\""),
                        Matchers.containsString("\"value\":\"toDelVal1")),
                Matchers.allOf(Matchers.containsString("\"id\":\"2\""),
                        Matchers.containsString("\"value\":\"toDelVal1")))));
        assertThat(ofBizDelegator.getCount(ENTITY_NAME), Matchers.equalTo(1l));
    }

    @Test
    public void shouldNotExecuteDeleteAndLogValuesWhenNothingMatches()
    {
        //having
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of("id", 1L, "value", "stayVal1"));
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of("id", 2L, "value", "stayVal1"));
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of("id", 3L, "value", "stayVal2"));
        //when
        entityRemover.deleteEntityByCondition(ENTITY_NAME, new EntityExpr("value", EntityOperator.EQUALS, "toDelVal1"));
        //then
        assertThat(loggedMessages.keys().elementSet(), Matchers.<Level>empty());
        assertThat(ofBizDelegator.getCount(ENTITY_NAME), Matchers.equalTo(3l));
    }

}
