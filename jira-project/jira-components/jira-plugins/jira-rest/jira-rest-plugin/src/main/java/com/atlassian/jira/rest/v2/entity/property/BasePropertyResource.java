package com.atlassian.jira.rest.v2.entity.property;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManagerImpl;
import com.atlassian.jira.issue.fields.rest.json.beans.*;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.util.RestStringUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.io.LimitInputStream;

import org.apache.commons.io.IOUtils;

import static com.atlassian.jira.entity.property.EntityPropertyService.DeletePropertyValidationResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyKeys;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.status;

/**
 *
 * @since v6.2
 */
public class BasePropertyResource<E extends WithId>
{
    private final EntityPropertyService<E> entityPropertyService;
    private final JiraAuthenticationContext authContext;
    private final JiraBaseUrls jiraBaseUrls;
    private final I18nHelper i18n;
    private final EntityPropertyType entityPropertyType;
    private final Function2<Long, String, String> entityIdAndPropertyKeyToSelfFunction;

    public BasePropertyResource(EntityPropertyService<E> entityPropertyService, JiraAuthenticationContext authContext,
            JiraBaseUrls jiraBaseUrls, I18nHelper i18n, Function2<Long, String, String> entityIdAndPropertyKeyToSelfFunction,
            EntityPropertyType entityPropertyType)
    {
        this.entityPropertyService = entityPropertyService;
        this.authContext = authContext;
        this.jiraBaseUrls = jiraBaseUrls;
        this.i18n = i18n;
        this.entityPropertyType = checkNotNull(entityPropertyType);
        this.entityIdAndPropertyKeyToSelfFunction = checkNotNull(entityIdAndPropertyKeyToSelfFunction);
    }

    /**
     * Returns the keys of all properties for the entity identified by the id.
     *
     * @param id the entity from which keys will be returned.
     * @return a response containing EntityPropertiesKeysBean.
     */
    public Response getPropertiesKeys(final String id)
    {
        final ApplicationUser user = authContext.getUser();
        PropertyKeys<E> propertyKeys = getPropertiesKeys(user, id);
        if (propertyKeys.isValid())
        {
            EntityPropertiesKeysBean entity = EntityPropertiesKeysBean.build(jiraBaseUrls,
                    propertyKeys.getEntity().getId(), propertyKeys.getKeys(), entityIdAndPropertyKeyToSelfFunction);
            return status(Response.Status.OK).entity(entity)
                    .cacheControl(never())
                    .build();
        }
        else
        {
            return error(propertyKeys.getErrorCollection());
        }
    }

    /**
     * Sets the value of the specified entity's property.
     * <p>
     *     This method can used to store a custom data against the entity identified by the key or by the id. The user
     *     who stores the data is required to have permissions to edit the entity.
     * </p>
     *
     * @param id the entity's id on which the property will be set.
     * @param propertyKey the key of the entity's property. The maximum length of the key is 255 bytes.
     * @param request the request containing value of the entity's property. The value has to a valid, non-empty JSON conforming
     *  to http://tools.ietf.org/html/rfc4627. The maximum length of the property value is 32768 bytes.
     */
    public Response setProperty(final String id, final String propertyKey, final HttpServletRequest request)
    {
        final ApplicationUser user = authContext.getUser();

        SetPropertyValidationResult setValidationResult =
                validateSetProperty(user, new PropertyInput(propertyValue(request), propertyKey), id);

        if (setValidationResult.isValid())
        {
            PropertyResult property = getProperty(user, propertyKey, id);
            PropertyResult propertyResult = entityPropertyService.setProperty(user, setValidationResult);
            if (!propertyResult.isValid())
            {
                return error(propertyResult.getErrorCollection());
            }
            return property.getEntityProperty().fold(
                    new Supplier<Response>()
                    {
                        @Override
                        public Response get()
                        {
                            return status(Response.Status.CREATED).cacheControl(never()).build();
                        }
                    },
                    new Function<EntityProperty, Response>()
                    {
                        @Override
                        public Response apply(final EntityProperty entityProperty)
                        {
                            return status(Response.Status.OK).cacheControl(never()).build();
                        }
                    }
            );
        }
        else
        {
            return error(setValidationResult.getErrorCollection());
        }
    }

