package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraArrayUtils;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Will add a new issue type and then redirect the user back to the modify issue type scheme page.
 *
 * @since v5.0.1
 */
@WebSudoRequired
public class AddNewIssueTypeToScheme extends JiraWebActionSupport implements AddIssueTypeAction
{
    private static final String PARAM_SCHEME_NAME = "schemeName";
    private static final String PARAM_SCHEME_ID = "schemeId";
    private static final String PARAM_FIELD_ID = "fieldId";
    private static final String PARAM_PROJECT_ID = "projectId";
    private static final String PARAM_SCHEME_DESCRIPTION = "schemeDescription";
    private static final String PARAM_SELECTED_OPTIONS = "selectedOptions";
    private static final String PARAM_DEFAULT_OPTION = "defaultOption";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DESCRIPTION = "description";

    private final ManageableOptionType manageableOptionType;
    private final ConstantsManager constantsManager;
    private final IssueTypeSchemeManager schemeManager;

    private String iconurl = ViewIssueTypes.NEW_ISSUE_TYPE_DEFAULT_ICON;
    private String style;
    private String name;
    private String description;
    private IssueType newIssueType;

    private String fieldId;
    private Long schemeId;
    private Long projectId;
    private String schemeName;
    private String schemeDescription;
    private String[] selectedOptions;
    private String defaultOption;

    public AddNewIssueTypeToScheme(ManageableOptionType manageableOptionType, ConstantsManager constantsManager,
            IssueTypeSchemeManager schemeManager)
    {
        this.manageableOptionType = manageableOptionType;
        this.constantsManager = constantsManager;
        this.schemeManager = schemeManager;
    }

    @Override
    public String getIconurl()
    {
        return iconurl;
    }

    @Override
    public void setIconurl(String iconUrl)
    {
        this.iconurl = iconUrl;
    }

    @Override
    public String getStyle()
    {
        return style;
    }

    @Override
    public void setStyle(String style)
    {
        this.style = style;
    }

    @Override
    public String getSubmitUrl()
    {
        return "AddNewIssueTypeToScheme.jspa";
    }

    @Override
    public String getCancelUrl()
    {
        return createViewUrl(null);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public void setSchemeId(Long schemeId)
    {
        this.schemeId = schemeId;
    }

    public String getDefaultOption()
    {
        return defaultOption;
    }

    public void setDefaultOption(String defaultOption)
    {
        this.defaultOption = defaultOption;
    }

    public String[] getSelectedOptions()
    {
        return selectedOptions;
    }

    public void setSelectedOptions(final String[] selectedOptions)
    {
        // Iff the one and only select is null
        if (JiraArrayUtils.isContainsOneBlank(selectedOptions))
        {
            this.selectedOptions = new String[0];
        }
        else
        {
            this.selectedOptions = selectedOptions;
        }
    }

    public String getSchemeName()
    {
        return schemeName;
    }

    public void setSchemeName(String schemeName)
    {
        this.schemeName = schemeName;
    }

    public String getSchemeDescription()
    {
        return schemeDescription;
    }

    public void setSchemeDescription(String schemeDescription)
    {
        this.schemeDescription = schemeDescription;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    @Override
    public List<Pair<String, Object>> getHiddenFields()
    {
        final ImmutableList.Builder<Pair<String, Object>> builder = builder();
        builder.add(Pair.<String, Object>nicePairOf(PARAM_SCHEME_NAME, getSchemeName()));
        builder.add(Pair.<String, Object>nicePairOf(PARAM_SCHEME_DESCRIPTION, getSchemeDescription()));
        builder.add(Pair.<String, Object>nicePairOf(PARAM_SCHEME_ID, getSchemeId()));
        builder.add(Pair.<String, Object>nicePairOf(PARAM_PROJECT_ID, getProjectId()));
        builder.add(Pair.<String, Object>nicePairOf(PARAM_FIELD_ID, getFieldId()));
        builder.add(Pair.<String, Object>nicePairOf(PARAM_DEFAULT_OPTION, getDefaultOption()));

        final String[] options = getSelectedOptions();

        if (options != null)
        {
            for (String s : options)
            {
                builder.add(Pair.<String, Object>nicePairOf(PARAM_SELECTED_OPTIONS, s));
            }
        }

        return builder.build();
    }

    @Override
    public ManageableOptionType getManageableOption()
    {
        return manageableOptionType;
    }

    public IssueConstantOption getNewIssueType()
    {
        return newIssueType == null ? null : new IssueConstantOption(newIssueType);
    }

    public Collection<IssueConstantOption> getAllOptions()
    {
        final Collection<?> constants = constantsManager.getConstantObjects(getManageableOption().getFieldId());
        return copyOf(transform(constants, new Function<Object, IssueConstantOption>()
        {
            @Override
            public IssueConstantOption apply(Object input)
            {
                return new IssueConstantOption((IssueConstant) input);
            }
        }));
    }

    public String doInput()
    {
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        final String avatarId = getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID);
        constantsManager.validateCreateIssueTypeWithAvatar(getName(), getStyle(), getDescription(), avatarId, this, "name");
    }

    @RequiresXsrfCheck
    protected String doExecute() throws CreateException
    {
        final String avatarId = getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID);
        final long avatarIdLong;
        try
        {
            avatarIdLong = Long.parseLong(avatarId);
        }
        catch (Exception x)
        {
            final String message = "System inconsistent. Invalid value when looking for " + APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID;
            log.error(message, x);
            throw new CreateException(message, x);
        }
        newIssueType = constantsManager.insertIssueType(getName(), null, getStyle(), getDescription(),
                Long.valueOf(avatarId));

        // Add to default scheme
        final String createdId = newIssueType.getId();
        schemeManager.addOptionToDefault(createdId);

        if (isInlineDialogMode())
        {
            returnComplete();
            return SUCCESS;
        }
        else
        {
            return getRedirect(createViewUrl(createdId));
        }
    }

    private String createViewUrl(String createdId)
    {
        final UrlBuilder builder = new UrlBuilder("ConfigureOptionSchemes!input.jspa");
        builder.addParameter(PARAM_NAME, getSchemeName()).addParameter(PARAM_DESCRIPTION, getSchemeDescription());
        builder.addParameter(PARAM_SCHEME_ID, getSchemeId()).addParameter(PARAM_FIELD_ID, getFieldId());
        builder.addParameter(PARAM_PROJECT_ID, getProjectId());
        builder.addParameter(PARAM_DEFAULT_OPTION, getDefaultOption());

        String[] options = getSelectedOptions();
        if (options != null)
        {
            for (String selectedOption : options)
            {
                builder.addParameter(PARAM_SELECTED_OPTIONS, selectedOption);
            }
        }

        if (createdId != null)
        {
            builder.addParameter(PARAM_SELECTED_OPTIONS, createdId);
        }
        return builder.asUrlString();
    }
}
