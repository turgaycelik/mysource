package com.atlassian.jira.association;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since v4.3
 */
public class UserAssociationStoreImpl implements UserAssociationStore
{
    private final OfBizDelegator ofBizDelegator;
    private UserManager userManager;

    public UserAssociationStoreImpl(OfBizDelegator ofBizDelegator, UserManager userManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.userManager = userManager;
    }

    @Override
    public void createAssociation(final String associationType, final ApplicationUser user, final GenericValue sink)
    {
        createAssociation(associationType, user.getKey(), sink.getEntityName(), sink.getLong("id"));
    }

    @Override
    public void createAssociation(String associationType, ApplicationUser user, Issue sink)
    {
        createAssociation(associationType, user.getKey(), Entity.Name.ISSUE, sink.getId());
    }

    @Override
    public void createAssociation(final String associationType, final String userName, final String sinkNodeEntity, final Long sinkNodeId)
    {
        GenericValue association = getAssociation(userName, sinkNodeId, sinkNodeEntity, associationType);
        if (association == null)
        {
            final FieldMap fields = FieldMap.build("associationType", associationType)
                    .add("sourceName", userName).add("sinkNodeEntity", sinkNodeEntity).add("sinkNodeId", sinkNodeId)
                    .add("created", new Timestamp(System.currentTimeMillis()));
            // Can't use ofBizDelegator.createValue() because this will try to force an "id" field into the GV
            final GenericValue genericValue = ofBizDelegator.makeValue("UserAssociation");
            genericValue.setFields(fields);
            try
            {
                genericValue.create();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    @Override
    public void removeAssociation(String associationType, String userkey, String sinkNodeEntity, Long sinkNodeId)
    {
        final FieldMap fields = new FieldMap();
        fields.put("sinkNodeId", sinkNodeId);
        fields.put("sinkNodeEntity", sinkNodeEntity);
        fields.put("sourceName", userkey);
        fields.put("associationType", associationType);
        ofBizDelegator.removeByAnd("UserAssociation", fields);
    }

    @Override
    public void removeAssociation(String associationType, ApplicationUser user, Issue sink)
    {
        removeAssociation(associationType, user.getKey(), Entity.Name.ISSUE, sink.getId());
    }

    @Override
    public void removeUserAssociationsFromUser(final String associationType, final ApplicationUser user, final String sinkNodeEntity)
    {
        final FieldMap fields = new FieldMap();
        fields.put("sourceName", user.getKey());
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sinkNodeEntity);
        ofBizDelegator.removeByAnd("UserAssociation", fields);
    }

    @Override
    public void removeUserAssociationsFromSink(String sinkNodeEntity, Long sinkNodeId)
    {
        final FieldMap fields = new FieldMap();
        fields.put("sinkNodeEntity", sinkNodeEntity);
        fields.put("sinkNodeId", sinkNodeId);
        ofBizDelegator.removeByAnd("UserAssociation", fields);
    }

    @Override
    public boolean associationExists(final String associationType, final User user, final String sinkNodeEntity, final Long sinkNodeId)
    {
        return associationExists(associationType, ApplicationUsers.from(user), sinkNodeEntity, sinkNodeId);
    }

    @Override
    public boolean associationExists(String associationType, ApplicationUser user, String sinkNodeEntity, Long sinkNodeId)
    {
        return user != null && ofBizDelegator.findByAnd("UserAssociation", fieldMap(associationType, user.getKey(), sinkNodeEntity, sinkNodeId)).size() > 0;
    }

    @Override
    public List<ApplicationUser> getUsersFromSink(String associationType, GenericValue sink)
    {
        if (sink == null)
        {
            throw new IllegalArgumentException("Sink GenericValue can not be null.");
        }

        final FieldMap fields = new FieldMap();
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("sinkNodeId", sink.getLong("id"));

        final List<GenericValue> results = getAssociations(fields);

        final List<ApplicationUser> outList = new ArrayList<ApplicationUser>(results.size());
        for (final GenericValue result : results)
        {
            outList.add(userManager.getUserByKeyEvenWhenUnknown(result.getString("sourceName")));
        }
        return outList;
    }

    @Override
    public List<String> getUsernamesFromSink(String associationType, GenericValue sink)
    {
        List<ApplicationUser> users = getUsersFromSink(associationType, sink);
        List<String> usernames = new ArrayList<String>(users.size());
        for (ApplicationUser user : users)
        {
            usernames.add(user.getUsername());
        }
        return usernames;
    }

    @Override
    public Collection<String> getUserkeysFromSink(String associationType, String sinkNodeEntity, Long sinkNodeId)
    {
        if (sinkNodeEntity == null || sinkNodeId == null)
        {
            throw new IllegalArgumentException("Sink cannot be null.");
        }

        final FieldMap fields = new FieldMap();
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sinkNodeEntity);
        fields.put("sinkNodeId", sinkNodeId);

        final List<GenericValue> results = getAssociations(fields);

        final List<String> outList = new ArrayList<String>(results.size());
        for (final GenericValue result : results)
        {
            outList.add(result.getString("sourceName"));
        }
        return outList;
    }

    @Override
    public Collection<String> getUserkeysFromIssue(String associationType, Long issueId)
    {
        return getUserkeysFromSink(associationType, Entity.Name.ISSUE, issueId);
    }

    @Override
    public List<GenericValue> getSinksFromUser(String associationType, ApplicationUser user, String sinkNodeEntity)
    {
        final List<GenericValue> associations = getAssociationsForUser(associationType, user, sinkNodeEntity);

        final List<GenericValue> sinks = new ArrayList<GenericValue>(associations.size());
        for (final GenericValue association : associations)
        {
            GenericValue sink = ofBizDelegator.findByPrimaryKey(sinkNodeEntity, association.getLong("sinkNodeId"));

            if (sink != null)
            {
                sinks.add(sink);
            }
        }
        return sinks;
    }

    private List<GenericValue> getAssociationsForUser(final String associationType, final ApplicationUser user, final String sinkNodeEntity)
    {
        Assertions.notNull("user", user);
        final FieldMap fields = new FieldMap();
        fields.put("associationType", associationType);
        fields.put("sourceName", user.getKey());
        fields.put("sinkNodeEntity", sinkNodeEntity);
        return getAssociations(fields);
    }

    private List<GenericValue> getAssociations(Map<String, ?> fields)
    {

        return ofBizDelegator.findByAnd("UserAssociation", fields);
    }

    private Map<String, ?> fieldMap(final String associationType, final String userName, final String sinkNodeEntity, final Long sinkNodeId)
    {
        return FieldMap.build("associationType", associationType)
                .add("sourceName", userName).add("sinkNodeEntity", sinkNodeEntity).add("sinkNodeId", sinkNodeId);
    }

//    private UserAssociation createUserAssociation(final GenericValue genericValue)
//    {
//        if (genericValue == null)
//        {
//            return null;
//        }
//        return new ImmutableUserAssociation(genericValue);
//    }


    private GenericValue getAssociation(String userName, Long sinkNodeId, String sinkNodeEntity, String associationType)
    {
        final FieldMap fields = FieldMap.build("associationType", associationType)
                .add("sourceName", userName).add("sinkNodeEntity", sinkNodeEntity).add("sinkNodeId", sinkNodeId);
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("UserAssociation", fields));
    }
}