    /**
     * Returns the value of the property with a given key from the entity identified by the the id. The user who retrieves
     * the property is required to have permissions to read the entity.
     *
     * @param entityId the id of the entity from which the property will be returned.
     * @param propertyKey the key of the property to returned.
     * @return a response containing {@link com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean}.
     */
    public Response getProperty(final String entityId, final String propertyKey)
    {
        final ApplicationUser user = authContext.getUser();

        PropertyResult propertyResult = getProperty(user, propertyKey, entityId);

        if (propertyResult.isValid())
        {
            Option<EntityProperty> property = propertyResult.getEntityProperty();

            return property.fold(new Supplier<Response>()
                                 {
                                     @Override
                                     public Response get()
                                     {
                                         final com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
                                         errorCollection.addErrorMessage(i18n.getText("jira.properties.service.property.does.not.exist", propertyKey), com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND);
                                         return error(errorCollection);
                                     }
                                 }, new Function<EntityProperty, Response>()
                                 {
                                     @Override
                                     public Response apply(final EntityProperty entityProperty)
                                     {
                                         com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean entityPropertyBean = com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean.builder(jiraBaseUrls, entityIdAndPropertyKeyToSelfFunction)
                                                 // escape unicode, because for some characters(i.e. pile of poo) jackson may throw exception
                                                 // during processing of JsonRawValue, which entity property value is in this case
                                                 .value(RestStringUtils.escapeUnicode(entityProperty.getValue()))
                                                 .key(entityProperty.getKey())
                                                 .build(entityProperty.getEntityId());
                                         return status(Response.Status.OK).entity(entityPropertyBean).cacheControl(never()).build();
                                     }
                                 }
            );
        }
        else
        {
            return error(propertyResult.getErrorCollection());
        }
    }

    public Response deleteProperty(final String id, String propertyKey)
    {
        final ApplicationUser user = authContext.getUser();

        DeletePropertyValidationResult deleteValidationResult = validateDeleteProperty(user, propertyKey, id);

        if (deleteValidationResult.isValid())
        {
            entityPropertyService.deleteProperty(user, deleteValidationResult);
            return status(Response.Status.NO_CONTENT).cacheControl(never()).build();
        }
        else
        {
            return error(deleteValidationResult.getErrorCollection());
        }
    }

    protected PropertyKeys<E> getPropertiesKeys(final ApplicationUser user, final String id)
    {
        return withIdValidation(id, new Function<Long, PropertyKeys<E>>()
        {
            @Override
            public PropertyKeys<E> apply(final Long entityId)
            {
                return entityPropertyService.getPropertiesKeys(user, entityId);
            }
        });
    }

    protected PropertyResult getProperty(final ApplicationUser user, final String propertyKey, final String id)
    {
        return withIdValidation(id, new Function<Long, PropertyResult>()
        {
            @Override
            public PropertyResult apply(final Long entityId)
            {
                return entityPropertyService.getProperty(user, entityId, propertyKey);
            }
        });
    }

    protected DeletePropertyValidationResult validateDeleteProperty(final ApplicationUser user, final String propertyKey, final String id)
    {
        return withIdValidation(id, new Function<Long, DeletePropertyValidationResult>()
        {
            @Override
            public DeletePropertyValidationResult apply(final Long id)
            {
                return entityPropertyService.validateDeleteProperty(user, id, propertyKey);
            }
        });
    }

    protected SetPropertyValidationResult validateSetProperty(final ApplicationUser user, final PropertyInput propertyInput,
            final String id)
    {
        return withIdValidation(id, new Function<Long, SetPropertyValidationResult>()
        {
            @Override
            public SetPropertyValidationResult apply(final Long entityId)
            {
                return entityPropertyService.validateSetProperty(user, entityId, propertyInput);
            }
        });
    }

    protected <T> T withIdValidation(final String id, final Function<Long, T> idFunction)
    {
        return idFunction.apply(getLongOrBadRequest(id));
    }

    private Response error(final com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        com.atlassian.jira.util.ErrorCollection.Reason reason = com.atlassian.jira.util.ErrorCollection.Reason.getWorstReason(errorCollection.getReasons());
        return status(reason.getHttpStatusCode()).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
    }

    private Long getLongOrBadRequest(final String idOrKey)
    {
        try
        {
            return Long.parseLong(idOrKey);
        }
        catch (NumberFormatException e)
        {
            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage(i18n.getText("jira.properties.service.property.invalid.entity", i18n.getText(entityPropertyType.getI18nKeyForEntityName())));
            throw new BadRequestWebException(ErrorCollection.of(errorCollection));
        }
    }

    private String propertyValue(final HttpServletRequest request)
    {
        try
        {
            LimitInputStream limitInputStream =
                    new LimitInputStream(request.getInputStream(), JsonEntityPropertyManagerImpl.MAXIMUM_VALUE_LENGTH + 1);
            byte[] bytes = IOUtils.toByteArray(limitInputStream);
            if (bytes.length > JsonEntityPropertyManagerImpl.MAXIMUM_VALUE_LENGTH)
            {
                throw new BadRequestWebException(
                        ErrorCollection.of(i18n.getText("jira.properties.service.length.unknown", JsonEntityPropertyManagerImpl.MAXIMUM_VALUE_LENGTH)));
            }
            return new String(bytes, Charset.forName(ComponentAccessor.getApplicationProperties().getEncoding()));
        }
        catch (IOException e)
        {
            throw new BadRequestWebException(ErrorCollection.of(e.getMessage()));
        }
    }

}
