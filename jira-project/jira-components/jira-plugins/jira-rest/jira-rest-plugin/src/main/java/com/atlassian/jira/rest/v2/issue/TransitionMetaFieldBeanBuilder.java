package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.WorkflowIssueOperationImpl;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for {@link FieldMetaBean} instances, in the context of meta data for creating issues.
 *
 * @since v5.0
 */
public class TransitionMetaFieldBeanBuilder extends AbstractMetaFieldBeanBuilder
{
    private final ActionDescriptor action;
    private FieldScreenRenderer fieldScreenRenderer;

    public TransitionMetaFieldBeanBuilder(final FieldScreenRendererFactory fieldScreenRendererFactory, final FieldLayoutManager fieldLayoutManager, ActionDescriptor action, final Issue issue, final User user, VersionBeanFactory versionBeanFactory, VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo, JiraBaseUrls baseUrls)
    {
        super(fieldLayoutManager, issue.getProjectObject(), issue, issue.getIssueTypeObject(), user, versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls, null);
        this.action = action;
        fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, action);
    }

    @Override
    public OperationContext getOperationContext()
    {
        return new OperationContext()
        {
            @Override
            public Map getFieldValuesHolder()
            {
                return null;
            }

            @Override
            public IssueOperation getIssueOperation()
            {
                return new WorkflowIssueOperationImpl(action);
            }
        };
    }


    @Override
    public Map<String, FieldMetaBean> build()
    {
        final Map<String, FieldMetaBean> fields = new HashMap<String, FieldMetaBean>();


        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
         {
             for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
             {
                 if (fieldScreenRenderLayoutItem.isShow(issue))
                 {
                     OrderableField field = fieldScreenRenderLayoutItem.getOrderableField();
                     FieldLayoutItem fieldLayoutItem = fieldScreenRenderLayoutItem.getFieldLayoutItem();

                     // JRA-16112 - This is a hack that is here because the resolution field is "special". You can not
                     // make the resolution field required and therefore by default the FieldLayoutItem for resolution
                     // returns false for the isRequired method. This is so that you can not make the resolution field
                     // required for issue creation. HOWEVER, whenever the resolution system field is shown it is
                     // required because the edit template does not provide a none option and indicates that it is
                     // required. THEREFORE, when the field is included on a transition screen we will do a special
                     // check to make the FieldLayoutItem claim it is required IF we run into the resolution field.
                     final FieldMetaBean fieldMetaBean;
                     if (IssueFieldConstants.RESOLUTION.equals(field.getId()))
                     {
                         // this field is forced to be required
                         fieldMetaBean = getFieldMetaBean(true, field);
                     }
                     else
                     {
                         fieldMetaBean = getFieldMetaBean(fieldLayoutItem.isRequired(), field);
                     }
                     fields.put(field.getId(), fieldMetaBean);
                 }
             }
         }

        return fields;
    }

    @Override
    public boolean hasPermissionToPerformOperation()
    {
        return true; // should never be called by the super class
    }

    @Override
    FieldScreenRenderer getFieldScreenRenderer(Issue issue)
    {
        throw new RuntimeException("Should never call this method!");
    }
}
