package com.atlassian.jira.sharing.type;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.user.ApplicationUser;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the {@link DefaultShareTypeFactory}.
 *
 * @since v3.13
 */

public class TestDefaultShareTypeFactory
{
    private static final Name BADTYPE = new Name("badtype");
    private static final SharePermission BAD_PERMISSION = new SharePermissionImpl(TestDefaultShareTypeFactory.BADTYPE, null, null);

    private ShareType type1;
    private ShareType type2;
    private ShareType type3;

    private MockControl type1ComparatorControl;
    private MockControl type2ComparatorControl;
    private MockControl type3ComparatorControl;

    private Comparator<SharePermission> type1Comparator;
    private Comparator<SharePermission> type2Comparator;
    private Comparator<SharePermission> type3Comparator;
    private DefaultShareTypeFactory factory;

    private SharePermission permission1;
    private SharePermission permission2;
    private SharePermission permission3;

    @Before
    public void setUp() throws Exception
    {
        type1ComparatorControl = MockControl.createControl(Comparator.class);
        type1Comparator = (Comparator<SharePermission>) type1ComparatorControl.getMock();
        type1 = new AbstractShareType(new Name("type1"), false, 3, createNopShareTypeRenderer(), createNopValidator(), createNopPermissionChecker(),
            createNopShareTypeQueryBuilder(), type1Comparator);

        type2ComparatorControl = MockControl.createControl(Comparator.class);
        type2Comparator = (Comparator<SharePermission>) type2ComparatorControl.getMock();
        type2 = new AbstractShareType(new Name("type2"), false, 1, createNopShareTypeRenderer(), createNopValidator(), createNopPermissionChecker(),
            createNopShareTypeQueryBuilder(), type2Comparator);

        type3ComparatorControl = MockControl.createControl(Comparator.class);
        type3Comparator = (Comparator<SharePermission>) type3ComparatorControl.getMock();
        type3 = new AbstractShareType(new Name("type3"), false, 7, createNopShareTypeRenderer(), createNopValidator(), createNopPermissionChecker(),
            createNopShareTypeQueryBuilder(), type3Comparator);
        factory = new DefaultShareTypeFactory(new HashSet(EasyList.build(type1, type2, type3)));

        permission1 = new SharePermissionImpl(type1.getType(), "ahh", null);
        permission2 = new SharePermissionImpl(type1.getType(), "ahh", null);
        permission3 = new SharePermissionImpl(type2.getType(), "ahh", null);
    }

    private ShareQueryFactory createNopShareTypeQueryBuilder()
    {
        return new ShareQueryFactory()
        {
            public Query getQuery(final ShareTypeSearchParameter searchParameter, final ApplicationUser user)
            {
                return null;
            }

            @Override
            public Query getQuery(ShareTypeSearchParameter parameter, User user)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            public Query getQuery(final ShareTypeSearchParameter searchParameter)
            {
                return null;
            }

            public Term[] getTerms(final ApplicationUser user)
            {
                return new Term[0];
            }

            @Override
            public Term[] getTerms(User user)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            public Field getField(final SharedEntity entity, final SharePermission permission)
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void testGetShareType()
    {
        initialiseMocks();

        assertSame(type1, factory.getShareType(type1.getType()));
        assertSame(type2, factory.getShareType(type2.getType()));
        assertSame(type3, factory.getShareType(type3.getType()));

        validateMocks();

        assertNull(factory.getShareType(TestDefaultShareTypeFactory.BADTYPE));
    }

    @Test
    public void testGetAllShareTypes()
    {
        initialiseMocks();

        assertEquals(EasyList.build(type2, type1, type3), factory.getAllShareTypes());

        validateMocks();
    }

    @Test
    public void testComparatorSamePermission()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertEquals(0, comparator.compare(permission1, permission1));

        validateMocks();
    }

    @After
    public void tearDown() throws Exception
    {
        type1 = null;
        type2 = null;
        type3 = null;

        type1Comparator = null;
        type2Comparator = null;
        type3Comparator = null;

        type1ComparatorControl = null;
        type2ComparatorControl = null;
        type3ComparatorControl = null;

        permission1 = null;
        permission2 = null;
        permission3 = null;

        factory = null;
    }

    @Test
    public void testComparatorEqualsPermission()
    {
        type1Comparator.compare(permission1, permission2);
        type1ComparatorControl.setReturnValue(0);

        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertEquals(0, comparator.compare(permission1, permission2));

        validateMocks();
    }

    @Test
    public void testComparatorNotEqualsPermission()
    {
        type1Comparator.compare(permission1, permission2);
        type1ComparatorControl.setReturnValue(Integer.MAX_VALUE);

        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertEquals(Integer.MAX_VALUE, comparator.compare(permission1, permission2));

        validateMocks();
    }

    @Test
    public void testComparatorLeftTypeMorePermission()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(permission1, permission3) > 0);

