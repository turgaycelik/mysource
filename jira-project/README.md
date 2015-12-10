# JIRA Source Code

## Getting Started
[Development Handbook](https://extranet.atlassian.com/display/JIRADEV/JIRA+Development+Handbook)

[Rules of Engagement](https://extranet.atlassian.com/display/JIRADEV/The+JIRA+development+process)

## Running from Source

```
> ./jmake run
```

or

```
> ./jmake debug
```

Run Unit tests:

```
> ./jmake unit-tests
```

See also

```
> ./jmake --help
> ./jmake <command> -h
```

## Working on master

First create an issue branch from master named like **issue/JDEV-1234-my-change** (Stash UI will help you)

Watch the branch builds on Tier 1 and Tier 2 builds:

[Tier 1](https://jira-bamboo.internal.atlassian.com/browse/MASTERONE)

[Tier 2](https://jira-bamboo.internal.atlassian.com/browse/MASTERTWO)
