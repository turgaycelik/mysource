package com.atlassian.jira.gadgets.system.util;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.gadgets.system.StatsMarkup;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

/**
 * Util object for transformaing a field/value into a displayable format for the StatsGadget. This implementation
 * of FieldValueToDisplayTransformer returns a StatsMarkup object containing html markup and css classes specifically
 * for use with the StatsGadget
 *
 * @since v4.1
 */
public class StatsMarkupFieldValueToDisplayTransformer implements FieldValueToDisplayTransformer<StatsMarkup>
{

    private final JiraAuthenticationContext authenticationContext;
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final VelocityRequestContextFactory contextFactory;
    private final SoyTemplateRendererProvider soyTemplateRendererProvider;

    public StatsMarkupFieldValueToDisplayTransformer(final JiraAuthenticationContext authenticationContext,
            final ConstantsManager constantsManager, final CustomFieldManager customFieldManager,
            final VelocityRequestContextFactory contextFactory, final SoyTemplateRendererProvider soyTemplateRendererProvider)
    {
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.contextFactory = contextFactory;
        this.soyTemplateRendererProvider = soyTemplateRendererProvider;
    }

    @Override
    public StatsMarkup transformFromIrrelevant(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        final String html = makeMarkupForCellWithHtmlSafeText(i18n.getText("common.concepts.irrelevant"), url);
        return new StatsMarkup(html);
    }

    @Override
    public StatsMarkup transformFromProject(final String fieldType, final Object input, final String url)
    {
        return generateProjectMarkup(input, url);
    }

    @Override
    public StatsMarkup transformFromAssignee(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateAssigneesMarkup(input, url, i18n);
    }

    @Override
    public StatsMarkup transformFromReporter(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateReporterMarkup(input, url, i18n);
    }

    @Override
    public StatsMarkup transformFromCreator(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateCreatorMarkup(input, url, i18n);
    }

    @Override
    public StatsMarkup transformFromResolution(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if (input == null)
        {
            final String html = makeMarkupForCellWithHtmlSafeText(i18n.getText("common.resolution.unresolved"), url);
            return new StatsMarkup(html);
        }
        return generateConstantsMarkup(input, url);
    }

    @Override
    public StatsMarkup transformFromPriority(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if (input == null)
        {
            final String html = makeMarkupForCellWithHtmlSafeText(i18n.getText("gadget.filterstats.priority.nopriority"), url);
            return new StatsMarkup(html);
        }
        return generateConstantsMarkup(input, url);

    }

    @Override
    public StatsMarkup transformFromIssueType(final String fieldType, final Object input, final String url)
    {
        return generateConstantsMarkup(input, url);
    }

    @Override
    public StatsMarkup transformFromStatus(final String fieldType, final Object input, final String url)
    {
        return generateStatusMarkup(input, url);
    }

    @Override
    public StatsMarkup transformFromComponent(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateComponentsMarkup(input, url, i18n);
    }

    @Override
    public StatsMarkup transformFromVersion(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateVersionMarkup(input, url, i18n, "gadget.filterstats.raisedin.unscheduled");
    }

    @Override
    public StatsMarkup transformFromFixFor(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return generateVersionMarkup(input, url, i18n, "gadget.filterstats.fixfor.unscheduled");

    }

    @Override
    public StatsMarkup transformFromLabels(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if(input != null)
        {
            return new StatsMarkup(makeMarkupForCellWithHtmlUnsafeText(((Label) input).getLabel(), url));
        }
        return new StatsMarkup(makeMarkupForCellWithHtmlSafeText(i18n.getText("gadget.filterstats.labels.none"), url));
    }

