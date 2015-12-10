package com.atlassian.jira.webtests;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.functest.framework.util.dom.SneakyDomExtractor;
import com.atlassian.jira.functest.framework.util.text.TextKit;
import com.meterware.httpunit.Button;
import com.meterware.httpunit.HTMLElementPredicate;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.util.ExceptionUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

/** This super class provides extensions to JWebUnit useful in acceptance tests. */
public class AbstractAtlassianWebTestCase extends WebTestCaseWrapper
{
    /**
     * A predicate for matching the class of an HTML table. Actual class name is passed into the matchesCriteria method
     * by users of this interface. This is used to select nodes from the stupid httpunit api.
     */
    static final HTMLElementPredicate TABLE_CLASS_IS = new HTMLElementPredicate()
    {
        public boolean matchesCriteria(Object htmlElement, Object criteria)
        {
            return ((String) criteria).equalsIgnoreCase(((WebTable) htmlElement).getClassName());
        }
    };
    //------------------------------------------------------------------------------------------------------------ ctors

    AbstractAtlassianWebTestCase(String name)
    {
        super(name);
    }

    //--------------------------------------------------------------------------------------------------- helper methods

    /**
     * Click only a single button with the specific value.
     * If more than one button found with that value it will barf.
     *
     * @param value the value attribute of the of the button element to click.
     */
    protected void clickButtonWithValue(String value)
    {
        assertButtonPresentWithValue(value);
        Button buttonToClick = null;
        Button[] buttons = getDialog().getForm().getButtons();
        for (Button button : buttons)
        {
            if (value.equals(button.getValue()))
            {
                buttonToClick = button;
                break; // we've already tested for multiple buttons in assert above
            }
        }

        try
        {
            // Stops IDEA's NPE complier warning, although assertAnyButtonPresentWithValue() would already cover this.
            assertNotNull(buttonToClick);
            // ideally we would just click the button now, but we can't do that through jwebunit yet!
            if (buttonToClick.getID() != null)
            {
                clickButton(buttonToClick.getID());
            }
            else
            {
                if (buttonToClick.getName() != null)
                {
                    setFormElement(buttonToClick.getName(), buttonToClick.getValue());
                }

                submit();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(ExceptionUtility.stackTraceToString(e));
        }
    }

    /**
     * Click the first button you find with the given value. Ignores duplicate values.
     *
     * @param value the value attribute of the of the button element to click.
     */
    protected void clickAnyButtonWithValue(String value)
    {
        assertAnyButtonPresentWithValue(value);
        Button buttonToClick = null;
        Button[] buttons = getDialog().getForm().getButtons();
        for (Button button : buttons)
        {
            if (value.equals(button.getValue()))
            {
                buttonToClick = button;
                break; // we've already tested for multiple buttons in assert above
            }
        }

        try
        {
            // Stops IDEA's NPE complier warning, although assertAnyButtonPresentWithValue() would already cover this.
            assertNotNull(buttonToClick);
            // ideally we would just click the button now, but we can't do that through jwebunit yet!
            if (buttonToClick.getID() != null)
            {
                clickButton(buttonToClick.getID());
            }
            else
            {
                if (buttonToClick.getName() != null)
                {
                    setFormElement(buttonToClick.getName(), buttonToClick.getValue());
                }

                submit();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(ExceptionUtility.stackTraceToString(e));
        }
    }

    /**
     * Assert a single button exists with the given value. Barfs if 0 or > 1 buttons found.
     *
     * @param value the value attribute of the of the button element to click.
     */
    private void assertButtonPresentWithValue(String value)
    {
        Button buttonToAssert = null;
        Button[] buttons = getDialog().getForm().getButtons();
        for (Button button : buttons)
        {
            if (value.equals(button.getValue()))
            {
                if (buttonToAssert != null)
                {
                    fail("Found multiple buttons with value: " + value + "\n" + foundButtons());
                }

                buttonToAssert = button;
            }
        }

        if (buttonToAssert == null)
        {
            fail("Did not find button with value: " + value + "\n" + foundButtons());
        }
    }

    /**
     * Assert any buttons are found with this value. Barfs if 0 buttons found.
     *
     * @param value the value attribute of the of the button element to click.
     */
    private void assertAnyButtonPresentWithValue(String value)
    {
        Button[] buttons = getDialog().getForm().getButtons();
        for (Button button : buttons)
        {
            if (value.equals(button.getValue()))
            {
                return;
            }
        }

        fail("Did not find button with value: " + value);
    }

    private String foundButtons()
    {
        StringBuilder result = new StringBuilder();
        Button[] buttons = getDialog().getForm().getButtons();
        for (int i = 0; i < buttons.length; i++)
        {
            Button button = buttons[i];
            result.append("Button[");
            if (button.getID() != null)
            {
                result.append("id:").append(button.getID()).append(" ");
            }

            result.append(button.getName());
            result.append("=");
            result.append(button.getValue());
            result.append("]");

            if (i != buttons.length - 1)
            {
                result.append(", ");
            }
        }

        return result.toString();
    }

    protected boolean hasLinkWithText(String linkText)
    {
        try
        {
            return getDialog().getResponse().getLinkWith(linkText) != null;
        }
        catch (SAXException e)
        {
            raiseRuntimeException(e);
        }
        return false;
    }

    protected boolean hasLinkWithName(String linkName)
    {
        try
        {
            return getDialog().getResponse().getLinkWithName(linkName) != null;
        }
        catch (SAXException e)
        {
            raiseRuntimeException(e);
        }
        return false;
    }

    protected void assertLinkWithTextExists(String message, String linkText)
    {
        assertTrue(message, hasLinkWithText(linkText));
    }

    protected void assertLinkWithTextNotPresent(String message, String linkText)
    {
        assertFalse(message, hasLinkWithText(linkText));
    }

    @Deprecated
    protected void assertLinkWithNameExists(String message, String linkName)
    {
        assertTrue(message, hasLinkWithName(linkName));
    }

    @Deprecated
    protected void assertLinkWithNameNotPresent(String message, String linkName)
    {
        assertFalse(message, hasLinkWithName(linkName));
    }

    /**
     * Asserts that the given String before occurs *before* the given String after.
     *
     * @param before supposed to come before the other.
     * @param after  supposed to come after the other.
     */
    public void assertTextPresentBeforeText(String before, String after)
    {
        log("Asserting text \"" + before + "\" is present before text \"" + after + "\"");
        assertTextPresent(before);

        String text;

        try
        {
            text = getDialog().getResponse().getText();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        int firstTextLocation = text.indexOf(before);
        int secondTextLocation = text.substring(firstTextLocation + before.length()).indexOf(after);
        assertTrue("Expected text '" + after + "' should come after '" + before + "'", secondTextLocation >= 0);
        secondTextLocation += firstTextLocation + before.length();
        assertTrue(firstTextLocation < secondTextLocation);
    }

    // Ensure that text is is displayed only once in response
    protected void assertTextPresentOnlyOnce(String text1)
    {
        assertTextPresent(text1, 1);
    }

    /**
     * Asserts that the specified text appears in the response text an exact number of times
     *
     * @param text1             the text to search for
     * @param numOccurrences    the number of times the text should occur. Must not be negative.
     * @deprecated please use {@link com.atlassian.jira.functest.framework.assertions.TextAssertions#assertTextPresentNumOccurences(String, String, int)} 
     */
    public void assertTextPresent(String text1, int numOccurrences)
    {
        if (numOccurrences < 0)
        {
            throw new IllegalArgumentException("numOccurrences must not be negative!");
        }

        log("Asserting text \"" + text1 + "\" is present exactly " + numOccurrences + " times");
        String body;

        try
        {
            body = getDialog().getResponse().getText().trim();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        int bodyLength = body.length();
        int index = -1;
        int i = 0;
        while ((index + 1 < bodyLength))
        {
            index = body.indexOf(text1, index + 1);
            if (index == -1)
            {
                break;
            }
            else
            {
                log("Number of times found: " + ++i);
            }
        }
        assertTrue("Found text: " + i + " times; expected: " + numOccurrences, (i == numOccurrences));
    }


    /**
     * Asserts that the given String after occurs *after* the given String before.
     *
     * @param after  supposed to come after the other.
     * @param before supposed to come before the other.
     */
    public void assertTextPresentAfterText(String after, String before)
    {
        assertTextPresentBeforeText(before, after);
    }

    /**
     * Makes a junit assertion that a is less than b.
     * <p/>
     * Note: Don't remove this method.  It is used by the soapclient tests!
     *
     * @param a asserted to be the smaller.
     * @param b asserted to be the greater.
     */
    protected void assertLessThan(int a, int b)
    {
        assertTrue(a < b);
    }

    /**
     * Asserts that the array of strings appears in the given order in the current response body.
     *
     * @param text the sequence to assert is present.
     */
    public void assertTextSequence(String[] text)
    {
        assertTextSequence("", text);
    }

    /**
     * Asserts that the array of strings appears in the given order in the response body, on failure, using
     * failMesgPrefix as a prefix on the assertion failure message.
     *
     * @param failMesgPrefix the message prefix in the case of a failure.
     * @param text           the array.
     */
    public void assertTextSequence(String failMesgPrefix, String[] text)
    {
        String response = getDialog().getResponseText();
        int indexOfMissing = findTextSequence(response, text);
        if (indexOfMissing != -1)
        {
            fail(failMesgPrefix + " failed to find text '" + text[indexOfMissing] + "' in sequence position " + indexOfMissing);
        }
    }

    /**
     * Attempts to find the given sequence in the given string and if it fails to find the sequence, returns the index
     * in the sequence that it failed to find a match for. If the sequence is present, returns -1.
     *
     * @param stringToSearch the String to search.
     * @param sequenceToFind the sequence to search for.
     * @return -1 if sequence is found, otherwise the index in sequenceToFind of the first text that was not found in position.
     */
    public static int findTextSequence(String stringToSearch, String[] sequenceToFind)
    {
        int startIndex = 0;
        for (int i = 0; i < sequenceToFind.length; i++)
        {
            int index = stringToSearch.indexOf(sequenceToFind[i], startIndex);
            if (index == -1)
            {
                return i;
            }
            startIndex = index + sequenceToFind[i].length();
        }
        return -1;
    }


    /**
     * Utility method that flattens a 2d array into a 1d array by stringing each inner array together in their
     * outer array order. E.g. a 2d array {{1,2},{3,4}} would become {1,2,3,4}.
     * TODO: find me a new home
     *
     * @param text the 2d array to flatten
     * @return the flattened array.
     */
    public static String[] flattenArray(String[][] text)
    {
        int newsize = 0;
        for (final String[] aText1 : text)
        {
            newsize += aText1.length;
        }
        String[] flat = new String[newsize];
        int flatPos = 0;
        for (final String[] aText : text)
        {
            System.arraycopy(aText, 0, flat, flatPos, aText.length);
            flatPos += aText.length;
        }
        return flat;
    }

    protected String getResponseText()
    {
        try
        {
            return getDialog().getResponse().getText();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
            return null;
        }
    }

    private final Map<String, String> responseTextMap = new WeakHashMap<String, String>();

    /**
     * Returns all the text nodes of the web response collapsed and then concatenated together.  Very useful for text
     * sequence detection when you don care about intermediate Elements such as anchors and spans.
     *
     * @return all the text nodes of the web respone collapsed and then concatenated together
     */
    protected String getCollapsedResponseText()
    {
        WebResponse response = getDialog().getResponse();
        String responseKey = String.valueOf(System.identityHashCode(response));
        String text = responseTextMap.get(responseKey);
        if (text == null)
        {
            // This doesnt work because of Xerces cloning issues so we have to be sneaky
            //Document doc = response.getDOM();
            Document doc = SneakyDomExtractor.getDOM(getTester());
            Element bodyE = DomKit.getBodyElement(doc);
            if (bodyE != null)
            {
                text = DomKit.getCollapsedText(bodyE);
            }
            else
            {
                text = "";
            }
            responseTextMap.put(responseKey, text);
        }
        return text;
    }


    /**
     * Asserts that the text sequence occurs somewhere in the document in the order specified.
     * It also uses the collapsed text nodes of the page response.
     *
     * @param textSequence the text sequence
     */
    protected void assertCollapsedTextSequence(String[] textSequence)
    {
        // TODO: getCollapsedResponseText() is supposed to collapse all whitespace down to a single space, but I don't think it is working.
        // TODO: It would be nice to externalise getCollapsedResponseText() so that we can test it.
        String responseText = getCollapsedResponseText();
        TextKit.assertContainsTextSequence(responseText, textSequence);
    }

    /**
     * Gets the first table on the current page that has the given class attribute.
     *
     * @param className the name of the class attribute of the desired table element.
     * @return the tables.
     */
    public WebTable getFirstWebTableWithClass(final String className)
    {
        try
        {
            return getDialog().getResponse().getFirstMatchingTable(TABLE_CLASS_IS, className);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the table on the current page that has the given ID.
     *
     * @param id ID of the required table element.
     * @return The table on the current page that has the given ID.
     */
    public WebTable getWebTableWithID(final String id)
    {
        try
        {
            return getDialog().getResponse().getTableWithID(id);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the tables on the current page that have the given class attribute.
     *
     * @param className the name of the class attribute of the desired table element.
     * @return the tables.
     */
    public WebTable[] getWebTablesWithClass(final String className)
    {
        try
        {
            return getDialog().getResponse().getMatchingTables(TABLE_CLASS_IS, className);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }
}
