// Depends on:
//     tier3/shortcuts.issue.search.webdriver.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'ISWD', name: 'Issue Search - WebDriver', description: '') {
    def jdk = 'JDK 1.7'
    def jiraRepo = 'JIRA 6.3.x branch'
    def issueSearchRepo = 'JIRA Issue Search Plugin 6.3.x branch'

    tier3Project()
    tier3Labels()

    repositoryPollingWithFrequency(repoName: issueSearchRepo, frequency: '60')
    repositoryPollingWithFrequency(repoName: jiraRepo, frequency: '21600')
    notifications()

    stage(name: 'Default stage', manual: 'false') {
        hallelujahClients(issueSearchRepo: issueSearchRepo, jiraRepo: jiraRepo, jdk: jdk)
        hallelujahServer(issueSearchRepo: issueSearchRepo, jiraRepo: jiraRepo, jdk: jdk)
    }
}
