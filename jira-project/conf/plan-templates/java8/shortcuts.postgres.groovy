dailyBuild(['repoName', 'scheduledHour', 'scheduledMin']) {
    repository(name: '#repoName')
    trigger(description: 'daily execution trigger', type: 'cron', cronExpression: '0 #scheduledMin #scheduledHour 1/1 * ? *', onlyBranchesWithChanges: 'true')
}

noBranchMonitoring() {
    branchMonitoring(
            enabled: 'false',
            notificationStrategy: 'NOTIFY_COMMITTERS',
            remoteJiraBranchLinkingEnabled: 'true'
    )
}

notifications() {
    notification(type: 'Comment Added', recipient: 'user', user: 'bayers')
    notification(type: 'Failed Builds and First Successful', recipient: 'user', user: 'bayers')
}

globalVariables() {
    variable(key: 'batch.max', value: '10')
    variable(key: 'branch.url', value: 'trunk')
    variable(key: 'postgresql.version', value: '9.3')
}

miscellaneousSettings() {
    planMiscellaneous() {
        sandbox(enabled:"true", automaticPromotion:"false")
    }
}

buildJiraFromSources(['repoName', 'jdk']) {
    job(name: 'Build JIRA from sources', key: 'BJFS', description: '') {
        requireLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        checkout(repoName: '#repoName', forceClean: 'false')
        deployJira(jdk: '#jdk')
    }
}

deployJira(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploys JIRA',
            goal: 'deploy -B -pl jira-distribution/jira-standalone-distribution,jira-distribution/jira-func-tests-runner,jira-components/jira-plugins/jira-plugin-test-resources,jira-components/jira-plugins/jira-func-test-plugin,jira-components/jira-plugins/jira-reference-plugin,jira-components/jira-plugins/jira-reference-language-pack,jira-components/jira-plugins/jira-reference-dependent-plugin -am -Pdistribution -Dmaven.test.skip -Djira.minify.skip=true -Djira.func.tests.runner.create -DobrRepository=NONE -DskipSources',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}

postgresTestsJobs(['repoName', 'jdk']) {
    postgresTestsJob(index: '01', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '02', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '03', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '04', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '05', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '06', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '07', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '08', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '09', repoName: '#repoName', jdk: '#jdk')
    postgresTestsJob(index: '10', repoName: '#repoName', jdk: '#jdk')
}

postgresTestsJob(['index', 'repoName', 'jdk']) {
    job(name: 'Postgres Batch #index', key: 'PB#index', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requirePostgres()

        artifactDefinition(name: 'All Logs', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Cargo Logs', location: '.', pattern: '**/test-reports/**', shared: 'false')
        artifactDefinition(name: 'Catalina out', location: '.', pattern: '**/*.out', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        clearLocalMavenRepo()
        runPostgresTests(jdk: '#jdk')
    }
}

clearLocalMavenRepo() {
    task(
            type: 'script',
            description: 'Clear Cache',
            scriptBody: '''
echo "=== Cleaning JIRA local .m2 cache ==="
if [ -d ~/.m2/repository/com/atlassian/jira ]; then
   rm -rf ~/.m2/repository/com/atlassian/jira
fi'''
    )
}

runPostgresTests(['jdk']) {
    task(
            type: 'maven2',
            description: 'Runs postgres tests',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA -DPostgres=${bamboo.postgresql.version} -DnumberOfBatches=${bamboo.batch.max} -Dbatch=1 -Dsnapshot=${bamboo.jira.version} -B -e',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms256m -XX:MaxPermSize=256m"',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
