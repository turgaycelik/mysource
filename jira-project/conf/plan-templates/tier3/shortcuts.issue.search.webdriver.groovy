notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Comment Added', recipient: 'watchers')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'watchers')
    notification(type: 'Job Hung', recipient: 'responsible')
    notification(type: 'Job Hung', recipient: 'watchers')
}

hallelujahClients(['issueSearchRepo', 'jiraRepo', 'jdk']) {
    hallelujahClient(id: '01', issueSearchRepo: '#issueSearchRepo', jiraRepo: '#jiraRepo', jdk: '#jdk')
    hallelujahClient(id: '02', issueSearchRepo: '#issueSearchRepo', jiraRepo: '#jiraRepo', jdk: '#jdk')
    hallelujahClient(id: '03', issueSearchRepo: '#issueSearchRepo', jiraRepo: '#jiraRepo', jdk: '#jdk')
    hallelujahClient(id: '04', issueSearchRepo: '#issueSearchRepo', jiraRepo: '#jiraRepo', jdk: '#jdk')
    hallelujahClient(id: '05', issueSearchRepo: '#issueSearchRepo', jiraRepo: '#jiraRepo', jdk: '#jdk')
    hallelujahClient(id: '06', issueSearchRepo: '#issueSearchRepo', jiraRepo: '#jiraRepo', jdk: '#jdk')
}

hallelujahClient(['id', 'issueSearchRepo', 'jiraRepo', 'jdk']) {
    job(name: 'Hallelujah Client #id', key: 'CLIENT#id', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requireLocalLinux()

        artifactDefinition(name: 'logs', location: '', pattern: '**/*.log', shared: 'false')

        brokerUrlDiscovery()
        checkoutInDir(repoName: '#issueSearchRepo', dir: 'PLUGIN')
        checkoutInDir(repoName: '#jiraRepo', dir: 'JIRA')
        generatePoms()
        buildAndInstall(jdk: '#jdk')
        runHallelujahClient()

    }
}

generatePoms() {
    task(
            type: 'script',
            description: 'Generate poms to build JIRA and PLUGIN together',
            script: 'JIRA/bin/generate-maven-aggregator-pom.sh',
            argument: 'JIRA PLUGIN'
    )
}

buildAndInstall(['jdk']) {
    task(
            type: 'maven3',
            description: 'Build and install',
            goal: 'install -B -pl JIRA/jira-distribution/jira-func-tests-runner -am -Pdistribution -Dmaven.test.skip -Djira.minify.skip=true -Dfunc.mode.plugins -Djira.func.tests.runner.create -DobrRepository=NONE -DskipSources',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}

runHallelujahClient() {
    task(
            type: 'script',
            description: 'Run Hallelujah client',
            script: 'run.sh',
            argument: '-B -PhallelujahClient -DnoDevMode verify',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            workingSubDirectory: 'PLUGIN'
    )
}

hallelujahServer(['issueSearchRepo', 'jiraRepo', 'jdk']) {
    job(name: 'Hallelujah Server', key: 'SERVER', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requireLocalLinux()

        artifactDefinition(name: 'Flakey tests file', location: '', pattern: '**/PLUGIN-flakyTest.txt', shared: 'false')

        brokerUrlDiscovery()
        checkoutInDir(repoName: '#issueSearchRepo', dir: 'PLUGIN')
        checkoutInDir(repoName: '#jiraRepo', dir: 'JIRA')
        generatePoms()
        buildAndInstall(jdk: '#jdk')
        runHallelujahServer(jdk: '#jdk')
        parseJUnitResults()
        flakyTestsDiscovery()
    }
}

runHallelujahServer(['jdk']) {
    task(
            type: 'maven3',
            description: 'Run Hallelujah server',
            goal: '-PhallelujahServer test -Dhallelujah.retries=5',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            workingSubDirectory: 'PLUGIN',
            hasTests: 'false'
    )
}

flakyTestsDiscovery() {
    task(
            type: 'flakyTestDiscovery',
            description: 'Discovery of flaky tests',
            flakyTestFileName: 'PLUGIN/PLUGIN-flakyTest.txt',
            final: 'true'
    )
}

parseJUnitResults() {
    task(type: 'jUnitParser', description: 'Parse JUnit results', final: 'true', resultsDirectory: 'PLUGIN/Hallelujah.xml, PLUGIN/**/surefire-reports/*.xml')
}
