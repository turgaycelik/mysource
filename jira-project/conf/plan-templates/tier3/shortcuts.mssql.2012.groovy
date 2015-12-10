notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

globalVariables() {
    variable(key: 'batch.max', value: '10')
    variable(key: 'branch.url', value: 'master')
}

mssqlBatches(['repoName', 'jdk']) {
    mssqlBatch(id: '01', batch: '1', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '02', batch: '2', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '03', batch: '3', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '04', batch: '4', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '05', batch: '5', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '06', batch: '6', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '07', batch: '7', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '08', batch: '8', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '09', batch: '9', repoName: '#repoName', 'jdk': '#jdk')
    mssqlBatch(id: '10', batch: '10', repoName: '#repoName', 'jdk': '#jdk')
}

mssqlBatch(['id', 'batch', 'repoName', 'jdk']) {
    job(name: 'MSSQL2012 Batch #id', key: 'MSSQL#id', description: '') {
        requireElasticWindows()
        requireSql2012Server()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'All Logs', location: '.', pattern: '**/*.log*', shared: 'false')
        artifactDefinition(name: 'Cargo Logs', location: '.', pattern: '**/test-reports/**', shared: 'false')
        artifactDefinition(name: 'Catalina out', location: '.', pattern: '**/*.out', shared: 'false')
        artifactDefinition(name: 'Jira Version', location: '', pattern: '**/serverInfo.txt', shared: 'false')

        cleanCheckout(repoName: '#repoName')
        runMssqlBatch(jdk: '#jdk', batch: '#batch')
    }
}

runMssqlBatch(['jdk', 'batch']) {
    task(
            type: 'maven2',
            description: 'Runs MSSQL batch',
            goal: '-f jira-distribution/tpm-standalone/spm/pom.xml clean install -DJIRA=Trunk -DStandalone -DIntegrationTest=JIRA -DMSSQL2012 -DnumberOfBatches=10 -Dbatch=#batch -Dsnapshot=${bamboo.jira.6.3.branch.version} -B -e',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms256m -XX:MaxPermSize=256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
