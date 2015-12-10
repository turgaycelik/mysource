package com.atlassian.jira.functest.matcher;

import org.hamcrest.Description;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Matches workflow XML files that have conditions with the given class name in them.
 *
 * @since v5.0
 */
public class ConditionClassMatcher extends DocumentMatcher
{
    private final String className;

    public static ConditionClassMatcher usesConditionClass(String className)
    {
        return new ConditionClassMatcher(className);
    }

    public ConditionClassMatcher(String className)
    {
        this.className = className;
    }

    @Override
    protected boolean matchesDocument(@Nullable Document document) throws Exception
    {
        return document != null && hasConditionClass(document);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("a backup file that uses the condition class " + className);
    }

    private boolean hasConditionClass(Document document) throws XPathExpressionException
    {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // <arg name="class.name">com.opensymphony.workflow.util.OSUserGroupCondition</arg>
        XPathExpression findConditionClass = xpath.compile(String.format("//arg[@name='class.name' and text()='%s']", className));
        NodeList conditionClasses = (NodeList) findConditionClass.evaluate(document, XPathConstants.NODESET);

        return conditionClasses.getLength() > 0;
    }
}
