package com.atlassian.jira.webtests;

import com.meterware.httpunit.WebTable;

import java.net.URL;
import java.util.List;

public abstract class AbstractTestIssueNavigatorColumnsView extends AbstractTestIssueNavigatorView
{

    public AbstractTestIssueNavigatorColumnsView(String name)
    {
        super(name);
    }

    protected class ItemVerifier
    {

        protected final AbstractTestIssueNavigatorColumnsView test;
        protected final Item item;
        protected final WebTable table;
        protected final int row;
        protected final URL baseUrl;

        public ItemVerifier(AbstractTestIssueNavigatorColumnsView test, Item item, WebTable table, URL baseUrl)
        {
            this.test = test;
            this.item = item;
            this.table = table;
            this.row = Integer.parseInt(item.getAttribute("rowId"));
            this.baseUrl = baseUrl;
        }

        public void verify()
        {
            String key = item.getAttribute(ATT_KEY);
            test.log("Checking item [" + key + "] on row [" + row + "]");

            //this should have the same link as the key column.
            final String issueLink = baseUrl + "/browse/" + key;

            verifyLinkExists(ISSUE_TYPE, issueLink);

            verifyLinkExists(ISSUE_KEY, issueLink);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_SUMMARY, ATT_SUMMARY);
            verifyLinkExists(ISSUE_SUMMARY, issueLink);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_ASSIGNEE, ATT_ASSIGNEE);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_REPORTER, ATT_REPORTER);

            verifyImageExists(ISSUE_PRIORITY, baseUrl + "/images/icons/" +
                    ISSUE_PRIORITY_IMAGE_MAP.get(item.getAttribute(ATT_PRIORITY)));

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_STATUS, ATT_STATUS);

            final String resolution = item.getAttribute(ATT_RESOLUTION);
            verifyCellValueEmptyOrEquals(ISSUE_RESOLUTION, resolution.equals("Unresolved") ? "Unresolved" : resolution);

            // For the following three dates, we are only testing that the cells are not empty.
            // Should perhaps add more detail to the test Items in the future to contain the date details
            verifyCellIssueAttributeEmptyOrEquals(ISSUE_CREATED, ATT_DATE_CREATED);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_UPDATED, ATT_DATE_UPDATED);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_DUE, ATT_DATE_DUE);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_AFFECTS_VERSIONS, ATT_VERSION);

            verifyCustomFieldDisplayValues(ISSUE_CASCADING_SELECT_FIELD, CF_CASCADING_SELECT_FIELD);

            for (String component : item.getComponents())
            {
                verifyCellValueEmptyOrEquals(ISSUE_COMPONENTS, component);
            }

            verifyCustomFieldDisplayValues(ISSUE_DATE_PICKER_FIELD, CF_DATE_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_DATE_TIME_FIELD, CF_DATE_TIME_FIELD);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_DESCRIPTION, ATT_DESCRIPTION);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_ENVIRONMENT, ATT_ENVIRONMENT);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_FIX_VERSIONS, ATT_FIX_VERSION);

            verifyCustomFieldDisplayValues(ISSUE_FREE_TEXT_FIELD, CF_FREE_TEXT_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_GROUP_PICKER_FIELD, CF_GROUP_PICKER_FIELD);            

            verifyCustomFieldDisplayValues(ISSUE_IMPORT_ID_FIELD, CF_IMPORT_ID_FIELD);

            //Links
            final IssueLinks links = item.getLinks();
            for (final Object o1 : links.getInLinks())
            {
                IssueLink link = (IssueLink) o1;
                verifyCellValueEmptyOrEquals(ISSUE_LINKS, link.getLink());
                verifyLinkExists(ISSUE_LINKS, link.getUrl());
            }
            for (final Object o : links.getOutLinks())
            {
                IssueLink link = (IssueLink) o;
                verifyCellValueEmptyOrEquals(ISSUE_LINKS, link.getLink());
                verifyLinkExists(ISSUE_LINKS, link.getUrl());
            }

            verifyCustomFieldDisplayValues(ISSUE_MULTI_CHECKBOXES_FIELD, CF_MULTI_CHECKBOXES_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_MULTI_GROUP_PICKER_FIELD, CF_MULTI_GROUP_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_MULTI_SELECT_FIELD, CF_MULTI_SELECT_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_MULTI_USER_PICKER_FIELD, CF_MULTI_USER_PICKER_FIELD);
            verifyCustomFieldLinks(ISSUE_MULTI_USER_PICKER_FIELD, CF_MULTI_USER_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_NUMBER_FIELD, CF_NUMBER_FIELD);

            if (FORMAT_DAYS.equals(timeFormat))
            {
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_ORIGINAL_ESTIMATE, ATT_TIMEORIGINALESTIMATE_DAYS);
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_REMAINING_ESTIMATE, ATT_REMAINING_ESTIMATE_DAYS);
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_TIME_SPENT, ATT_TIMESPENT_DAYS);
            }
            else if (FORMAT_HOURS.equals(timeFormat))
            {
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_ORIGINAL_ESTIMATE, ATT_TIMEORIGINALESTIMATE_HOURS);
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_REMAINING_ESTIMATE, ATT_REMAINING_ESTIMATE_HOURS);
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_TIME_SPENT, ATT_TIMESPENT_HOURS);
            }
            else
            {
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_ORIGINAL_ESTIMATE, ATT_TIMEORIGINALESTIMATE);
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_REMAINING_ESTIMATE, ATT_REMAINING_ESTIMATE);
                verifyCellIssueAttributeEmptyOrEquals(ISSUE_TIME_SPENT, ATT_TIMESPENT);
            }

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_PROJECT, ATT_PROJECT);

            verifyCustomFieldDisplayValues(ISSUE_PROJECT_PICKER_FIELD, CF_PROJECT_PICKER_FIELD);
            verifyCustomFieldLinks(ISSUE_PROJECT_PICKER_FIELD, CF_PROJECT_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_ROTEXT_FIELD, CF_RO_TEXT_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_RADIO_BUTTONS_FIELD, CF_RADIO_BUTTONS_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_SELECT_LIST, CF_SELECT_LIST);

            verifyCustomFieldDisplayValues(ISSUE_SINGLE_VERSION_PICKER_FIELD, CF_SINGLE_VERSION_PICKER_FIELD);
            verifyCustomFieldLinks(ISSUE_SINGLE_VERSION_PICKER_FIELD, CF_SINGLE_VERSION_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_TEXT_FIELD255, CF_TEXT_FIELD255);

            verifyCustomFieldDisplayValues(ISSUE_URL_FIELD, CF_URLFIELD);
            verifyCustomFieldLinks(ISSUE_URL_FIELD, CF_URLFIELD);

            verifyCustomFieldDisplayValues(ISSUE_USER_PICKER_FIELD, CF_USER_PICKER_FIELD);
            verifyCustomFieldLinks(ISSUE_USER_PICKER_FIELD, CF_USER_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_VERSION_PICKER_FIELD, CF_VERSION_PICKER_FIELD);
            verifyCustomFieldLinks(ISSUE_VERSION_PICKER_FIELD, CF_VERSION_PICKER_FIELD);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_VOTES, ATT_VOTES);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_WORK_RATIO, ATT_WORK_RATIO);

        }

        protected void verifyImageExists(String fieldName, String imageUrl)
        {
            assertTableCellHasImage(table, row, getColumn(fieldName), imageUrl);
        }

        protected void verifyLinkExists(String fieldName, String link)
        {
            assertTrue(test.tableCellHasLinkThatContains(table, row, getColumn(fieldName), link));
        }

        protected int getColumn(String key)
        {
            int i = issueFieldColumnMap.indexOf(key);
            if (i < 0)
            {
                test.log("No column found for: " + key);
            }
            return i;
        }

        protected void verifyCellIssueAttributeEmptyOrEquals(String fieldName, String itemFieldName)
        {
            String fieldValue = item.getAttribute(itemFieldName);
            verifyCellValueEmptyOrEquals(fieldName, fieldValue);
        }

        protected void verifyCellValueEmptyOrEquals(String fieldName, String fieldExpectedValue)
        {
            if (fieldExpectedValue != null)
            {
                if (NOT_TESTED.equals(fieldExpectedValue))
                {
                    String cellAsText = table.getCellAsText(row, getColumn(fieldName));
                    assertNotNull(cellAsText);
                    assertTrue(cellAsText.trim().length() > 0);
                }
                else
                {
                    assertTrue(test.tableCellHasText(table, row, getColumn(fieldName), fieldExpectedValue));
                }
            }
            else
            {
                String cellAsText = table.getCellAsText(row, getColumn(fieldName));
                // null is ok; if not null, must be white spaces
                if (cellAsText != null)
                {
                    assertEquals("", cellAsText.trim());
                }
            }
        }

        protected void verifyCustomFieldDisplayValues(String columnKey, String customFieldName)
        {
            CustomField customField = item.getCustomFieldByName(customFieldName);
            if (customField != null)
            {
                List<CustomField.Value> values = customField.getValues();
                for (final CustomField.Value value : values)
                {
                    final int column = getColumn(columnKey);
                    assertTrue(String.format("Table %s[row=%d, col=%d] does not contain text '%s'", table.getID(), row, column, value.getDisplayValue()), test.tableCellHasText(table, row, column, value.getDisplayValue()));
                }
            }
            else
            {
                verifyCellValueEmptyOrEquals(columnKey, null);
            }
        }

        protected void verifyCustomFieldLinks(String columnKey, String customFieldName)
        {
            CustomField customField = item.getCustomFieldByName(customFieldName);
            if (customField != null)
            {
                List<CustomField.Value> values = customField.getValues();
                for (final CustomField.Value value : values)
                {
                    final String link = value.getLink();
                    if (link != null)
                    {
                        final int column = getColumn(columnKey);
                        assertTrue(test.tableCellHasLinkThatContains(table, row, column, link));
                    }
                    else
                    {
                        fail("link not defined");
                    }
                }
            }
        }
    }

    protected Item createItem1()
    {
        final Item item = super.createItem1();
        item.setAttribute("rowId", "1");
        item.setAttribute(ATT_PROJECT, "homosapien");
        return item;
    }

    protected Item createItem2()
    {
        final Item item = super.createItem2();
        item.setAttribute("rowId", "2");
        item.setAttribute(ATT_PROJECT, "homosapien");
        return item;
    }

    protected Item createItem3()
    {
        final Item item = super.createItem3();
        item.setAttribute("rowId", "3");
        item.setAttribute(ATT_PROJECT, "homosapien");
        item.setAttribute(ATT_WORK_RATIO, "0%");
        return item;
    }
}