    @Override
    public StatsMarkup transformFromCustomField(final String fieldType, final Object input, final String url)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        if (input != null)
        {
            final CustomField customField = customFieldManager.getCustomFieldObject(fieldType);

            if (customField != null)
            {
                final CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
                final CustomFieldSearcherModuleDescriptor moduleDescriptor = searcher.getDescriptor();
                return new StatsMarkup(moduleDescriptor.getStatHtml(customField, input, url));
            }
        }
        final String html = makeMarkupForCellWithHtmlSafeText(i18n.getText("common.words.none"), url);
        return new StatsMarkup(html);

    }

    private StatsMarkup generateVersionMarkup(final Object input, final String url, final I18nHelper i18n, final String noneKey)
    {
        if (input != null)
        {
            final Version version = (Version) input;
            final String html = makeMarkupForCellWithHtmlUnsafeDescriptionAndText(version.getName(), version.getDescription(), url);
            return new StatsMarkup(html, getVersionClasses(version));
        }
        else
        {
            final String html = makeMarkupForCellWithHtmlSafeText(i18n.getText(noneKey), url);
            return new StatsMarkup(html);
        }
    }

    private StatsMarkup generateComponentsMarkup(final Object input, final String url, final I18nHelper i18n)
    {
        if (input != null)
        {
            final String name;
            final String desc;
            if (input instanceof ProjectComponent)
            {
                final ProjectComponent component = (ProjectComponent) input;
                name = component.getName();
                desc = component.getDescription();
            }
            else
            {
                final GenericValue gv = (GenericValue) input;
                name = gv.getString("name");
                desc = gv.getString("description");
            }
            final String html = makeMarkupForCellWithHtmlUnsafeDescriptionAndText(name, desc, url);
            final List<String> classes = CollectionBuilder.list("default_image", "default_image_component");

            return new StatsMarkup(html, classes);
        }
        else
        {
            final String html = makeMarkupForCellWithHtmlSafeText(i18n.getText("gadget.filterstats.component.nocomponent"), url);
            return new StatsMarkup(html);
        }
    }

    private StatsMarkup generateConstantsMarkup(final Object input, final String url)
    {
        final IssueConstant constant;
        if (input instanceof GenericValue)
        {
            constant = constantsManager.getIssueConstant((GenericValue) input);
        }
        else
        {
            constant = (IssueConstant) input;
        }
        final String html = makeConstantMarkup(constant, url);
        final List<String> classes = CollectionBuilder.list("gadget_image");
        return new StatsMarkup(html, classes);
    }

    private StatsMarkup generateStatusMarkup(final Object input, final String url)
    {
        final SoyTemplateRenderer renderer = soyTemplateRendererProvider.getRenderer();
        final IssueConstant constant;
        final SimpleStatus simpleStatus;
        final String result;

        if (input instanceof GenericValue)
        {
            constant = constantsManager.getIssueConstant((GenericValue) input);
        }
        else
        {
            constant = (IssueConstant) input;
        }

        ImmutableMap.Builder<String, Object> params = ImmutableMap.builder();
        simpleStatus = constantsManager.getStatusObject(constant.getId()).getSimpleStatus();
        params.put("issueStatus", simpleStatus);
        params.put("isSubtle", true);

        try
        {
            result = renderer.render("jira.webresources:issue-statuses", "JIRA.Template.Util.Issue.Status.issueStatusResolver", params.build());
        }
        catch (SoyException e)
        {
            throw new RuntimeException(e);
        }

        return new StatsMarkup(result);
    }

    private StatsMarkup generateReporterMarkup(final Object input, final String url, final I18nHelper i18n)
    {
        final String html;
        if (input != null)
        {
            final User assignee = (User) input;
            html = makeMarkupForCellWithHtmlUnsafeText(assignee.getDisplayName(), url);
        }
        else
        {
            html = makeMarkupForCellWithHtmlSafeText(i18n.getText("common.concepts.no.reporter"), url);
        }
        return new StatsMarkup(html);
    }

    private StatsMarkup generateCreatorMarkup(final Object input, final String url, final I18nHelper i18n)
    {
        final String html;
        if (input != null)
        {
            final User creator = (User) input;
            html = makeMarkupForCellWithHtmlUnsafeText(creator.getDisplayName(), url);
        }
        else
        {
            html = makeMarkupForCellWithHtmlSafeText(i18n.getText("common.concepts.anonymous.creator"), url);
        }
        return new StatsMarkup(html);
    }


    private StatsMarkup generateAssigneesMarkup(final Object input, final String url, final I18nHelper i18n)
    {
        final String html;
        if (input != null)
        {
            final User assignee = (User) input;
            html = makeMarkupForCellWithHtmlUnsafeText(assignee.getDisplayName(), url);
        }
        else
        {
            html = makeMarkupForCellWithHtmlSafeText(i18n.getText("gadget.filterstats.assignee.unassigned"), url);
        }

        return new StatsMarkup(html);
    }

    private StatsMarkup generateProjectMarkup(final Object input, final String url)
    {
        final String html;
        if (input instanceof GenericValue)
        {
            final GenericValue project = (GenericValue) input;
            html = makeMarkupForCellWithHtmlUnsafeText(project.getString("name"), url);
        }
        else
        {
            final Project project = (Project) input;
            html = makeMarkupForCellWithHtmlUnsafeText(project.getName(), url);
        }
        return new StatsMarkup(html);
    }

    private String makeConstantMarkup(final IssueConstant constant, final String url)
    {
        return makeConstantIconMarkup(constant) + makeMarkupForCellWithHtmlUnsafeDescriptionAndText(constant.getNameTranslation(), constant.getDescTranslation(), url);
    }

    private String makeConstantIconMarkup(final IssueConstant constant)
    {
        final String iconUrl = constant.getIconUrl();
        if (iconUrl != null && !"".equals(iconUrl))
        {
            if (iconUrl.startsWith("http://") || iconUrl.startsWith("https://"))
            {
                return makeImageMarkup(constant, iconUrl);
            }
            else
            {
                final VelocityRequestContext context = contextFactory.getJiraVelocityRequestContext();
                final String baseUrl = context.getCanonicalBaseUrl();

                return makeImageMarkup(constant, baseUrl + iconUrl);
            }
        }
        return "";
    }

    private String makeImageMarkup(final IssueConstant constant, final String url)
    {
        final String name = TextUtils.htmlEncode(constant.getNameTranslation());
        String result = "<img src=\"" + url + "\" height=\"16\" width=\"16\" alt=\"" + name + "\" title=\"" + name + " - ";
        final String descTranslation = constant.getDescTranslation() == null ? "" : constant.getDescTranslation();
        result += TextUtils.htmlEncode(descTranslation) + "\"/>";
        return result;
    }

    /**
     * Creates a link using given parameters. The text WON'T BE HTML-escaped. Be aware!
     * @param htmlSafeLinkText A text used as link text. It won't be additionally HTML-encoded.
     * @param url A link url.
     * @return Created HTML.
     */
    private String makeMarkupForCellWithHtmlSafeText(final String htmlSafeLinkText, final String url)
    {
        return "<a href='" + url + "'>" + htmlSafeLinkText + "</a>";
    }

    /**
     * Creates a link using given parameters. The text WILL BE HTML-escaped.
     * @param linkTextToEncode A text used in created link. Given value will be HTML-encoded.
     * @param url A link url.
     * @return Created HTML.
     */
    private String makeMarkupForCellWithHtmlUnsafeText(final String linkTextToEncode, final String url)
    {
        return makeMarkupForCellWithHtmlSafeText(TextUtils.htmlEncode(linkTextToEncode), url);
    }

    /**
     * Creates link with given parameters.Note that this method will encode given text and description.
     * @param linkTextToEncode Value that will be used for link. Given value will be HTML-encoded.
     * @param desc An optional link title. Given value will be HTML-encoded.
     * @param url A link url.
     * @return Created HTML.
     */
    private String makeMarkupForCellWithHtmlUnsafeDescriptionAndText(final String linkTextToEncode, final String desc, final String url)
    {
        if (StringUtils.isBlank(desc))
        {
            return makeMarkupForCellWithHtmlUnsafeText(linkTextToEncode, url);
        }
        return "<a href='" + url + "' title='" + TextUtils.htmlEncode(desc) + "'>" + TextUtils.htmlEncode(linkTextToEncode) + "</a>";
    }

    private List<String> getVersionClasses(final Version version)
    {
        final List<String> classes = new ArrayList<String>();
        if (version.isArchived())
        {
            classes.add("archived_version");
        }
        classes.add("default_image");

        if (version.isReleased() && !version.isArchived())
        {
            classes.add("released_unarchived_version");
        }
        else if (version.isReleased() && version.isArchived())
        {
            classes.add("released_archived_version");
        }
        else if (!version.isReleased() && !version.isArchived())
        {
            classes.add("unreleased_unarchived_version");
        }
        else if (!version.isReleased() && version.isArchived())
        {
            classes.add("unreleased_archived_version");
        }

        return classes;

    }

}
