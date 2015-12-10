package com.atlassian.jira.functest.matcher;

import org.hamcrest.Description;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * Hamcrest matcher used to verify that a JIRA XML backup has a given build number in it. This is used in upgrade task
 * tests to ensure that the upgrade is actually being performed.
 *
 * @since v5.0
 */
public class BuildNumberMatcher extends DocumentMatcher
{
    /**
     * Does the backup file have the given build number.
     *
     * @param buildNumber a build number
     * @return a BuildNumberMatcher
     */
    public static BuildNumberMatcher hasBuildNumber(int buildNumber)
    {
        return new BuildNumberMatcher(buildNumber);
    }

    /**
     * The expected build number.
     */
    private final int expectedBuildNumber;

    /**
     * Creates a new BuildNumberMatcher.
     *
     * @param expectedBuildNumber a build number
     */
    public BuildNumberMatcher(int expectedBuildNumber)
    {
        this.expectedBuildNumber = expectedBuildNumber;
    }

    @Override
    protected boolean matchesDocument(@Nullable Document doc) throws Exception
    {
        return doc != null && (readBuildNumberFrom(doc) == expectedBuildNumber);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("a backup file with buildnumber equal to " + expectedBuildNumber);
    }

    /**
     * Reads the build number from a JIRA XML backup using DOM + XPath.
     *
     * @param doc a Document containing the XML backup
     * @return an int containing the build number
     * @throws Exception if anything goes wrong
     */
    protected int readBuildNumberFrom(Document doc) throws Exception
    {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // <OSPropertyEntry id="10023" entityName="jira.properties" entityId="1" propertyKey="jira.version.patched" type="5"/>
        XPathExpression findOsPropertyId = xpath.compile("//OSPropertyEntry[@propertyKey='jira.version.patched']");
        Node osPropertyEntry = (Node) findOsPropertyId.evaluate(doc, XPathConstants.NODE);
        if (osPropertyEntry != null)
        {
            // <OSPropertyString id="10023" value="700"/>
            String osPropertyId = osPropertyEntry.getAttributes().getNamedItem("id").getTextContent();
            XPathExpression findOsPropertyString = xpath.compile(String.format("//OSPropertyString[@id='%s']", osPropertyId));

            Node osPropertyString = (Node) findOsPropertyString.evaluate(doc, XPathConstants.NODE);
            if (osPropertyString != null)
            {
                return Integer.valueOf(osPropertyString.getAttributes().getNamedItem("value").getTextContent());
            }
        }

        throw new RuntimeException();
    }
}
