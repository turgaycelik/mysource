package com.atlassian.jira.mock.plugin.webfragments;

import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
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
 * Builds and initializes mock instaces of {@link JiraWebItemModuleDescriptor}.
 *
 * @since v4.2
 */
public class MockJiraWebItemDescriptorBuilder
{
    private String key;
    private String section;
    private String labelKey;
    private String label;
    private String tooltip;
    private String linkId;
    private String linkUrl;
    private JiraAuthenticationContext context;
    // TODO add rest if necessary

    public MockJiraWebItemDescriptorBuilder itemKey(String key)
    {
        this.key = key;
        return this;
    }

    public MockJiraWebItemDescriptorBuilder section(String section)
    {
        this.section = section;
        return this;
    }

    public MockJiraWebItemDescriptorBuilder label(String label)
    {
        return label(label, label);
    }

    public MockJiraWebItemDescriptorBuilder label(String labelKey, String labelText)
    {
        this.labelKey = labelKey;
        this.label = labelText;
        return this;
    }

    public MockJiraWebItemDescriptorBuilder link(String linkId, String linkUrl)
    {
        this.linkId = linkId;
        this.linkUrl = linkUrl;
        return this;
    }

    public MockJiraWebItemDescriptorBuilder tooltip(String tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public MockJiraWebItemDescriptorBuilder authenticationContext(JiraAuthenticationContext ctx)
    {
        context = ctx;
        return this;
    }

    public JiraWebItemModuleDescriptor build()
    {
        I18nHelper helper = newMockI18n();
        JiraWebItemModuleDescriptor newMock = new JiraWebItemModuleDescriptor(mockContext(helper),
                newMockWebInterfaceManager(helper));
        driveLifeCycle(newMock, newItemElement(newLabelElement(this.label),
                newLabelElement(this.tooltip), newLinkElement()));
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
            return new MockI18nHelper().stubWith(this.labelKey, this.label).stubWith(this.tooltip, this.tooltip);
    }

    private JiraAuthenticationContext newMockAuthenticationContext(I18nHelper i18n)
    {
        JiraAuthenticationContext mockCtx = createNiceMock(JiraAuthenticationContext.class);
        expect(mockCtx.getI18nHelper()).andReturn(i18n).anyTimes();
        expect(mockCtx.getLocale()).andReturn(Locale.getDefault()).anyTimes();
        replay(mockCtx);
        return mockCtx;
    }


    private void driveLifeCycle(JiraWebItemModuleDescriptor tested, Element webItemElement)
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

    private Element newItemElement(Element labelElement, Element tooltipElement, Element linkElement)
    {
        Element newItem = createNiceMock(Element.class);
        expect(newItem.attributeValue("key")).andReturn(key).anyTimes();
        expect(newItem.attributeValue("section")).andReturn(section).anyTimes();
        stubForLabel(labelElement, newItem);
        stubForTooltip(tooltipElement, newItem);
        stubForLink(linkElement, newItem);
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

    private void stubForTooltip(final Element tooltipElement, final Element newItem)
    {
        if (tooltipElement != null)
        {
            expect(newItem.element("tooltip")).andReturn(tooltipElement).anyTimes();
        }
    }

    private void stubForLink(final Element linkElement, final Element newItem)
    {
        if (linkElement != null)
        {
            expect(newItem.element("link")).andReturn(linkElement).anyTimes();
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

    private Element newLinkElement()
    {
        if (linkId == null)
        {
            return null;
        }
        Element newLink = createNiceMock(Element.class);
        expect(newLink.getTextTrim()).andReturn(linkUrl).anyTimes();
        expect(newLink.attributeValue("linkId")).andReturn(linkId).anyTimes();
        replay(newLink);
        return newLink;
    }
}
