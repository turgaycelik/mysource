plan(key: 'FUNCJ8', name: 'CI Functional Tests - Java 8', description: 'Functional tests running with JDK 8') {
    def jdk7 = 'JDK 1.7'
    def jdk8 = 'JDK 1.8'
    def repoName = 'JIRA 6.3.x branch'

    java8Project()
    java8Labels()

    notifications()
    globalVariables()
    branches(pattern: 'issue/JDEV-28412-plan-templates', removeAfterDays: '7')
    repositoryPolling(repoName: repoName)

    stage(name: 'Compile', manual: 'false') {
        produceFuncArtifacts(repoName: repoName, jdk: jdk7)
    }

    stage(name: 'Unit and Functional Tests', manual: 'false') {
        findbugs(repoName: repoName, jdk: jdk7)
        funcTestsJobs(jdk: jdk8)
        funcTestsHallelujahServer(jdk: jdk8)
        onDemandPluginUnitTests(repoName: repoName, jdk: jdk7)
        platformCompatibilityTest(repoName: repoName, jdk: jdk7)
        qUnitTests(repoName: repoName, jdk: jdk7)
        unitTestsAndFunctionalUnitTests(repoName: repoName, jdk: jdk7)

        signleTest(name: 'Test Setup - Greenhopper', index: '01', testClass: 'com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundlesAgile', jdk: jdk7)
        singleTest(name: 'Test Setup - ServiceDesk', index: '02', testClass: 'com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundleServiceDesk', jdk: jdk7)
        singleTest(name: 'Test Setup - Default', index: '03', testClass: 'com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundleDefault', jdk: jdk7)
    }
}
