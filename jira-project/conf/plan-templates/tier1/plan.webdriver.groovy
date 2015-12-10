// Depends on:
//     tier1/shortcuts.webdriver.groovy
//     tier1/shortcuts.tier1.groovy
//     shortcuts.common.groovy

plan(key: 'CIWEBDRIVER', name: 'CI Webdriver', description: '') {
    def jdk6 = 'JDK 1.6'
    def jdk7 = 'JDK 1.7'
    def repoName = 'JIRA 6.3.x branch'

    tier1Project()
    tier1Labels()

    repositoryPolling(repoName: repoName)
    branches(pattern: 'stable-issue/.*', removeAfterDays: '7')
    notifications()
    globalVariables()
    miscellaneousConfig()

    stage(name: 'Unit', manual: 'false') {
        produceWebDriverArtifacts(repoName: repoName, jdk: jdk6)
    }

    stage(name: 'Webdriver Batches', manual: 'false') {
        webDriverTestsJobs(jdk: jdk7)
        webDriverTestsHallelujahServer(jdk: jdk7)
    }
}
