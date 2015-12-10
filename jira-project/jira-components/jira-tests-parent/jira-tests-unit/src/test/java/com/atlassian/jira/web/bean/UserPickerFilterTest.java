package com.atlassian.jira.web.bean;

import java.util.List;
import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.UserNameComparator;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * @since v6.2
 */
public class UserPickerFilterTest
{
    @SuppressWarnings ("unchecked")
    @Test
    public void testIntersectLists() throws Exception
    {
        final List<TestCase<List<User>[], List<User>>> testCases = ImmutableList.<TestCase<List<User>[], List<User>>>builder()
                .add(new TestCase<List<User>[], List<User>>(
                        "null lists",
                        new List[] { null, null },
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "null list1",
                        new List[] { null, ImmutableList.of(new MockUser("user1")) },
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "null list2",
                        new List[] { ImmutableList.of(new MockUser("user1")), null },
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "empty list1",
                        new List[] { ImmutableList.of(), ImmutableList.of(new MockUser("user1")) },
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "empty list2",
                        new List[] { ImmutableList.of(new MockUser("user1")), ImmutableList.of() },
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "empty list2",
                        new List[] { ImmutableList.of(new MockUser("user1")), ImmutableList.of()},
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "no intersection",
                        new List[] { ImmutableList.of(new MockUser("user1")), ImmutableList.of(new MockUser("user2"))},
                        ImmutableList.<User>of()
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "same list one item",
                        new List[] { ImmutableList.of(new MockUser("user1")), ImmutableList.of(new MockUser("user1"))},
                        ImmutableList.<User>of(new MockUser("user1"))
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "same list two items",
                        new List[] { ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user2")
                                    ), ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user2")
                        )},
                        ImmutableList.<User>of(new MockUser("user1"), new MockUser("user2"))
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "diff list one item diff beginning",
                        new List[] { ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user2")
                                    ), ImmutableList.of(
                                        new MockUser("user2")
                        )},
                        ImmutableList.<User>of(new MockUser("user2"))
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "diff list one item diff end",
                        new List[] { ImmutableList.of(
                                        new MockUser("user2")
                                    ), ImmutableList.of(
                                        new MockUser("user2"),
                                        new MockUser("user3")
                        )},
                        ImmutableList.<User>of(new MockUser("user2"))
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "diff list one item diff beginning and end",
                        new List[] { ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user2")
                                    ), ImmutableList.of(
                                        new MockUser("user2"),
                                        new MockUser("user3")
                        )},
                        ImmutableList.<User>of(new MockUser("user2"))
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "diff list two items diff middle",
                        new List[] { ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user2"),
                                        new MockUser("user3")
                                    ), ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user3"),
                                        new MockUser("user4")
                        )},
                        ImmutableList.<User>of(new MockUser("user1"), new MockUser("user3"))
                ))
                .add(new TestCase<List<User>[], List<User>>(
                        "diff list two items continuous same",
                        new List[] { ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user2"),
                                        new MockUser("user3"),
                                        new MockUser("user4")
                                    ), ImmutableList.of(
                                        new MockUser("user1"),
                                        new MockUser("user3"),
                                        new MockUser("user4")
                        )},
                        ImmutableList.<User>of(new MockUser("user1"), new MockUser("user3"), new MockUser("user4"))
                ))
                .build();
        final UserNameComparator userNameComparator = new UserNameComparator(Locale.ENGLISH);
        UserPickerFilter filter = new UserPickerFilter(null, null, null, null, null);
        for (TestCase<List<User>[], List<User>> testCase : testCases)
        {
            final List<User> result = filter.intersectLists(userNameComparator, testCase.input[0], testCase.input[1]);
            assertThat(testCase.name + " size", result, hasSize(testCase.expected.size()));
            for (int i = 0 ; i < result.size(); i++)
            {
                assertThat(testCase.name + " item " + i, result.get(i), equalTo(testCase.expected.get(i)));
            }
        }
    }

    private static class TestCase<Input, Output>
    {
        String name;
        Input input;
        Output expected;

        TestCase(final String name, final Input input, final Output expected)
        {
            this.name = name;
            this.input = input;
            this.expected = expected;
        }

        @Override
        public String toString()
        {
            return "TestCase{name='" + name + "'}";
        }
    }
}
