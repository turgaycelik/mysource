package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.opensymphony.util.FileUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tests JRA-11485.  Empty strings should no longer be restored as null.
 * <p/>
 * Uses TestEmptyStringDataRestore.xml
 */
@WebTest ({ Category.FUNC_TEST, Category.IMPORT_EXPORT })
public class TestEmptyStringDataRestore extends FuncTestCase
{
    public void testEmtpyStringRestore()
            throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        // stop idea/maven from complaining about no tests in this class
        log("we must do something");
        log("this is something");
        log("therefore we must do this");
    }

    public void testEmptyStringRestore() throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        final String importFileName = "TestEmptyStringDataRestore";
        final String inFile = importFileName + ".xml";
        administration.restoreData(inFile);
        File outFile = null;

        try
        {
            outFile = administration.exportDataToFile(inFile + "_out.xml");
            outFile.deleteOnExit();

            final InputStream inputStream = new ZipFile(outFile).getInputStream(new ZipEntry("entities.xml"));
            Document doc = XMLUnit.buildControlDocument(new InputSource(inputStream));

            checkXpath("10002", "10000", ADMIN_USERNAME, "comment", "Comment3", doc);

            // The idea of this test is to ensure that XML Backup and Restore code preserves empty strings. Of course, if the database
            // does not support storing empty strings (e.g. Oracle or Sybase) we cannot really make this work.
            // So the test makes sure that empty strings are preseserved if possible
            if (isOracle)
            {
                // Oracle stores empty strings as NULLs, so test that this comment is stored as null
                checkXpath("10001", "10000", ADMIN_USERNAME, "comment", null, doc);
            }
            else
            {
                checkXpath("10001", "10000", ADMIN_USERNAME, "comment", "", doc);
            }

            checkXpath("10000", "10000", ADMIN_USERNAME, "comment", null, doc);
        }
        finally
        {
            if (outFile != null)
            {
                //noinspection ResultOfMethodCallIgnored
                outFile.delete();
            }
        }
    }

    private void checkXpath(String id, String issue, String author, String type, String body, Document doc)
            throws TransformerException
    {
        if (body == null)
        {
            String xpath = "//entity-engine-xml/Action[@id='" + id + "'][@issue='" + issue + "'][@author='" + author + "'][@type='" + type + "'][@body]";
            log("Searching for non-existence of xpath " + xpath);
            XMLAssert.assertXpathNotExists(xpath, doc);
            xpath = "//entity-engine-xml/Action[@id='" + id + "'][@issue='" + issue + "'][@author='" + author + "'][@type='" + type + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);
        }
        else if (body.length() == 0)
        {
            String xpath = "//entity-engine-xml/Action[@id='" + id + "'][@issue='" + issue + "'][@author='" + author + "'][@type='" + type + "'][@body='']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);
        }
        else
        {
            String xpath = "//entity-engine-xml/Action[@id='" + id + "'][@issue='" + issue + "'][@author='" + author + "'][@type='" + type + "'][@body='" + body + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);
        }
    }
}
