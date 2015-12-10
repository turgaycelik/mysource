package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.impl.LabelsCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.AlphabeticalLabelRenderer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.gzipfilter.org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Labels Resource for the label gadget
 *
 * @since v4.2
 */
@Path ("labels")
@AnonymousAllowed
@Produces (MediaType.APPLICATION_JSON)
public class LabelsResource
{
    private static final Logger log = Logger.getLogger(LabelsResource.class);

    private final CustomFieldManager customFieldManager;
    private final JiraAuthenticationContext authenticationContext;
    private AlphabeticalLabelRenderer alphabeticalLabelRenderer;

    public LabelsResource(final CustomFieldManager customFieldManager,
            final JiraAuthenticationContext authenticationContext,
            final AlphabeticalLabelRenderer alphabeticalLabelRenderer)
    {
        this.customFieldManager = customFieldManager;
        this.authenticationContext = authenticationContext;
        this.alphabeticalLabelRenderer = alphabeticalLabelRenderer;
    }

    @GET
    @Path ("gadget/fields")
    @Produces (MediaType.APPLICATION_JSON)
    public Response getLabelFields()
    {
        @SuppressWarnings ("unchecked")
        final Collection<CustomField> labelCfTypes = CollectionUtil.filter(
                customFieldManager.getCustomFieldObjects(), new Predicate<CustomField>()
                {
                    public boolean evaluate(final CustomField input)
                    {
                        return input.getCustomFieldType() instanceof LabelsCFType;
                    }
                }
        );

        final List<LabelField> labelFields = new ArrayList<LabelField>();
        labelFields.add(new LabelField(authenticationContext.getI18nHelper().getText("issue.field.labels"),
                IssueFieldConstants.LABELS));
        for (CustomField labelCf : labelCfTypes)
        {
            LabelField labelField = new LabelField(labelCf.getName(), labelCf.getId());
            labelFields.add(labelField);
        }

        return Response.ok(new LabelFields(labelFields)).cacheControl(NO_CACHE).build();
    }

    @GET
    @Path ("gadget/configuration/validate")
    @Produces (MediaType.APPLICATION_JSON)
    public Response validateLabelGadgetConfiguration()
    {
        // No point validating -- all fields are select drop downs and the config page won't be shown if fields (or their options) go missing
        return Response.ok().cacheControl(NO_CACHE).build();
    }

    @GET
    @Path ("gadget/{project}/{fieldId}/groups")
    @Produces (MediaType.TEXT_HTML)
    public Response getLabelGroups(@PathParam ("project") String project, @PathParam ("fieldId") String fieldId)
    {
        long projectId;
        try
        {
            projectId = Long.parseLong(StringUtils.substring(project, "project-".length()));
        }
        catch (NumberFormatException e)
        {
            log.error("Error parsing project id from '" + project + "'");
            return Response.status(Response.Status.BAD_REQUEST).entity("Error parsing project id from '" + escapeHtml(project) + "'").cacheControl(NO_CACHE).build();
        }

        return Response.ok(alphabeticalLabelRenderer.getHtml(authenticationContext.getLoggedInUser(), projectId, fieldId, true)).cacheControl(NO_CACHE).build();
    }

    @XmlRootElement
    public static class LabelField
    {
        @XmlElement
        private String label;

        @XmlElement
        private String value;

        private LabelField() { }

        public LabelField(final String label, final String value)
        {
            this.label = label;
            this.value = value;
        }
    }

    @XmlRootElement
    public static class LabelFields
    {
        @XmlElement
        private final List<LabelField> labelFields = new ArrayList<LabelField>();

        private LabelFields() {}

        public LabelFields(final List<LabelField> labelFields)
        {
            this.labelFields.addAll(labelFields);
        }
    }

}
