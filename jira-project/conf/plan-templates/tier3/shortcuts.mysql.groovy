notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

globalVariables() {
    variable(key: 'batch.max', value: '10')
    variable(key: 'branch.url', value: 'master')
}

mysqlBatches(['repoName', 'jdk']) {
    mysqlBatch(id: '01', batch: '1', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '02', batch: '2', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '03', batch: '3', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '04', batch: '4', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '05', batch: '5', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '06', batch: '6', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '07', batch: '7', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '08', batch: '8', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '09', batch: '9', repoName: '#repoName', jdk: '#jdk')
    mysqlBatch(id: '10', batch: '10', repoName: '#repoName', jdk: '#jdk')
}

mysqlBatch(['id', 'batch', 'repoName', 'jdk']) {
    job(name: 'MySQL Batch #id', key: 'MYSQL#id', description: '') {
        requireLinux()
        requireMysql()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'All logs', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Catalina out', location: '.', pattern: '**/*.out', shared: 'false')
        artifactDefinition(name: 'Everything in test-reports', location: '.', pattern: '**/test-reports/*', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        displayNofiles()
        runMysqlBatch(jdk: '#jdk', batch: '#batch')

    }
}

displayNofiles() {
    task(
            type: 'script',
            description: 'Display nofiles',
            scriptBody: '''
echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
echo "Displaying nofiles"
echo "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
ulimit -n'''
    )
}

runMysqlBatch(['jdk', 'batch']) {
    task(
            type: 'maven2',
            description: 'Runs MYSQL batch',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA -DMySQL -DnumberOfBatches=${bamboo.batch.max} -Dbatch=#batch -Dsnapshot=${bamboo.jira.6.3.branch.version} -B -e -X',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms256m -XX:MaxPermSize=256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
