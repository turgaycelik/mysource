notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

globalVariables() {
    variable(key: 'batch.max', value: '10')
    variable(key: 'branch.url', value: 'trunk')
}

miscellaneousConfig() {
    planMiscellaneous() {
        concurrentBuilds(enabled:"true", max:"2")
    }
}

postgresBatches(['repoName', 'jdk']) {
    postgresBatch(id: '01', batch:'1', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '02', batch:'2', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '03', batch:'3', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '04', batch:'4', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '05', batch:'5', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '06', batch:'6', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '07', batch:'7', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '08', batch:'8', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '09', batch:'9', repoName: '#repoName', jdk: '#jdk')
    postgresBatch(id: '10', batch:'10', repoName: '#repoName', jdk: '#jdk')
}

postgresBatch(['id', 'batch', 'repoName', 'jdk']) {
    job(name: 'Postgres Batch #id', key: 'PG#id', description: '') {
        requireMaven2()
        requirePostgres9_0()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'All Logs', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Cargo Logs', location: '.', pattern: '**/test-reports/**', shared: 'false')
        artifactDefinition(name: 'Catalina out', location: '.', pattern: '**/*.out', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        runTests(jdk: '#jdk', batch: '#batch')
    }
}

runTests(['jdk', 'batch']) {
    task(
            type: 'maven2',
            description: 'Runs TPM tests',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA -DPostgres -DnumberOfBatches=${bamboo.batch.max} -Dbatch=#batch -Dsnapshot=${bamboo.jira.6.3.branch.version} -B -e',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms256m -XX:MaxPermSize=256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
