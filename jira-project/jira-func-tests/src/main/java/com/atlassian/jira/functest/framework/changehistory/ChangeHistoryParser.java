package com.atlassian.jira.functest.framework.changehistory;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

import java.util.StringTokenizer;

/**
 * A ChangeHistoryParser can take a web page and parse out the {@link com.atlassian.jira.util.changehistory.ChangeHistoryList}
 * from the page.
 *
 * @since v3.13
 */
public class ChangeHistoryParser
{
    private ChangeHistoryParser()
    {
    }

    public static ChangeHistoryList getChangeHistory(WebTester tester) throws SAXException
    {
        ChangeHistoryList changeHistoryList = new ChangeHistoryList();
        String htmltext = tester.getDialog().getResponseText();
        String CHANGE_HISTORY_ID = "id=\"changehistory_";
        int CHANGE_HISTORY_ID_LEN = CHANGE_HISTORY_ID.length();

        int startIndex = htmltext.indexOf(CHANGE_HISTORY_ID);
        while (startIndex != -1)
        {
            int endIndex = htmltext.indexOf("\"", startIndex + CHANGE_HISTORY_ID_LEN);
            String id = htmltext.substring(startIndex + CHANGE_HISTORY_ID_LEN, endIndex);

            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='changehistorydetails_" + id + "']");
            String changeHistoryDetails = xPathLocator.getText();

            WebTable changeHistoryTable = tester.getDialog().getResponse().getTableWithID("changehistory_" + id);
            String changedByTD = changeHistoryDetails.substring(0, changeHistoryDetails.indexOf("made changes"));
            changedByTD = smooshText(changedByTD);

            ChangeHistorySet set = changeHistoryList.addChangeSet(changedByTD);
            for (int row = 0; row < changeHistoryTable.getRowCount(); row++)
            {
                // get the
                String fieldNameTD = smooshText(changeHistoryTable.getCellAsText(row, 0));
                String oldValueTD = smooshText(changeHistoryTable.getCellAsText(row, 1));
                String newValueTD = smooshText(changeHistoryTable.getCellAsText(row, 2));

                set.add(fieldNameTD, oldValueTD, newValueTD);
            }
            startIndex = htmltext.indexOf(CHANGE_HISTORY_ID, endIndex);
        }
        return changeHistoryList;
    }


    static String smooshText(String s)
    {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens())
        {
            sb.append(st.nextToken().trim());
            if (st.hasMoreTokens())
            {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

}
