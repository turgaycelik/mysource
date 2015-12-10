package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v4.0
 */
public class MockFieldScreenStore implements FieldScreenStore
{
    private Map<Long, FieldScreen> screensMap = new HashMap<Long, FieldScreen>();
    private List<FieldScreen> fieldScreens;
    private static final Comparator<FieldScreen> fieldScreenComparator = new FieldScreenComparator();

    public void setFieldScreenManager(final FieldScreenManager fieldScreenManager)
    {
    }

    public FieldScreen getFieldScreen(final Long id)
    {
        return screensMap.get(id);
    }

    @Override
    public List<Long> getFieldScreenIds()
    {
        List<Long> ids = new ArrayList<Long>();
        for (FieldScreen fieldScreen : screensMap.values())
        {
            ids.add(fieldScreen.getId());
        }
        return ids;
    }

    public List<FieldScreen> getFieldScreens()
    {
        if (fieldScreens == null)
        {
            List<FieldScreen> fieldScreens = new ArrayList<FieldScreen>(screensMap.values());
            Collections.sort(fieldScreens, fieldScreenComparator);

            return fieldScreens;
        }

        return fieldScreens;
    }

    public void setFieldScreens(List<FieldScreen> fieldScreens)
    {
        this.fieldScreens = fieldScreens;
    }

    public void createFieldScreen(final FieldScreen fieldScreen)
    {
        screensMap.put(fieldScreen.getId(), fieldScreen);
    }

    public void removeFieldScreen(final Long id)
    {
        screensMap.remove(id);
    }

    public void updateFieldScreen(final FieldScreen fieldScreen)
    {
        screensMap.put(fieldScreen.getId(), fieldScreen);
    }

    public void createFieldScreenTab(final FieldScreenTab fieldScreenTab)
    {
    }

    public void updateFieldScreenTab(final FieldScreenTab fieldScreenTab)
    {
    }

    public List<FieldScreenTab> getFieldScreenTabs(final FieldScreen fieldScreen)
    {
        return null;
    }

    public void updateFieldScreenLayoutItem(final FieldScreenLayoutItem fieldScreenLayoutItem)
    {
    }

    public void removeFieldScreenLayoutItem(final FieldScreenLayoutItem fieldScreenLayoutItem)
    {
    }

    public void removeFieldScreenLayoutItems(final FieldScreenTab fieldScreenTab)
    {
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(final FieldScreenTab fieldScreenTab)
    {
        return null;
    }

    public void refresh()
    {
    }

    public void createFieldScreenLayoutItem(final FieldScreenLayoutItem fieldScreenLayoutItem)
    {
    }

    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(final GenericValue genericValue)
    {
        return null;
    }

    public void removeFieldScreenTabs(final FieldScreen fieldScreen)
    {
    }

    public void removeFieldScreenTab(final Long id)
    {
    }

    public FieldScreenTab getFieldScreenTab(final Long fieldScreenTabId)
    {
        return null;
    }

    private static class FieldScreenComparator implements Comparator<FieldScreen>
    {
        public int compare(final FieldScreen fs1, final FieldScreen fs2)
        {
            return fs1.getName().compareTo(fs2.getName());
        }
    }
}
