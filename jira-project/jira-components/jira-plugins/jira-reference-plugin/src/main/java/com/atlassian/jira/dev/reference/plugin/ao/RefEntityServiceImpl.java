package com.atlassian.jira.dev.reference.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.collect.Lists;

import java.util.List;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Lists.newArrayList;


public class RefEntityServiceImpl implements RefEntityService
{
    private final ActiveObjects ao;

    public RefEntityServiceImpl(ActiveObjects ao)
    {
        this.ao = Assertions.notNull(ao);
    }

    @Override
    public RefEntity add(String description)
    {
        final RefEntity entity = ao.create(RefEntity.class);
        entity.setDescription(notNull("Description should not be null", description));
        entity.save();
        return entity;
    }

    @Override
    public List<RefEntity> allEntities()
    {
        return Lists.newArrayList(ao.find(RefEntity.class));
    }
}
