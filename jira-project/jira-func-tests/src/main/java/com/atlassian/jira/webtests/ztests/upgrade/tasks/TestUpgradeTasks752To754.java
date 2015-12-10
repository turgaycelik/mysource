package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.atlassian.jira.testkit.client.DashboardControl.Dashboard;
import static com.atlassian.jira.testkit.client.SearchRequestControl.SearchBean;

/**
 * Uprgade Tasks 752, 753 and 754 actually work together to help fix JRA-26441. In 5.0.x
 * these upgrade tasks where called 727, 728 and 729.
 *
 * @since v5.0.3
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTasks752To754 extends FuncTestCase
{
    public void testUpgrade()
    {
        administration.restoreDataWithBuildNumber("TestUpgradeTasks752To754.xml", 721);
        checkDashboardsUpgradedCorrectly();
        checkSearchRequestsUpgradedCorrectly();
    }

    //TestUser used to own {a,b,c,d,e,f}. We need to ensure they are owned by testuser.
    private void checkDashboardsUpgradedCorrectly()
    {
        assertSharedEntitiesEqual(createdSharedBeans(), getServerDashboards());
    }

    //TestUser used to own {a,b,c,d,e,f}. We need to ensure they are owned by testuser.
    private void checkSearchRequestsUpgradedCorrectly()
    {
        assertSharedEntitiesEqual(createdSharedBeans(), getServerFilters());
    }

    private static List<SharedEntity> createdSharedBeans()
    {
        List<SharedEntity> searchBeans = new ArrayList<SharedEntity>();
        searchBeans.add(new SharedEntity("b", "testuser", 1L, true));
        searchBeans.add(new SharedEntity("bb", "testuser", 1L, true));
        searchBeans.add(new SharedEntity("c", "testuser", 1L, true));
        searchBeans.add(new SharedEntity("cc", "testuser", 1L, true));
        searchBeans.add(new SharedEntity("d", "testuser", 0, false));
        searchBeans.add(new SharedEntity("dd", "testuser", 0, false));
        searchBeans.add(new SharedEntity("e", "testuser", 1, true));
        searchBeans.add(new SharedEntity("ee", "testuser", 1, true));
        searchBeans.add(new SharedEntity("f", "testuser", 0, false));
        searchBeans.add(new SharedEntity("ff", "testuser", 0, false));
        return searchBeans;
    }

    private ImmutableList<SharedEntity> getServerDashboards()
    {
        return ImmutableList.copyOf(Iterables.transform(backdoor.dashboard().getOwnedDashboard("testuser"), new Function<Dashboard, SharedEntity>()
        {
            @Override
            public SharedEntity apply(Dashboard input)
            {
                return new SharedEntity(input.getName(), input.getOwner(), input.getFavouriteCount(), input.isFavourite());
            }
        }));
    }

    private ImmutableList<SharedEntity> getServerFilters()
    {
        return ImmutableList.copyOf(Iterables.transform(backdoor.searchRequests().getOwnedFilters("testuser"), new Function<SearchBean, SharedEntity>()
        {
            @Override
            public SharedEntity apply(SearchBean input)
            {
                return new SharedEntity(input.getSearchName(), input.getUsername(), input.getFavouriteCount(), input.isFavourite());
            }
        }));
    }

    private static void assertSharedEntitiesEqual(List<? extends SharedEntity> expected, List<? extends SharedEntity> actual)
    {
        int pos = 0;
        ListIterator<? extends SharedEntity> actualIter = actual.listIterator();
        ListIterator<? extends SharedEntity> expectedIter = expected.listIterator();
        while (actualIter.hasNext() && expectedIter.hasNext())
        {
            final SharedEntity actualEntity = actualIter.next();
            final SharedEntity expectedEntity = expectedIter.next();
            
            if (!StringUtils.equals(actualEntity.getName(), expectedEntity.getName()))
            {
                fail(String.format("[%d].name != [%1$d].name (%s != %s).", pos, expectedEntity.getName(), actualEntity.getName()));
            }

            if (actualEntity.isFavourite() != expectedEntity.isFavourite())
            {
                fail(String.format("[%d].favourite != [%1$d].favourite (%s != %s).", pos, expectedEntity.isFavourite(), actualEntity.isFavourite()));
            }

            if (actualEntity.getFavouriteCount() != expectedEntity.getFavouriteCount())
            {
                fail(String.format("[%d].favouriteCount != [%1$d].favouriteCount (%s != %s).", pos, expectedEntity.getFavouriteCount(), actualEntity.getFavouriteCount()));
            }

            if (!StringUtils.equals(actualEntity.getOwner(), expectedEntity.getOwner()))
            {
                fail(String.format("[%d].owner != [%1$d].owner (%s != %s).", pos, expectedEntity.getOwner(), actualEntity.getOwner()));
            }
            pos++;
        }
        
        if (actualIter.hasNext())
        {
            fail(String.format("Got extra dashboards: [%s]", actual.subList(pos, actual.size())));
        }
        else if (expectedIter.hasNext())
        {
            fail(String.format("Didn't get dashboards: [%s]", expected.subList(pos, expected.size())));
        }
    }
    
    private static class SharedEntity
    {
        private final String name;
        private final String owner;
        private final long favouriteCount;
        private final boolean favourite;

        private SharedEntity(String name, String owner, long favouriteCount, boolean favourite)
        {
            this.name = name;
            this.owner = owner;
            this.favouriteCount = favouriteCount;
            this.favourite = favourite;
        }

        public String getName()
        {
            return name;
        }

        public String getOwner()
        {
            return owner;
        }

        public long getFavouriteCount()
        {
            return favouriteCount;
        }

        public boolean isFavourite()
        {
            return favourite;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SharedEntity that = (SharedEntity) o;

            if (favourite != that.favourite) { return false; }
            if (favouriteCount != that.favouriteCount) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (owner != null ? !owner.equals(that.owner) : that.owner != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            result = 31 * result + (int) (favouriteCount ^ (favouriteCount >>> 32));
            result = 31 * result + (favourite ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
