notifications() {
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JIRA Dev', notify: 'true')
    notification(type: 'Failed Jobs and First Successful', recipient: 'responsible')
    notification(type: 'Failed Jobs and First Successful', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JBAC Notifications', notify: 'true')
    notification(type: 'All Builds Completed', recipient: 'stash')
}

restApiGuard(['jiraRepo', 'restClientRepo', 'jdk']) {
    job(name: 'Default Job', key: 'JOB1', description: '') {
        requireLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        checkoutInDir(repoName: '#jiraRepo', dir: 'jira-master')
        checkoutInDir(repoName: '#restClientRepo', dir: 'jrjc')
        installJira(jdk: '#jdk')
        runIntegrationTests(jdk: '#jdk')
    }
}

installJira(['jdk']) {
    task(
            type: 'maven2',
            description: 'install JIRA to .m2',
            goal: 'install -DskipTests=true',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx1536m -Xms256m -XX:MaxPermSize=256m"',
            workingSubDirectory: 'jira-master'
    )
}

runIntegrationTests(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run Integration Tests',
            goal: 'clean integration-test -Djira.version=${bamboo.jira.6.3.branch.version}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx256m -Xms128m"',
            workingSubDirectory: 'jrjc',
            hasTests: 'true',
            testDirectory: '**/target/surefire-reports/*.xml, **/target/group-__no_test_group__/tomcat6x/surefire-reports/*.xml'
    )
}
