package com.atlassian.jira.mock.plugin.webfragments;

import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebSectionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import org.dom4j.Element;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.Locale;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Builds and initializes mock instaces of {@link com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor}.
 *
 * @since v4.2
 */
public class MockJiraWebSectionDescriptorBuilder
{
    private String key;
    private String location;
    private String labelKey;
    private String label;
    private JiraAuthenticationContext context;

    public MockJiraWebSectionDescriptorBuilder sectionKey(String key)
    {
        this.key = key;
        return this;
    }

    public MockJiraWebSectionDescriptorBuilder location(String location)
    {
        this.location = location;
        return this;
    }

    public MockJiraWebSectionDescriptorBuilder label(String label)
    {
        return label(label, label);
    }

    public MockJiraWebSectionDescriptorBuilder label(String labelKey, String label)
    {
        this.labelKey = labelKey;
        this.label = label;
        return this;
    }

    public MockJiraWebSectionDescriptorBuilder authenticationContext(JiraAuthenticationContext ctx)
    {
        context = ctx;
        return this;
    }

    public JiraWebSectionModuleDescriptor build()
    {
        I18nHelper helper = newMockI18n();
        JiraWebSectionModuleDescriptor newMock = new JiraWebSectionModuleDescriptor(mockContext(helper),
                newMockWebInterfaceManager(helper));
        driveLifeCycle(newMock, newSectionElement());
        return newMock;
    }

    private JiraAuthenticationContext mockContext(I18nHelper i18n)
    {
        if (context != null)
        {
            return context;
        }
        else
        {
            return newMockAuthenticationContext(i18n);
        }
    }

    private I18nHelper newMockI18n()
    {
        return new MockI18nHelper().stubWith(this.labelKey, this.label);
    }

    private JiraAuthenticationContext newMockAuthenticationContext(I18nHelper i18n)
    {
        JiraAuthenticationContext mockCtx = createNiceMock(JiraAuthenticationContext.class);
        expect(mockCtx.getI18nHelper()).andReturn(i18n).anyTimes();
        expect(mockCtx.getLocale()).andReturn(Locale.getDefault()).anyTimes();
        replay(mockCtx);
        return mockCtx;
    }


    private void driveLifeCycle(JiraWebSectionModuleDescriptor tested, Element webItemElement)
    {
        tested.init(newMockPlugin(), webItemElement);
        tested.enabled();
    }

    private WebInterfaceManager newMockWebInterfaceManager(I18nHelper i18n)
    {
        WebInterfaceManager mockManager = EasyMock.createNiceMock(WebInterfaceManager.class);
        expect(mockManager.getWebFragmentHelper()).andReturn(newMockFragmentHelper(i18n)).anyTimes();
        replay(mockManager);
        return mockManager;
    }

    private WebFragmentHelper newMockFragmentHelper(final I18nHelper i18n)
    {
        return new MockWebFragmentHelper(i18n);
    }

    @SuppressWarnings ({ "deprecation" })
    private Plugin newMockPlugin()
    {
        Plugin mockPlugin = createNiceMock(Plugin.class);
        expect(mockPlugin.getResourceDescriptors()).andReturn(Collections.<ResourceDescriptor>emptyList()).anyTimes();
        expect(mockPlugin.getResourceDescriptors(any(String.class))).andReturn(Collections.<ResourceDescriptor>emptyList()).anyTimes();
        replay(mockPlugin);
        return mockPlugin;
    }

    private Element newSectionElement()
    {
        Element newItem = createNiceMock(Element.class);
        expect(newItem.attributeValue("key")).andReturn(key).anyTimes();
        expect(newItem.attributeValue("location")).andReturn(location).anyTimes();
        stubForLabel(newLabelElement(label), newItem);
        expect(newItem.elements("param")).andReturn(Collections.emptyList()).anyTimes();
        expect(newItem.elements("resource")).andReturn(Collections.emptyList()).anyTimes();
        replay(newItem);
        return newItem;
    }

    private void stubForLabel(final Element labelElement, final Element newItem)
    {
        if (labelElement != null)
        {
            expect(newItem.element("label")).andReturn(labelElement).anyTimes();
        }
    }

    private Element newLabelElement(String key)
    {
        if (key == null)
        {
            return null;
        }
        Element newLabel = createNiceMock(Element.class);
        expect(newLabel.attributeValue("key")).andReturn(key).anyTimes();
        expect(newLabel.elements("param")).andReturn(Collections.emptyList()).anyTimes();
        expect(newLabel.elements("resource")).andReturn(Collections.emptyList()).anyTimes();
        replay(newLabel);
        return newLabel;
    }

}
