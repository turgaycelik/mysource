notifications() {
    notification(type: 'Comment Added', recipient: 'group', group: 'jira-developers')
}

globalVariables() {
    variable(key: 'batch.max', value: '10')
}

oracleBatches(['repoName', 'jdk']) {
    oracleBatch(id: '01', batch: '1', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '02', batch: '2', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '03', batch: '3', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '04', batch: '4', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '05', batch: '5', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '06', batch: '6', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '07', batch: '7', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '08', batch: '8', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '09', batch: '9', repoName: '#repoName', jdk: '#jdk')
    oracleBatch(id: '10', batch: '10', repoName: '#repoName', jdk: '#jdk')
}

oracleBatch(['id','batch', 'repoName', 'jdk']) {
    job(name: 'Oracle11g Batch #id', key: 'B#id', description: '') {
        requireElasticAgents()
        requireOracle()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'All Log', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Cargo Logs', location: '.', pattern: '**/test-reports/**', shared: 'false')
        artifactDefinition(name: 'Catalina out', location: '.', pattern: '**/*.out', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        // FIXME: The oracle workaround can be removed once https://extranet.atlassian.com/jira/browse/BUILDENG-5706 is resolved
        oracleWorkaround()
        cleanMavenCache()
        runTests(jdk: '#jdk', batch: '#batch')
    }
}

oracleWorkaround() {
    task(
            type: 'script',
            description: 'Oracle Workaround',
            scriptBody: '''
sqlplus -s /nolog <<EOF
connect oracle/oracle
alter system set local_listener = XE;
alter system register;
quit
EOF'''
    )
}

runTests(['jdk', 'batch']) {
    task(
            type: 'maven2',
            description: 'Runs TPM tests',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA -DOracle10g -DnumberOfBatches=${bamboo.batch.max} -Dbatch=#batch -Dsnapshot=${bamboo.jira.6.3.branch.version} -B -e',
            environmentVariables: 'TZ="America/New_York" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=128m -Xms256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
