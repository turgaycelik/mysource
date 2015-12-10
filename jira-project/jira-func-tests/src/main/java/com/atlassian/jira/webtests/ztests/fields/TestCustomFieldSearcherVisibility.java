package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestCustomFieldSearcherVisibility extends FuncTestCase
{
    public void testJRA_16356()
    {
        administration.restoreData("TestCustomFieldSearcherVisibility.xml");

        // the fields are start off visible so assert that
        issueTableAssertions.assertSearchersPresent("DatePickerCF", "TextCF");

        // now hide the fields are assert that they are gone
        hideCustomFields();
        issueTableAssertions.assertSearchersNotPresent("DatePickerCF", "TextCF");
    }

    private void hideCustomFields()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("DatePickerCF");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("TextCF");
    }
}
