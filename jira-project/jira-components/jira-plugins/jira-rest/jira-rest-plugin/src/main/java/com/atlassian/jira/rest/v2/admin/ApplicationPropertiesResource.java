package com.atlassian.jira.rest.v2.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.validation.Validated;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

@Path ("application-properties")
@Produces ( { MediaType.APPLICATION_JSON })
@WebSudoRequired
public class ApplicationPropertiesResource
{
    private static final Logger log = Logger.getLogger(ApplicationPropertiesResource.class);

    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ApplicationPropertiesService applicationPropertiesService;

    /**
       * A version bean instance used for auto-generated documentation.
       */
      static final Property DOC_EXAMPLE;
      static
      {
          Property property = new Property();
          property.id = "jira.home";
          property.key = "jira.home";
          property.value = "/var/jira/jira-home";
          property.name = "jira.home";
          property.desc = "JIRA home directory";
          property.type = "string";
          property.defaultValue = "";

          DOC_EXAMPLE = property;
      }

    public ApplicationPropertiesResource(
            final JiraAuthenticationContext authenticationContext,
            final PermissionManager permissionManager,
            final ApplicationPropertiesService applicationPropertiesService)
    {
        this.authenticationContext = Assertions.notNull("authenticationContext", authenticationContext);
        this.permissionManager = Assertions.notNull("permissionManager", permissionManager);
        this.applicationPropertiesService = Assertions.notNull("applicationPropertiesService", applicationPropertiesService);
    }

