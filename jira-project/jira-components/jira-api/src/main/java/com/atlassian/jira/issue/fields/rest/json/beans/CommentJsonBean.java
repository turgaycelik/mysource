package com.atlassian.jira.issue.fields.rest.json.beans;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
* @since v5.0
*/
@JsonIgnoreProperties (ignoreUnknown = true)
public class CommentJsonBean
{
    private final static String EXPAND_RENDERED_BODY = "renderedBody";
    private final static String EXPAND_PROPERTIES = "properties";

    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private UserJsonBean author;

    @JsonProperty
    private String body;

    @JsonProperty
    private String renderedBody;

    @JsonProperty
    private UserJsonBean updateAuthor;

    @JsonProperty
    private String created;

    @JsonProperty
    private String updated;

    @JsonProperty
    private VisibilityJsonBean visibility;

    @JsonIgnore
    private boolean isVisibilitySet = false;

    @JsonIgnore
    private boolean isBodySet = false;

    private List<EntityPropertyBean> properties;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public void setAuthor(UserJsonBean author)
    {
        this.author = author;
    }

    public String getBody()
    {
        return body;
    }

    public String getRenderedBody()
    {
        return renderedBody;
    }

    public void setRenderedBody(final String renderedBody)
    {
        this.renderedBody = renderedBody;
    }

    public void setBody(String body)
    {
        this.body = body;
        this.isBodySet = true;
    }

    public UserJsonBean getUpdateAuthor()
    {
        return updateAuthor;
    }

