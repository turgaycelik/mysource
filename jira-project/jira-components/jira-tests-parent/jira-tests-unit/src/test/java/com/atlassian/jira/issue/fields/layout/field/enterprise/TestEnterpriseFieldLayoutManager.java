package com.atlassian.jira.issue.fields.layout.field.enterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.DefaultFieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@SuppressWarnings ({ "ReturnOfNull", "ClassTooDeepInInheritanceTree", "AnonymousInnerClassWithTooManyMethods", "OverlyComplexAnonymousInnerClass" })
@RunWith(MockitoJUnitRunner.class)
public class TestEnterpriseFieldLayoutManager
{
    private static final List<String> ALL_ISSUE_TYPE_IDS = asList("1", "2", "3", "4");

    @Mock private FieldManager fieldManager;
    @Mock private OfBizDelegator ofBizDelegator;
    @Mock private ConstantsManager constantsManager;
    @Mock private SubTaskManager subTaskManager;
    @Mock private EventPublisher eventPublisher;

    private CacheManager cacheManager = new MemoryCacheManager();

    @AvailableInContainer
    private I18nHelper.BeanFactory i18nFactory = new MockI18nHelper().factory();

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        cacheManager = new MemoryCacheManager();
    }

    @Test
    public void testGetUniqueFieldLayoutsNoAssociatedScheme() throws Exception
    {
        // Set up
        final AtomicBoolean getDefaultLayoutCalled = new AtomicBoolean(false);
        DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(), ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            public FieldLayout getFieldLayout()
            {
                getDefaultLayoutCalled.set(true);
                return null;
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final GenericValue project)
            {
                return null;
            }
        };

        // Invoke
        fieldLayoutManager.getUniqueFieldLayouts(new MockProject(12345L, "TST", "Test"));
        
        // Check
        assertTrue(getDefaultLayoutCalled.get());
    }

    @Test
    public void testGetUniqueFieldLayouts() throws Exception
    {
        // Set up
        when(constantsManager.getAllIssueTypeIds()).thenReturn(Collections.<String>emptyList());
        final FieldConfigurationScheme fieldConfigurationScheme = mock(FieldConfigurationScheme.class);
        final Set<Long> allFieldLayoutIds = ImmutableSet.of(1L, 2L, 3L);
        when(fieldConfigurationScheme.getAllFieldLayoutIds(Collections.<String>emptyList())).thenReturn(allFieldLayoutIds);

        final Set<Long> foundLayoutIds = new HashSet<Long>(4);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(),
                ofBizDelegator, constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final GenericValue project)
            {
                return fieldConfigurationScheme;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                foundLayoutIds.add(id);
                return null;
            }
        };

        // Invoke
        fieldLayoutManager.getUniqueFieldLayouts(new MockProject(12345L, "TST", "Test"));

        // Check
        assertEquals(allFieldLayoutIds, foundLayoutIds);
    }

    @Test
    public void testFieldConfigurationSchemeExists() throws Exception
    {
        // Set up
        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        ofBizDelegator.createValue("FieldLayoutScheme", new FieldMap("name", "Eno"));
        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(),
                ofBizDelegator, constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher);

        assertTrue(fieldLayoutManager.fieldConfigurationSchemeExists("Eno"));
        assertFalse(fieldLayoutManager.fieldConfigurationSchemeExists("Byrne"));
    }

    @Test
    public void testGetFieldConfigurationSchemes() throws Exception
    {
        // Set up
        OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        ofBizDelegator.createValue("FieldLayoutScheme", new FieldMap("id", 1L).add("name", "Scheme 1"));
        ofBizDelegator.createValue("FieldLayoutSchemeEntity", new FieldMap("id", 101L).add("scheme", 1L).add("fieldlayout", 11L));
        ofBizDelegator.createValue("FieldLayoutSchemeEntity", new FieldMap("id", 102L).add("scheme", 1L).add("fieldlayout", null));
        ofBizDelegator.createValue("FieldLayoutScheme", new FieldMap("id", 2L).add("name", "Scheme 2"));
        ofBizDelegator.createValue("FieldLayoutSchemeEntity", new FieldMap("id", 103L).add("scheme", 2L).add("fieldlayout", 11L));

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(),
                ofBizDelegator, constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher);

        // Test for the Custom FieldLayout ID = 11
        FieldLayout fieldLayout = makeFieldLayout(11);
        Set<FieldConfigurationScheme> expected = ImmutableSet.of(
                fieldLayoutManager.getFieldConfigurationScheme(1L), fieldLayoutManager.getFieldConfigurationScheme(2L));
        assertEquals(expected, fieldLayoutManager.getFieldConfigurationSchemes(fieldLayout));
        // Now test for the Default FieldLayout
        fieldLayout = makeDefaultFieldLayout(12);
        expected = singleton(fieldLayoutManager.getFieldConfigurationScheme(1L));
        assertEquals(expected, fieldLayoutManager.getFieldConfigurationSchemes(fieldLayout));
    }

    private static FieldLayout makeDefaultFieldLayout(final long id)
    {
        return new EditableDefaultFieldLayoutImpl(
                new MockGenericValue("FieldLayout", new FieldMap("id", id)), new ArrayList<FieldLayoutItem>(4));
    }

    private static FieldLayout makeFieldLayout(final long id)
    {
        return new EditableFieldLayoutImpl(
                new MockGenericValue("FieldLayout", new FieldMap("id", id)), new ArrayList<FieldLayoutItem>(4));
    }

    @Test
    public void testIsFieldLayoutsVisiblyEquivalentSameLayoutItems() throws Exception
    {
        // Set up
        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems()).thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems()).thenReturn(layoutItems1);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            protected FieldLayout getRelevantFieldLayout(final Long id)
            {
                if (id.equals(10L))
                {
                    return layout1;
                }
                if (id.equals(20L))
                {
                    return layout2;
                }
                return null;
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(10L, 20L));
        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(20L, 10L));
    }

    @Test
    public void testIsFieldLayoutsVisiblyEquivalentDifferentLayoutItemsEqual() throws Exception
    {
        // Set up
        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                );

        final List<FieldLayoutItem> layoutItems2 = asList(
                createLayoutItem("id2", false),
                createLayoutItem("id1", true)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems())
                .thenReturn(layoutItems2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            protected FieldLayout getRelevantFieldLayout(final Long id)
            {
                if (id.equals(10L))
                {
                    return layout1;
                }
                if (id.equals(20L))
                {
                    return layout2;
                }
                return null;
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(10L, 20L));
        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(20L, 10L));
    }

    @Test
    public void testIsFieldLayoutsVisiblyEquivalentDifferentLayoutItemsDifferent() throws Exception
    {
        // Set up
        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                );

        final List<FieldLayoutItem> layoutItems2 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", true)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems())
                .thenReturn(layoutItems2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            protected FieldLayout getRelevantFieldLayout(final Long id)
            {
                if (id.equals(10L))
                {
                    return layout1;
                }
                if (id.equals(20L))
                {
                    return layout2;
                }
                return null;
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(10L, 20L));
        assertFalse(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(20L, 10L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentBothDefault() throws Exception
    {
        // Set up
        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(null, null));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentSimple() throws Exception
    {
        // Set up
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity(null, 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11L, asList(e1));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12L, asList(e2));

        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentSimpleAndNullScheme() throws Exception
    {
        // Set up
        final FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11L, asList(e1));

        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id ==null)
                {
                    return layout2;
                }
                if (id == 1L)
                {
                    return layout1;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                throw new UnsupportedOperationException();
            }

        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, null));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(null, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentDefaultsSameAndAdditionalSame() throws Exception
    {
        // Set up
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("1", 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11L, asList(e1));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12L, asList(e2, e3));

        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentDefaultsSameButAdditionalDifferent() throws Exception
    {
        // Set up
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("1", 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, asList(e1));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, asList(e2, e3));

        final List<FieldLayoutItem> layoutItems1 = asList(
                createLayoutItem("id1", false),
                createLayoutItem("id2", false)
                );

        final List<FieldLayoutItem> layoutItems2 = asList(
                createLayoutItem("id1", true),
                createLayoutItem("id2", true)
                );

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems())
                .thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems())
                .thenReturn(layoutItems2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentTwoAdditionalsDifferent() throws Exception
    {
        // Set up
        final FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        final FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 2L);
        final FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity(null, 1L);
        final FieldLayoutSchemeEntity e4 = createFieldLayoutSchemeEntity("2", 3L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, asList(e1, e2));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, asList(e3, e4));

        final List<FieldLayoutItem> layoutItems1 = asList(createLayoutItem("id1", false), createLayoutItem("id2", false));
        final List<FieldLayoutItem> layoutItems2 = asList(createLayoutItem("id1", true), createLayoutItem("id2", true));
        final List<FieldLayoutItem> layoutItems3 = asList(createLayoutItem("id1", false), createLayoutItem("id2", true));

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems()).thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems()).thenReturn(layoutItems2);

        final FieldLayout layout3 = mock(FieldLayout.class);
        when(layout3.getFieldLayoutItems()).thenReturn(layoutItems3);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                if (id == 3L)
                {
                    return layout3;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentTwoAdditionalsEquivalentToDefault() throws Exception
    {
        // Set up
        final FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        final FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 1L);
        final FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity(null, 1L);
        final FieldLayoutSchemeEntity e4 = createFieldLayoutSchemeEntity("2", 1L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, asList(e1, e2));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, asList(e3, e4));

        final List<FieldLayoutItem> layoutItems1 = asList(createLayoutItem("id1", false), createLayoutItem("id2", false));

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems()).thenReturn(layoutItems1);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                return layout1;
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentExhaustiveSameButDifferentDefault() throws Exception
    {
        // Set up
        final FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        final FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 2L);
        final FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("2", 2L);
        final FieldLayoutSchemeEntity e4 = createFieldLayoutSchemeEntity(null, 2L);
        final FieldLayoutSchemeEntity e5 = createFieldLayoutSchemeEntity("1", 2L);
        final FieldLayoutSchemeEntity e6 = createFieldLayoutSchemeEntity("2", 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, asList(e1, e2, e3));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, asList(e4, e5, e6));

        final List<FieldLayoutItem> layoutItems1 = asList(createLayoutItem("id1", false), createLayoutItem("id2", false));
        final List<FieldLayoutItem> layoutItems2 = asList(createLayoutItem("id1", true), createLayoutItem("id2", true));

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems()).thenReturn(layoutItems1);

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems()).thenReturn(layoutItems2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return asList("1", "2");
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentNoDefault() throws Exception
    {
        // Set up
        final FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        final FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 1L);
        final FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("2", 1L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, asList(e1, e2));
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, asList(e3));

        final List<FieldLayoutItem> layoutItems1 = asList(createLayoutItem("id1", false), createLayoutItem("id2", false));

        final FieldLayout layout1 = mock(FieldLayout.class);
        when(layout1.getFieldLayoutItems()).thenReturn(layoutItems1);

        final List<FieldLayoutItem> layoutItems2 = asList(createLayoutItem("id1", false), createLayoutItem("id2", true));

        final FieldLayout layout2 = mock(FieldLayout.class);
        when(layout2.getFieldLayoutItems()).thenReturn(layoutItems2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator,
                constantsManager, subTaskManager, null, i18nFactory, null, cacheManager, eventPublisher)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return ALL_ISSUE_TYPE_IDS;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == null)
                {
                    return layout2;
                }
                if (id == 1L)
                {
                    return layout1;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));
    }

    private FieldLayoutSchemeEntity createFieldLayoutSchemeEntity(final String issueTypeId, final Long fieldLayoutId)
    {
        final FieldLayoutSchemeEntity entity = mock(FieldLayoutSchemeEntity.class, new Strict());
        doReturn(issueTypeId).when(entity).getIssueTypeId();
        doReturn(fieldLayoutId).when(entity).getFieldLayoutId();
        return entity;
    }

    private static FieldConfigurationScheme createFieldConfigurationScheme(final long id, final List<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities)
    {
        final Map<String, Long> issueTypeToFieldLayoutIdMap = new HashMap<String, Long>(4);
        for (FieldLayoutSchemeEntity entity : fieldLayoutSchemeEntities)
        {
            issueTypeToFieldLayoutIdMap.put(entity.getIssueTypeId(), entity.getFieldLayoutId());
        }

        final FieldConfigurationScheme scheme = mock(FieldConfigurationScheme.class);
        when(scheme.getId()).thenReturn(id);
        when(scheme.getFieldLayoutId(anyString())).thenAnswer(new Answer<Long>()
        {
            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable
            {
                final String issueTypeId = (String)invocation.getArguments()[0];
                final Long layoutId = issueTypeToFieldLayoutIdMap.get(issueTypeId);
                return (layoutId != null) ? layoutId : issueTypeToFieldLayoutIdMap.get(null);
            }
        });
        return scheme;
    }

    private static FieldLayoutItem createLayoutItem(final String fieldId, final boolean isHidden)
    {
        final OrderableField orderableField = mock(OrderableField.class);
        when(orderableField.getId()).thenReturn(fieldId);

        final FieldLayoutItem item = mock(FieldLayoutItem.class);
        when(item.getOrderableField()).thenReturn(orderableField);
        when(item.isHidden()).thenReturn(isHidden);
        return item;
    }
}
