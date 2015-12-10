package com.atlassian.jira.web.dispatcher;

import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.ActionViewDataMappings;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;

/**
 */
public class ActionViewDataAnnotationSupportTest
{

    private ActionViewDataAnnotationSupport actionViewDataAnnotationSupport;

    static class Case1
    {

        @ActionViewData (value = "success")
        private String getHiddenGetter()
        {
            return "hiddenGetter";
        }

        @ActionViewData (value = "fail")
        public String getWrongResult()
        {
            return "wrongResult";
        }

        @ActionViewData (value = "success")
        public String notGetter()
        {
            return "notGetterOK";
        }

        public String getNoAnnotation()
        {
            return "notAnnotated";
        }

        public String noAnnotation()
        {
            return "notAnnotatedEither";
        }

        @ActionViewData (value = "success")
        public String getStringKey()
        {
            return "stringValue";
        }

        @ActionViewData ()
        public String forEveryResult()
        {
            return "defaultOf*";
        }

        @ActionViewData ()
        public static String getStatic()
        {
            return "static";
        }

        @ActionViewData ()
        public String setterLike(String s)
        {
            return s;
        }

        @ActionViewDataMappings ({ "success", "fail" })
        public String multiView()
        {
            return "multipleViews";
        }

        @ActionViewData (key = "thisNameWillBeUsed")
        public String thisNameWontBeUsed()
        {
            return "remappedMethodName";
        }

        @ActionViewDataMappings (value = { "success", "fail" }, key = "thisNameWillBeUsedAsWell")
        public String thisAlsoNameWontBeUsed()
        {
            return "remappedMethodName";
        }


        @ActionViewData ()
        public Map<String, Object> mapData()
        {
            return MapBuilder.<String, Object>newBuilder()
                    .add("mapKey1", "mapValue1")
                    .add("mapKey2", "mapValue2")
                    .toMap();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        actionViewDataAnnotationSupport = new ActionViewDataAnnotationSupport();
    }

    @org.junit.Test
    public void testGetData() throws Exception
    {
        Map<String, Object> actual = actionViewDataAnnotationSupport.getData("success", new Case1());

        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("stringKey", "stringValue"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("notGetter", "notGetterOK"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("forEveryResult", "defaultOf*"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("multiView", "multipleViews"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("mapKey1", "mapValue1"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("mapKey2", "mapValue2"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("thisNameWillBeUsed", "remappedMethodName"));
        Assert.assertThat(actual, Matchers.<String, Object>hasEntry("thisNameWillBeUsedAsWell", "remappedMethodName"));
        Assert.assertThat(actual.size(), Matchers.equalTo(8));
    }
}
