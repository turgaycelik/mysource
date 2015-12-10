plan(key: 'WDJ8', name: 'CI Webdriver - Java 8', description: 'Webdriver tests running with JDK 8') {
    def jdk7 = 'JDK 1.7'
    def jdk8 = 'JDK 1.8'
    def repoName = 'JIRA 6.3.x branch'

    java8Project()
    java8Labels()

    notifications()
    globalVariables()
    miscellaneousConfig()
    branches(pattern: 'issue/JDEV-28412-plan-templates', removeAfterDays: '7')
    repositoryPolling(repoName: repoName)

    stage(name: 'Unit', manual: 'false') {
        produceWebDriverArtifacts(repoName: repoName, jdk: jdk7)
    }

    stage(name: 'Webdriver Batches', manual: 'false') {
        webDriverTestsJobs(jdk: jdk8)
        webDriverTestsHallelujahServer(jdk: jdk8)
    }
}