    /**
     * Returns an application property.
     *
     * @param key a String containing the property key
     * @param permissionLevel when fetching a list specifies the permission level of all items in the list
     *      see {@link com.atlassian.jira.bc.admin.ApplicationPropertiesService.EditPermissionLevel}
     * @param keyFilter when fetching a list allows the list to be filtered by the property's start of key
     *      e.g. "jira.lf.*" whould fetch only those permissions that are editable and whose keys start with
     *      "jira.lf.". This is a regex.
     * @return an application property.
     *
     * @response.representation.200.qname
     *      property
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the property exists and the currently authenticated user has permission to view it. Contains a
     *      full representation of the property.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.admin.ApplicationPropertiesResource#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the property does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    public Response getProperty(@QueryParam ("key") String key,@QueryParam ("permissionLevel") String permissionLevel,@QueryParam("keyFilter") String keyFilter)
    {
        if (key != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Got request for property value with key " + key);
            }
            ApplicationProperty property = applicationPropertiesService.getApplicationProperty(key);
            if (hasPermissionToEdit(property))
            {
                return Response.ok(new Property(property, authenticationContext.getI18nHelper())).cacheControl(NO_CACHE).build();
            }
            else
            {
                log.debug("No permission to get property: "+key);
                return noPermissionResponse();
            }
        }
        else if (applicationPropertiesService.hasPermissionForLevel(permissionLevel))
        {
            log.debug("Got request for all editable property values");
            List<ApplicationProperty> editableApplicationProperties = applicationPropertiesService.getEditableApplicationProperties(permissionLevel,keyFilter);
            List<Property> props = new ArrayList<Property>();
            for (ApplicationProperty editableApplicationProperty : editableApplicationProperties)
            {
                props.add(new Property(editableApplicationProperty, authenticationContext.getI18nHelper()));
            }
            return Response.ok(props).cacheControl(NO_CACHE).build();
        }
        else
        {
            log.debug("No permission to get properties.");
            return noPermissionResponse();
        }
    }

    /**
     * Modify an application property via PUT. The "value" field present in the PUT will override thee existing value.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.admin.ApplicationPropertyBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to edit it.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @PUT
    @Path("/{id}")
    public Response setPropertyViaRestfulTable(@PathParam ("id") final String key, final ApplicationPropertyBean applicationPropertyBean)
    {
        final String value = applicationPropertyBean.getValue();

        return setProperty(key, value);
    }

    private boolean hasPermissionToEdit(ApplicationProperty applicationProperty)
    {
       if (applicationProperty.getMetadata().isAdminEditable())
       {
           return (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser())
                   || permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser()));
       }
       else if (applicationProperty.getMetadata().isSysadminEditable())
       {
           return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser());
       }
       else
       {
           return false;
       }
    }

    public Response setProperty(final String key, final String value)
    {
        if (key != null)
        {
            ApplicationProperty property = applicationPropertiesService.getApplicationProperty(key);
            if (!hasPermissionToEdit(property))
            {
                log.debug("No permission to set a property: " + key);
                return noPermissionResponse();
            }

            try
            {
                Validated<ApplicationProperty> validatedApplicationProperty = applicationPropertiesService.setApplicationProperty(key, value);
                if (validatedApplicationProperty.getResult().isValid())
                {
                    return Response.ok(new Property(validatedApplicationProperty.getValue(), authenticationContext.getI18nHelper())).
                            cacheControl(NO_CACHE).build();
                }
                else
                {
                    final SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
                    simpleErrorCollection.addError("value",
                            validatedApplicationProperty.getResult().getErrorMessage());

                    return Response.status(Response.Status.BAD_REQUEST).
                            entity(ErrorCollection.of(simpleErrorCollection)).
                            cacheControl(NO_CACHE).build();
                }

            }
            catch (Exception e)
            {
                log.info("Error setting Application Property", e);
                return Response.serverError().cacheControl(NO_CACHE).build();
            }
        }
        else
        {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(ErrorCollection.of("No property key passed with the request!")).
                    cacheControl(NO_CACHE).build();
        }
    }

    private Response noPermissionResponse()
    {
        return Response.status(Response.Status.FORBIDDEN).entity(ErrorCollection.of("No permission")).cacheControl(NO_CACHE).build();
    }

    @XmlRootElement
    public static class Property
    {
        @XmlElement
        private String id;
        @XmlElement
        private String key;
        @XmlElement
        private String value;
        @XmlElement
        private String name;
        @XmlElement
        private String desc;
        @XmlElement
        private String type;
        @XmlElement
        private String defaultValue;
        @XmlElement
        private String example;

        @XmlElement
        private Collection<String> allowedValues;

        private Property()
        {
        }

        public Property(ApplicationProperty applicationProperty)
        {
            this(applicationProperty, null);
        }
        
        public Property(ApplicationProperty applicationProperty, I18nHelper i18n)
        {
            ApplicationPropertyMetadata metadata = applicationProperty.getMetadata();
            this.id = metadata.getKey();
            this.key = metadata.getKey();
            this.value = applicationProperty.getCurrentValue();
            this.name = getName(metadata, i18n);
            this.desc = getDescription(metadata, i18n);
            this.type = metadata.getType();
            if (!metadata.getDefaultValue().equals(value))
            {
                // save transport of values that are default by leaving it unspecified in that case
                this.defaultValue = metadata.getDefaultValue();
            }
            if(metadata.getType().equals("enum"))
            {
                this.allowedValues = metadata.getEnumerator().getEnumeration();
            }
            if(metadata.getExampleGenerator() != null)
            {
                this.example = metadata.getExampleGenerator().generate(this.value);
            }
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return "Property{"
                    + "key='" + key + '\''
                    + ", value='" + value + '\'' +
                    ", name='" + name + '\''
                    + ", desc='" + desc + '\''
                    + ", type='" + type + '\''
                    + ", defaultValue='" + defaultValue + '\''
                    + '}';
        }

        private static String getName(ApplicationPropertyMetadata metadata, I18nHelper i18n)
        {
            // Use the i18n key if we have one
            if (i18n != null && !StringUtils.isBlank(metadata.getNameKey()))
            {
                return i18n.getText(metadata.getNameKey());
            }

            return metadata.getName();
        }

        private static String getDescription(ApplicationPropertyMetadata metadata, I18nHelper i18n)
        {
            // Use the i18n key if we have one
            if (i18n != null && !StringUtils.isBlank(metadata.getDescriptionKey()))
            {
                return i18n.getText(metadata.getDescriptionKey());
            }

            return metadata.getDescription();
        }

        public String getExample()
        {
            return example;
        }
    }
}
