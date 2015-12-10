notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Comment Added', recipient: 'watchers')
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JIRA Dev', notify: 'true')
    notification(type: 'Job Hung', recipient: 'responsible')
    notification(type: 'Job Hung', recipient: 'watchers')
    notification(type: 'All Builds Completed', recipient: 'stash')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'watchers')
}

miscellaneousConfig() {
    planMiscellaneous(){
        concurrentBuilds(enabled:"true", max:"3")
    }
}

unitTests(['repoName', 'jdk']) {
    job(name: 'Unit Tests', key: 'UNIT', description: '') {
        requireLocalLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'dependencies report', location: 'jira-components/jira-core', pattern: 'dependencies.txt*', shared: 'false')
        artifactDefinition(name: 'maven stats', location: '', pattern: 'maven-stats.log', shared: 'false')
        artifactDefinition(name: 'Test Reports', location: '', pattern: '**/TEST-*.xml', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runUnitTests(jdk: '#jdk')
    }
}

runUnitTests(['jdk']) {
    task(
            type: 'maven3',
            description: 'Maven tests',
            goal: '-B clean test -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true -Dmaven.test.failure.ignore=true -pl jira-components/jira-tests-parent/jira-tests,jira-components/jira-tests-parent/jira-tests-unit,jira-components/jira-plugins/jira-bundled-plugins,jira-func-tests -am -DverifyDependencies -DperformApiCheck -Pmaven3-statistics',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'true'
    )
}
