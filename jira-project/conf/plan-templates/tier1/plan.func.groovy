// Depends on:
//     tier1/shortcuts.func.groovy
//     tier1/shortcuts.tier1.groovy
//     shortcuts.common.groovy

plan(key: 'CIFUNCHAL', name: 'CI Functional Tests', description: '') {
    def jdk6 = 'JDK 1.6_patched'
    def jdk7 = 'JDK 1.7_patched'
    def repoName = 'JIRA 6.3.x branch'

    tier1Project()
    tier1Labels()

    repositoryPolling(repoName: repoName)
    branches(pattern: 'stable-issue/.*', removeAfterDays: '7')
    planDependencies()
    notifications()
    globalVariables()

    stage(name: 'Compile', manual: 'false') {
        produceFuncArtifacts(repoName: repoName, jdk: jdk6)
    }

    stage(name: 'Unit and Functional Tests', manual: 'false') {
        findbugs(repoName: repoName, jdk: jdk7)
        funcTestsJobs(jdk: jdk7)
        funcTestsHallelujahServer(jdk: jdk7)
        onDemandPluginUnitTests(repoName: repoName, jdk: jdk7)
        platformCompatibilityTest(repoName: repoName, jdk: jdk7)
        qUnitTests(repoName: repoName, jdk: jdk7)
        unitTestsAndFunctionalUnitTests(repoName: repoName, jdk: jdk7)

        singleTest(name: 'Test Setup - Greenhopper', index: '01', testClass: 'com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundlesAgile', jdk: jdk7)
        singleTest(name: 'Test Setup - ServiceDesk', index: '02', testClass: 'com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundleServiceDesk', jdk: jdk7)
        singleTest(name: 'Test Setup - Default', index: '03', testClass: 'com.atlassian.jira.webtests.ztests.setup.TestSetupPreinstalledBundleDefault', jdk: jdk7)
    }
}
