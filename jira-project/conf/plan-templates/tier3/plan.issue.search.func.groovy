// Depends on:
//     tier3/shortcuts.issue.search.func.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'ISFUNCUNIT', name: 'Issue Search - Functional and Units', description: '') {
    def jdk = 'JDK 1.6'
    def jiraRepo = 'JIRA 6.3.x branch'
    def issueSearchRepo = 'JIRA Issue Search Plugin 6.3.x branch'

    tier3Project()
    tier3Labels()

    repositoryPollingWithFrequency(repoName: issueSearchRepo, frequency: '60')
    repositoryPollingWithFrequency(repoName: jiraRepo, frequency: '21600')
    notifications()

    stage(name: 'Default stage', manual: 'false') {
        functionalTests(issueSearchRepo: issueSearchRepo, jiraRepo: jiraRepo, jdk: jdk)
        junitTests(issueSearchRepo: issueSearchRepo, jiraRepo: jiraRepo, jdk: jdk)
        qunitTests(issueSearchRepo: issueSearchRepo, jiraRepo: jiraRepo, jdk: jdk)
    }
}
