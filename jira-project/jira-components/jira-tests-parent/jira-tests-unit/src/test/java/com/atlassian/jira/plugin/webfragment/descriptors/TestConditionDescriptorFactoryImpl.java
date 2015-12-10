package com.atlassian.jira.plugin.webfragment.descriptors;

import javax.annotation.Nonnull;

import com.atlassian.jira.plugin.webfragment.conditions.IsOnDemandCondition;
import com.atlassian.jira.plugin.webfragment.conditions.IsVersionReleased;
import com.atlassian.jira.plugin.webfragment.conditions.JiraGlobalPermissionCondition;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.web.Condition;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestConditionDescriptorFactoryImpl
{
    private final Plugin mockPlugin = mock(Plugin.class);
    private final HostContainer mockHostContainer = mock(HostContainer.class);

    private ConditionDescriptorFactoryImpl factory = new ConditionDescriptorFactoryImpl(mockHostContainer);

    @Test
    public void retrieveSingleConditionWithParameter() throws Exception
    {
        final String conditionClassName = JiraGlobalPermissionCondition.class.getCanonicalName();
        final JiraGlobalPermissionCondition mockCondition = mock(JiraGlobalPermissionCondition.class);
        when(mockPlugin.<JiraGlobalPermissionCondition>loadClass(conditionClassName, factory.getClass())).thenReturn(JiraGlobalPermissionCondition.class);
        when(mockHostContainer.create(JiraGlobalPermissionCondition.class)).thenReturn(mockCondition);

        final Element element = createXmlRootElement(
                "<condition class=\"" + conditionClassName + "\">" +
                "   <param name=\"permission\">admin</param>" +
                "</condition>"
        );

        final Condition condition = factory.retrieveCondition(mockPlugin, element);
        assertThat(condition, is((Condition) mockCondition));
    }

    @Test
    public void retrieveSingleConditionInConditionsElement() throws Exception
    {
        final String conditionClassName = IsOnDemandCondition.class.getCanonicalName();
        final IsOnDemandCondition mockCondition = mock(IsOnDemandCondition.class);
        when(mockPlugin.<IsOnDemandCondition>loadClass(conditionClassName, factory.getClass())).thenReturn(IsOnDemandCondition.class);
        when(mockHostContainer.create(IsOnDemandCondition.class)).thenReturn(mockCondition);

        final Element element = createXmlRootElement(
                "<conditions type=\"OR\">" +
                "   <condition class=\"" + conditionClassName + "\"/>" +
                "</conditions>\n"
        );

        final Condition condition = factory.retrieveCondition(mockPlugin, element);
        assertNotNull(condition);
    }

    @Test
    public void retrieveConditionAndConditions() throws Exception
    {
        final String firstConditionClassName = IsOnDemandCondition.class.getCanonicalName();
        final IsOnDemandCondition mockFirstCondition = mock(IsOnDemandCondition.class);
        when(mockPlugin.<IsOnDemandCondition>loadClass(firstConditionClassName, factory.getClass())).thenReturn(IsOnDemandCondition.class);
        when(mockHostContainer.create(IsOnDemandCondition.class)).thenReturn(mockFirstCondition);

        final String secondConditionClassName = IsVersionReleased.class.getCanonicalName();
        final IsVersionReleased mockSecondCondition = mock(IsVersionReleased.class);
        when(mockPlugin.<IsVersionReleased>loadClass(secondConditionClassName, factory.getClass())).thenReturn(IsVersionReleased.class);
        when(mockHostContainer.create(IsVersionReleased.class)).thenReturn(mockSecondCondition);

        final Element document = createXmlRootElement(
                "<condition class=\"" + firstConditionClassName + "\"/>" +
                "<conditions type=\"OR\">" +
                "   <condition class=\"" + secondConditionClassName + "\"/>" +
                "</conditions>"
        );

        final Condition condition = factory.retrieveCondition(mockPlugin, document);
        assertNotNull(condition);
    }

    @Test
    public void retrieveMultipleConditionElementsWithinOneConditionsElement() throws Exception
    {
        final String firstConditionClassName = IsOnDemandCondition.class.getCanonicalName();
        final IsOnDemandCondition mockFirstCondition = mock(IsOnDemandCondition.class);
        when(mockPlugin.<IsOnDemandCondition>loadClass(firstConditionClassName, factory.getClass())).thenReturn(IsOnDemandCondition.class);
        when(mockHostContainer.create(IsOnDemandCondition.class)).thenReturn(mockFirstCondition);

        final String secondConditionClassName = IsVersionReleased.class.getCanonicalName();
        final IsVersionReleased mockSecondCondition = mock(IsVersionReleased.class);
        when(mockPlugin.<IsVersionReleased>loadClass(secondConditionClassName, factory.getClass())).thenReturn(IsVersionReleased.class);
        when(mockHostContainer.create(IsVersionReleased.class)).thenReturn(mockSecondCondition);

        final Element document = createXmlRootElement(
                "<conditions type=\"OR\">" +
                "   <condition class=\"" + firstConditionClassName + "\"/>" +
                "   <condition class=\"" + secondConditionClassName + "\"/>" +
                "</conditions>"
        );

        final Condition condition = factory.retrieveCondition(mockPlugin, document);
        assertNotNull(condition);
    }

    @Test
    public void retrieveMultipleConditionElements() throws Exception
    {
        final String firstConditionClassName = IsOnDemandCondition.class.getCanonicalName();
        final IsOnDemandCondition mockFirstCondition = mock(IsOnDemandCondition.class);
        when(mockPlugin.<IsOnDemandCondition>loadClass(firstConditionClassName, factory.getClass())).thenReturn(IsOnDemandCondition.class);
        when(mockHostContainer.create(IsOnDemandCondition.class)).thenReturn(mockFirstCondition);

        final String secondConditionClassName = IsVersionReleased.class.getCanonicalName();
        final IsVersionReleased mockSecondCondition = mock(IsVersionReleased.class);
        when(mockPlugin.<IsVersionReleased>loadClass(secondConditionClassName, factory.getClass())).thenReturn(IsVersionReleased.class);
        when(mockHostContainer.create(IsVersionReleased.class)).thenReturn(mockSecondCondition);

        final Element document = createXmlRootElement(
                "<condition class=\"" + firstConditionClassName + "\"/>" +
                "<condition class=\"" + secondConditionClassName + "\"/>"
        );

        final Condition condition = factory.retrieveCondition(mockPlugin, document);
        assertNotNull(condition);
    }

    @Test
    public void retrieveMultipleConditionsElements() throws Exception
    {
        final String firstConditionClassName = IsOnDemandCondition.class.getCanonicalName();
        final IsOnDemandCondition mockFirstCondition = mock(IsOnDemandCondition.class);
        when(mockPlugin.<IsOnDemandCondition>loadClass(firstConditionClassName, factory.getClass())).thenReturn(IsOnDemandCondition.class);
        when(mockHostContainer.create(IsOnDemandCondition.class)).thenReturn(mockFirstCondition);

        final String secondConditionClassName = IsVersionReleased.class.getCanonicalName();
        final IsVersionReleased mockSecondCondition = mock(IsVersionReleased.class);
        when(mockPlugin.<IsVersionReleased>loadClass(secondConditionClassName, factory.getClass())).thenReturn(IsVersionReleased.class);
        when(mockHostContainer.create(IsVersionReleased.class)).thenReturn(mockSecondCondition);

        final Element document = createXmlRootElement(
                "<conditions type=\"OR\">" +
                "   <condition class=\"" + firstConditionClassName + "\"/>" +
                "</conditions>" +
                "<conditions type=\"OR\">" +
                "   <condition class=\"" + secondConditionClassName + "\"/>" +
                "</conditions>"
        );

        final Condition condition = factory.retrieveCondition(mockPlugin, document);
        assertNotNull(condition);
    }

    @Test
    public void retrieveMissingClassAttributeForConditionElementResultsInFailure() throws DocumentException
    {
        final Element rootElement = createXmlRootElement("<condition />");

        try
        {
            factory.retrieveCondition(mockPlugin, rootElement);
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }


    @Test
    public void testMissingClassAttributeForConditionsElementResultsInFailure() throws Exception
    {
        final Element rootElement = createXmlRootElement("<conditions type=\"OR\"><condition /></conditions>\n");

        try
        {
            factory.retrieveCondition(mockPlugin, rootElement);
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    @Test
    public void testClassNotFoundPropagated() throws Exception
    {
        final String unknownClassName = "foobar";
        final Element rootElement = createXmlRootElementWithConditionClass(unknownClassName);

        try
        {
            when(mockPlugin.loadClass(unknownClassName, factory.getClass())).thenThrow(new ClassNotFoundException());

            factory.retrieveCondition(mockPlugin, rootElement);
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    @Test
    public void testClassUninstantiatablePropagated() throws Exception
    {
        final String conditionClassName = IsOnDemandCondition.class.getCanonicalName();
        final Element rootElement = createXmlRootElementWithConditionClass(conditionClassName);

        try
        {
            when(mockPlugin.<IsOnDemandCondition>loadClass(conditionClassName, factory.getClass())).thenReturn(IsOnDemandCondition.class);
            when(mockHostContainer.create(IsOnDemandCondition.class)).thenThrow(new IllegalArgumentException("class is uninstantiatable"));

            factory.retrieveCondition(mockPlugin, rootElement);
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    private Element createXmlRootElementWithConditionClass(@Nonnull final String conditionClassName) throws DocumentException
    {
        return createXmlRootElement("<condition class=\"" + conditionClassName + "\"/>");
    }

    private Element createXmlRootElement(@Nonnull final String text) throws DocumentException
    {
        final Document document = DocumentHelper.parseText("<container>" + text + "</container>");
        return document.getRootElement();
    }

}
