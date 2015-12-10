package com.atlassian.jira.event.issue;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestIssueEventParamsTransformerImpl
{
    @Mock
    private ApplicationProperties applicationProperties;

    private IssueEventParamsTransformer transformer;

    @Before
    public void setUp()
    {
        transformer = new IssueEventParamsTransformerImpl(applicationProperties);
    }

    @Test
    public void transformParamsAddsJiraBaseUrl()
    {
        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("base-url");

        Map<String, Object> transformedParameters = transformer.transformParams(new HashMap<String, Object>());

        assertThat((String) transformedParameters.get(IssueEvent.BASE_URL_PARAM_NAME), is("base-url"));
    }

    @Test
    public void transformParamsCopiesOriginalParameters()
    {
        Map<String, Object> originalParams = new HashMap<String, Object>();
        originalParams.put("key1", "value1");
        originalParams.put("key2", "value2");

        Map<String, Object> transformedParameters = transformer.transformParams(originalParams);

        assertTrue(originalParams != transformedParameters);
        assertThat((String) transformedParameters.get("key1"), is("value1"));
        assertThat((String) transformedParameters.get("key2"), is("value2"));
    }
}
