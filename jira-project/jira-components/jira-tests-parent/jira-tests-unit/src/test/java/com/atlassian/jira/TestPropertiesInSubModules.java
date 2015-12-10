package com.atlassian.jira;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.atlassian.jira.matchers.FileMatchers.isFile;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Verifies if properties that should have the same value in project pom.xml and jira-distribution/pom.xml does have the
 * same versions.
 *
 * @since v6.1
 */
public class TestPropertiesInSubModules
{

    private static final String UNIT_TESTS_PATH = "/jira-components/jira-tests-parent/jira-tests-unit";
    private final File parentPom;
    private final File distributionPom;
    private final XPathFactory xPathFactory = XPathFactory.newInstance();
    private final DocumentBuilder documentBuilder;

    public TestPropertiesInSubModules() throws ParserConfigurationException
    {
        final String rootPath = getProjectRootPath();
        parentPom = new File(rootPath + "pom.xml");
        distributionPom = new File(rootPath + "jira-distribution/pom.xml");
        assertThat(parentPom, isFile());
        assertThat(distributionPom, isFile());
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Test
    public void testPropertiesShouldBeInSync() throws Exception
    {
        final List<String> errorMessages = Lists.newArrayList();

        // Read properties
        final Map<String, String> parentProperties = getPropertiesFromPom(parentPom);
        final Map<String, String> distributionProperties = getPropertiesFromPom(distributionPom);

        if (parentProperties.isEmpty() && distributionProperties.isEmpty())
        {
            throw new AssertionError("I could not find any properties that require sync in parent pom.xml "
                    + "and jira-distribution/pom.xml. What does that mean? I'm not needed anymore? "
                    + "Or maybe someone wanted to get rid of me?");
        }

        // Compare
        final Set<String> missingInDistribution = difference(parentProperties.keySet(), distributionProperties.keySet());
        final Set<String> missingInParent = difference(distributionProperties.keySet(), parentProperties.keySet());
        final Set<String> commonKeys = intersection(parentProperties.keySet(), distributionProperties.keySet());

        errorMessages.addAll(printMissingProperties("jira-distribution/pom.xml", missingInDistribution));
        errorMessages.addAll(printMissingProperties("parent pom.xml", missingInParent));

        for (final String key : commonKeys)
        {
            final String valParent = parentProperties.get(key);
            final String valDistribution = distributionProperties.get(key);
            if (!valParent.equals(valDistribution))
            {
                errorMessages.add(String.format(
                        "Property %s has different values!\n"
                                + "Parent      : %s\n"
                                + "Distribution: %s\n",
                        key, valParent, valDistribution));
            }
        }

        if (errorMessages.size() > 0)
        {
            fail("There are some differences between properties in parent pom.xml and "
                    + "jira-distribution/pom.xml!\n" + Joiner.on("\n").join(errorMessages));
        }
    }

    private Map<String, String> getPropertiesFromPom(final File pomFile)
    {
        try
        {
            final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

            final XPathExpression xpath = xPathFactory.newXPath().compile("/project/properties");

            final Document doc = documentBuilder.parse(pomFile);
            doc.getDocumentElement().normalize();
            final Node propsNode = (Node) xpath.evaluate(doc, XPathConstants.NODE);
            final NodeList props = propsNode.getChildNodes();

            boolean insideKeepInSync = false;
            for (int i = 0; i < props.getLength(); i++)
            {
                final Node node = props.item(i);
                if (node.getNodeType() == Node.COMMENT_NODE)
                {
                    final String comment = node.getTextContent().trim();
                    if (comment.toLowerCase().startsWith("begin.keep.those.in.sync"))
                    {
                        insideKeepInSync = true;
                    }
                    else if (comment.toLowerCase().startsWith("end.keep.those.in.sync"))
                    {
                        insideKeepInSync = false;
                        // don't break - there can be another block
                    }
                }
                else if (node.getNodeType() == Node.ELEMENT_NODE && insideKeepInSync)
                {
                    builder.put(node.getNodeName(), node.getTextContent());
                }
            }

            return builder.build();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<String> printMissingProperties(final String where, final Set<String> properties)
    {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        if (properties.size() > 0)
        {
            final List<String> sorted = Ordering.natural().sortedCopy(properties);
            builder.add("I could not find those properties in " + where + ":");
            for (final String key : sorted)
            {
                builder.add("* " + key);
            }
            builder.add("");
        }
        return builder.build();
    }

    /**
     * Tries to find out where is project root. The cwd is different for maven and for IDE.
     *
     * @return the project root path
     */
    private String getProjectRootPath()
    {
        final File distributionPomFile = new File("jira-distribution/pom.xml");
        final String rootPath;
        if (distributionPomFile.exists())
        {
            // CWD is the project root
            rootPath = "";
        }
        else
        {
            // Where I am? Maybe in unit-tests?
            final String cwd = System.getProperty("user.dir");
            if (cwd.endsWith(UNIT_TESTS_PATH))
            {
                rootPath = cwd.replace(UNIT_TESTS_PATH, "/");
            }
            else
            {
                // Not in unit tests - no idea where - project structure has changed?
                throw new AssertionError("I'm lost, I don't know where I am! Please fix me. "
                        + "The current working directory is: " + cwd);
            }
        }
        return rootPath;
    }
}