    public void setUpdateAuthor(UserJsonBean updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    public Date getCreated()
    {
        return Dates.fromTimeString(created);
    }

    public void setCreated(Date created)
    {
        this.created = Dates.asTimeString(created);
    }

    public Date getUpdated()
    {
        return Dates.fromTimeString(updated);
    }

    public void setUpdated(Date updated)
    {
        this.updated = Dates.asTimeString(updated);
    }

    @JsonIgnore
    public boolean isBodySet()
    {
        return isBodySet;
    }

    @JsonIgnore
    public boolean isVisibilitySet()
    {
        return isVisibilitySet;
    }

    @JsonProperty
    public VisibilityJsonBean getVisibility()
    {
        return visibility;
    }

    @JsonProperty
    public void setVisibility(VisibilityJsonBean visibility)
    {
        this.visibility = visibility;
        this.isVisibilitySet = true;
    }

    @JsonProperty
    public List<EntityPropertyBean> getProperties()
    {
        return properties;
    }

    @JsonProperty
    public void setProperties(final List<Map<String, Object>> properties)
    {
        if (properties != null)
        {
            this.properties = Lists.newArrayList(Iterables.transform(properties, new Function<Map<String, Object>, EntityPropertyBean>()
            {
                @Override
                public EntityPropertyBean apply(final Map<String, Object> entityPropertyBean)
                {
                    final String key = (String) entityPropertyBean.get("key");
                    final Map<String, Object> value = (Map<String, Object>) entityPropertyBean.get("value");
                    return new EntityPropertyBean(key, new JSONObject(value).toString(), null);
                }
            }));
        }
    }

    /**
     * @deprecated Use {@link #shortBeans(java.util.Collection, JiraBaseUrls, com.atlassian.jira.security.roles.ProjectRoleManager, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<CommentJsonBean> shortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager)
    {
        return shortBeans(comments, urls, projectRoleManager, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<CommentJsonBean> shortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Collection<CommentJsonBean> result = Lists.newArrayListWithCapacity(comments.size());
        for (Comment from : comments)
        {
            result.add(shortBean(from, urls, projectRoleManager, loggedInUser, emailFormatter));
        }

        return result;
    }

    /**
     * @return null if the input is null
     * @deprecated Use {@link #shortBean(com.atlassian.jira.issue.comments.Comment, JiraBaseUrls, com.atlassian.jira.security.roles.ProjectRoleManager, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static CommentJsonBean shortBean(final Comment comment,  final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager)
    {
        return shortBean(comment, urls, projectRoleManager, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     *
     * @return null if the input is null
     */
    public static CommentJsonBean shortBean(final Comment comment,  final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (comment == null)
        {
            return null;
        }
        final CommentJsonBean bean = new CommentJsonBean();
        addNonRenderableStuff(bean, comment, urls, projectRoleManager, loggedInUser, emailFormatter);
        bean.body = comment.getBody();
        bean.setCreated(comment.getCreated());
        bean.setUpdated(comment.getUpdated());
        return bean;
    }

    /**
     * @deprecated Use {@link #renderedShortBeans(java.util.Collection, JiraBaseUrls, com.atlassian.jira.security.roles.ProjectRoleManager, com.atlassian.jira.datetime.DateTimeFormatterFactory, com.atlassian.jira.issue.RendererManager, String, com.atlassian.jira.issue.fields.renderer.IssueRenderContext, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<CommentJsonBean> renderedShortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final DateTimeFormatterFactory dateTimeFormatterFactory, final RendererManager rendererManager, final String rendererType, final IssueRenderContext renderContext)
    {
        return renderedShortBeans(comments, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<CommentJsonBean> renderedShortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final DateTimeFormatterFactory dateTimeFormatterFactory, final RendererManager rendererManager, final String rendererType, final IssueRenderContext renderContext,
            final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Collection<CommentJsonBean> result = Lists.newArrayListWithCapacity(comments.size());
        for (Comment from : comments)
        {
            result.add(renderedShortBean(from, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext, loggedInUser, emailFormatter));
        }

        return Lists.newArrayList(result);
    }

    /**
     * @deprecated Use {@link #expandedShortBeans(java.util.Collection, JiraBaseUrls, com.atlassian.jira.security.roles.ProjectRoleManager, com.atlassian.jira.datetime.DateTimeFormatterFactory, com.atlassian.jira.issue.RendererManager, String, com.atlassian.jira.issue.fields.renderer.IssueRenderContext, String, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<CommentJsonBean> expandedShortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final DateTimeFormatterFactory dateTimeFormatterFactory,
            final RendererManager rendererManager, final String rendererType, final IssueRenderContext renderContext, final String expand)
    {
        return expandedShortBeans(comments, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext, expand, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<CommentJsonBean> expandedShortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final DateTimeFormatterFactory dateTimeFormatterFactory,
            final RendererManager rendererManager, final String rendererType, final IssueRenderContext renderContext, final String expand,
            final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Collection<CommentJsonBean> result = Lists.newArrayListWithCapacity(comments.size());
        for (Comment from : comments)
        {
            result.add(expandedShortBean(from, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext, expand, loggedInUser, emailFormatter));
        }

        return Lists.newArrayList(result);
    }

    /**
     * @return null if the input is null
     * @deprecated Use {@link #renderedShortBean(com.atlassian.jira.issue.comments.Comment, JiraBaseUrls, com.atlassian.jira.security.roles.ProjectRoleManager, com.atlassian.jira.datetime.DateTimeFormatterFactory, com.atlassian.jira.issue.RendererManager, String, com.atlassian.jira.issue.fields.renderer.IssueRenderContext, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static CommentJsonBean renderedShortBean(Comment comment, JiraBaseUrls urls,
            ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory,
            RendererManager rendererManager, String rendererType, IssueRenderContext renderContext)
    {
        return renderedShortBean(comment, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     * @return null if the input is null
     */
    public static CommentJsonBean renderedShortBean(Comment comment, JiraBaseUrls urls,
            ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory,
            RendererManager rendererManager, String rendererType, IssueRenderContext renderContext,
            ApplicationUser loggedInUser, EmailFormatter emailFormatter)
    {
        if (comment == null)
        {
            return null;
        }

        final CommentJsonBean bean = new CommentJsonBean();
        addNonRenderableStuff(bean, comment, urls, projectRoleManager, loggedInUser, emailFormatter);
        if (StringUtils.isNotBlank(rendererType))
        {
            bean.body = rendererManager.getRenderedContent(rendererType, comment.getBody(), renderContext);
        }
        else
        {
            bean.body = comment.getBody();
        }
        bean.created = comment.getCreated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(comment.getCreated());
        bean.updated = comment.getUpdated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(comment.getUpdated());
        return bean;
    }

    /**
     * @return null if the input is null
     * @deprecated Use {@link #expandedShortBean(com.atlassian.jira.issue.comments.Comment, JiraBaseUrls, com.atlassian.jira.security.roles.ProjectRoleManager, com.atlassian.jira.datetime.DateTimeFormatterFactory, com.atlassian.jira.issue.RendererManager, String, com.atlassian.jira.issue.fields.renderer.IssueRenderContext, String, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static CommentJsonBean expandedShortBean(Comment comment, JiraBaseUrls urls,
            ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory,
            RendererManager rendererManager, String rendererType, IssueRenderContext renderContext, final String expand)
    {
        return expandedShortBean(comment, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext, expand, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     * @return null if the input is null
     */
    public static CommentJsonBean expandedShortBean(Comment comment, JiraBaseUrls urls,
            ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory,
            RendererManager rendererManager, String rendererType, IssueRenderContext renderContext, final String expand,
            final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (comment == null)
        {
            return null;
        }

        final CommentJsonBean bean = new CommentJsonBean();
        addNonRenderableStuff(bean, comment, urls, projectRoleManager, loggedInUser, emailFormatter);
        if (StringUtils.isNotBlank(rendererType))
        {
            bean.body = comment.getBody();

            if (expand.contains(EXPAND_RENDERED_BODY))
            {
                bean.renderedBody = rendererManager.getRenderedContent(rendererType, comment.getBody(), renderContext);
            }
        }
        else
        {
            bean.body = comment.getBody();
            if (expand.contains(EXPAND_RENDERED_BODY))
            {
                bean.renderedBody = comment.getBody();
            }
        }

        if (expand.contains(EXPAND_PROPERTIES))
        {
            bean.properties = getProperties(comment, urls);
        }

        bean.created = comment.getCreated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(comment.getCreated());
        bean.updated = comment.getUpdated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(comment.getUpdated());
        return bean;
    }

    private static List<EntityPropertyBean> getProperties(final Comment comment, final JiraBaseUrls urls)
    {
        final ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getUser();
        final CommentPropertyService commentPropertyService = ComponentAccessor.getComponent(CommentPropertyService.class);

        EntityPropertyService.PropertyKeys<Comment> keys = commentPropertyService.getPropertiesKeys(applicationUser, comment.getId());

        Iterable<Option<EntityPropertyBean>> propertyBeans = Iterables.transform(keys.getKeys(), new Function<String, Option<EntityPropertyBean>>()
        {
            @Override
            public Option<EntityPropertyBean> apply(final String key)
            {
                EntityPropertyService.PropertyResult property =
                        commentPropertyService.getProperty(applicationUser, comment.getId(), key);

                return Option.option(property.getEntityProperty().fold(new Supplier<EntityPropertyBean>()
                {
                    @Override
                    public EntityPropertyBean get()
                    {
                        return null;
                    }
                }, new Function<EntityProperty, EntityPropertyBean>()
                {
                    @Override
                    public EntityPropertyBean apply(final EntityProperty entityProperty)
                    {
                        return EntityPropertyBean.builder(urls, new EntityPropertyBeanSelfFunctions.CommentPropertySelfFunction())
                                .key(key)
                                .value(entityProperty.getValue())
                                .build(comment.getId());
                    }
                }));
            }
        });

        return Lists.newArrayList(Options.flatten(propertyBeans));
    }

    private static void addNonRenderableStuff(CommentJsonBean bean, @Nonnull final Comment comment, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        bean.self = urls.restApi2BaseUrl() + "issue/" + comment.getIssue().getId() + "/comment/" + JiraUrlCodec.encode(comment.getId().toString());
        bean.id = comment.getId().toString();
        bean.author = UserJsonBean.shortBean(comment.getAuthorApplicationUser(), urls, loggedInUser, emailFormatter);
        bean.updateAuthor = UserJsonBean.shortBean(comment.getUpdateAuthorApplicationUser(), urls, loggedInUser, emailFormatter);
        bean.visibility = getVisibilityBean(comment, projectRoleManager);
    }

    private static VisibilityJsonBean getVisibilityBean(Comment comment, ProjectRoleManager projectRoleManager)
    {
        VisibilityJsonBean visibilityBean = null;
        final String groupLevel = comment.getGroupLevel();
        if (groupLevel != null)
        {
            visibilityBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, groupLevel);
        }
        else
        {
            final Long roleId = comment.getRoleLevelId();
            if (roleId != null)
            {
                final String roleName = projectRoleManager.getProjectRole(roleId).getName();
                visibilityBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, roleName);
            }
        }
        return visibilityBean;
    }

    public static final CommentJsonBean DOC_EXAMPLE = new CommentJsonBean();
    public static final CommentJsonBean DOC_UPDATE_EXAMPLE = new CommentJsonBean();
    static
    {
        DOC_EXAMPLE.setId("10000");
        DOC_EXAMPLE.setSelf("http://www.example.com/jira/rest/api/2/issue/10010/comment/10000");
        DOC_EXAMPLE.setAuthor(UserJsonBean.USER_SHORT_DOC_EXAMPLE);
        DOC_EXAMPLE.setUpdateAuthor(UserJsonBean.USER_SHORT_DOC_EXAMPLE);
        DOC_EXAMPLE.setBody("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.");
        DOC_EXAMPLE.setCreated(new Date());
        DOC_EXAMPLE.setUpdated(new Date());
        DOC_EXAMPLE.setVisibility(new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, "Administrators"));
        DOC_UPDATE_EXAMPLE.setBody("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.");
        DOC_UPDATE_EXAMPLE.setVisibility(new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, "Administrators"));
    }

}