        validateMocks();
    }

    @Test
    public void testComparatorLeftTypeLessPermission()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(permission3, permission1) < 0);

        validateMocks();
    }

    @Test
    public void testComparatorLeftNull()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(null, permission2) < 0);

        validateMocks();
    }

    @Test
    public void testComparatorRightNull()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(permission1, null) > 0);

        validateMocks();
    }

    @Test
    public void testComparatorBothNull()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(null, null) == 0);

        validateMocks();
    }

    @Test
    public void testComparatorBadLeft()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(TestDefaultShareTypeFactory.BAD_PERMISSION, permission1) < 0);

        validateMocks();
    }

    @Test
    public void testComparatorBadRight()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(permission1, TestDefaultShareTypeFactory.BAD_PERMISSION) > 0);

        validateMocks();
    }

    @Test
    public void testComparatorBoth()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        assertTrue(comparator.compare(TestDefaultShareTypeFactory.BAD_PERMISSION, TestDefaultShareTypeFactory.BAD_PERMISSION) == 0);

        validateMocks();
    }

    @Test
    public void testComparatorBadType()
    {
        initialiseMocks();

        final Comparator comparator = factory.getPermissionComparator();
        try
        {
            comparator.compare("aa", "bb");
            fail("Should not accept invalid types.");
        }
        catch (final ClassCastException e)
        {
            // expected.
        }

        validateMocks();
    }

    private static ShareTypePermissionChecker createNopPermissionChecker()
    {
        return new ShareTypePermissionChecker()
        {
            public boolean hasPermission(final User user, final SharePermission permission)
            {
                return false;
            }
        };
    }

    private static ShareTypeValidator createNopValidator()
    {
        return new ShareTypeValidator()
        {
            public boolean checkSharePermission(final JiraServiceContext ctx, final SharePermission permission)
            {
                return false;
            }

            public boolean checkSearchParameter(final JiraServiceContext ctx, final ShareTypeSearchParameter searchParameter)
            {
                return false;
            }
        };
    }

    private static ShareTypeRenderer createNopShareTypeRenderer()
    {
        return new ShareTypeRenderer()
        {
            public String renderPermission(final SharePermission permission, final JiraAuthenticationContext userCtx)
            {
                return null;
            }

            public String getSimpleDescription(final SharePermission permission, final JiraAuthenticationContext userCtx)
            {
                return null;
            }

            public String getShareTypeEditor(final JiraAuthenticationContext userCtx)
            {
                return null;
            }

            public boolean isAddButtonNeeded(final JiraAuthenticationContext userCtx)
            {
                return false;
            }

            public String getShareTypeLabel(final JiraAuthenticationContext userCtx)
            {
                return null;
            }

            public Map /*<String, String>*/getTranslatedTemplates(final JiraAuthenticationContext userCtx, final TypeDescriptor type, final RenderMode mode)
            {
                return null;
            }
        };
    }

    private void initialiseMocks()
    {
        type1ComparatorControl.replay();
        type2ComparatorControl.replay();
        type3ComparatorControl.replay();
    }

    private void validateMocks()
    {
        type1ComparatorControl.verify();
        type2ComparatorControl.verify();
        type3ComparatorControl.verify();
    }
}
