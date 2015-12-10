notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Comment Added', recipient: 'watchers')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'watchers')
    notification(type: 'Job Hung', recipient: 'responsible')
    notification(type: 'Job Hung', recipient: 'watchers')
}

functionalTests(['issueSearchRepo', 'jiraRepo', 'jdk']) {
    job(name: 'Functional Tests', key: 'FUNC', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireFirefox(ver: '10.0');
        requireLocalLinux()

        artifactDefinition(name: 'Heap dumps', location: '', pattern: '**/*.hprof', shared: 'false')
        artifactDefinition(name: 'logs', location: '', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'Surefire reports', location: 'PLUGIN/jira-issue-nav-plugin/target/surefire-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Test reports', location: 'PLUGIN/jira-issue-nav-plugin/target/test-reports', pattern: '*', shared: 'false')

        checkoutInDir(repoName: '#issueSearchRepo', dir: 'PLUGIN')
        checkoutInDir(repoName: '#jiraRepo', dir: 'JIRA')
        generatePoms()
        installAMPSArtifacts(jdk: '#jdk')
        runFuncTests(jdk: '#jdk')
        parseJUnitResults()
        removeArtifacts(jdk: '#jdk')
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

installAMPSArtifacts(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build and install artifacts required by AMPS',
            goal: 'clean install -B -am -Pdistribution -pl JIRA -Dmaven.test.skip -DskipSources',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            hasTests: 'false'
    )
}

runFuncTests(['jdk']) {
    task(
            type: 'script',
            description: 'Run the Func tests with the issue-search category',
            script: 'run.sh',
            argument: '-B clean verify -Djava.awt.headless=true -Dmaven.test.failure.ignore=true -PrunIndividualTestSuite -DtestGroups=runFuncTest -DnoDevMode',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'PLUGIN'
    )
}

removeArtifacts(['jdk']) {
    task(
            type: 'maven2',
            description: 'Remove installed artifacts',
            goal: 'build-helper:remove-project-artifact -Dbuildhelper.removeAll=false -Pdistribution -pl JIRA -am',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            hasTests: 'false',
            final: 'true'
    )
}

parseJUnitResults() {
    task(type: 'jUnitParser', description: 'Parse JUnit results', final: 'true', resultsDirectory: '**/surefire-reports/*.xml')
}

junitTests(['issueSearchRepo', 'jiraRepo', 'jdk']) {
    job(name: 'JUnit Tests', key: 'JUNIT', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        checkoutInDir(repoName: '#issueSearchRepo', dir: 'PLUGIN')
        checkoutInDir(repoName: '#jiraRepo', dir: 'JIRA')
        generatePoms()
        installUnitTestsArtifacts(jdk: '#jdk')
        runUnitTests(jdk: '#jdk')
        removeArtifacts(jdk: '#jdk')
    }
}

installUnitTestsArtifacts(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build and install artifacts required by PLUGIN unit tests',
            goal: 'clean install -B -am -DskipTests',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            hasTests: 'false'
    )
}

runUnitTests(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run the unit tests with the issue-search category',
            goal: '-B test',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            hasTests: 'true',
            workingSubDirectory: 'PLUGIN'
    )
}

qunitTests(['issueSearchRepo', 'jiraRepo', 'jdk']) {
    job(name: 'QUnit Tests', key: 'QUNIT', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireFirefox(ver: '10.0');
        requireLocalLinux()

        artifactDefinition(name: 'Heap dumps', location: '', pattern: '**/*.hprof', shared: 'false')
        artifactDefinition(name: 'logs', location: '', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'Surefire reports', location: 'PLUGIN/jira-issue-nav-plugin/target/surefire-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Test reports', location: 'PLUGIN/jira-issue-nav-plugin/target/test-reports', pattern: '*', shared: 'false')

        checkoutInDir(repoName: '#issueSearchRepo', dir: 'PLUGIN')
        checkoutInDir(repoName: '#jiraRepo', dir: 'JIRA')
        generatePoms()
        installAMPSArtifacts(jdk: '#jdk')
        runQUnitTests(jdk: '#jdk')
        parseJUnitResults()
        removeArtifacts(jdk: '#jdk')
    }
}

runQUnitTests(['jdk']) {
    task(
            type: 'script',
            description: 'Run the Qunit tests with the issue-search category',
            script: 'run.sh',
            argument: '-B clean verify -Djava.awt.headless=true -Dmaven.test.failure.ignore=true -PrunIndividualTestSuite -DtestGroups=runQunitTest -DnoDevMode',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'PLUGIN'
    )
}
