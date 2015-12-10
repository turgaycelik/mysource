notifications() {
    notification(type: 'Change of Responsibilities', recipient: 'responsible')
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JBAC Notifications', notify: 'true')
    notification(type: 'All Builds Completed', recipient: 'stash')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

globalVariables() {
    variable(key: 'destruction.plugin.version', value: '"not-relevant-for-master-branch"')
    variable(key: 'planKey', value: 'JIRAMASTER-FIREFOXHAL')
    variable(key: 'produce.artifacts', value: 'package -Dmaven.test.skip=true -pl jira-components/jira-webapp,jira-webdriver-tests -am -Pdistribution,jar-with-dependencies,maven3-statistics -Dfunc.mode.plugins -Dreference.plugins -DskipSources')
    variable(key: 'run.selenium.server', value: 'test  -PhallelujahServer,use-artifact-passing,distribution -Dxvfb.enable=true -Dmaven.test.unit.skip=true -Dselenium.browser=firefox -Djava.awt.headless=true -pl jira-selenium-tests-runner -am')
    variable(key: 'run.selenium.test', value: 'verify -PhallelujahClient,distribution,use-artifact-passing -Dmaven.test.unit.skip=true -Djira.security.disabled=true -Dselenium.browser=firefox -Djava.awt.headless=true -Djira.use.artifact.passing -pl jira-selenium-tests-runner -am')
    variable(key: 'run.webdriver.server', value: 'test -PhallelujahServer,use-artifact-passing,maven3-statistics -pl jira-webdriver-tests-runner -am')
    variable(key: 'run.webdriver.server.tests', value: '-Datlassian.test.suite.includes=WEBDRIVER_TEST -Datlassian.test.suite.excludes=TPM,RELOADABLE_PLUGINS,VISUAL_REGRESSION,VISUAL_REGRESSION_SETUP')
    variable(key: 'run.webdriver.test.1', value: 'verify -B -PhallelujahClient,use-artifact-passing,maven3-statistics -Djira.security.disabled=true -Dsystem.bamboo.agent.home=${bamboo.system.bamboo.agent.home} -Dmaven.test.unit.skip=true -Djava.awt.headless=true')
    variable(key: 'run.webdriver.test.2', value: '-Djira.minify.skip=true -Datlassian.test.suite.package=com.atlassian.jira.webtest.webdriver.tests -Djira.use.artifact.passing=true -pl jira-webdriver-tests-runner -am')
}

miscellaneousConfig() {
    buildExpiry(artifacts: 'true', buildLogs: 'true', duration: '3', period: 'days', minimumBuildsToKeep: '3', labelsToKeep: 'infrastructure poison')
}

produceWebDriverArtifacts(['repoName', 'jdk']) {
    job(name: 'Produce JIRA artifacts', key: 'PRODJART', description: '') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'jira-distribution POM', location: 'jira-distribution/target', pattern: 'pom-transformed.xml', shared: 'true')
        artifactDefinition(name: 'jira-webapp-dist', location: 'jira-components/jira-webapp/target', pattern: 'jira.war', shared: 'true')
        artifactDefinition(name: 'jira-webdriver-tests-runner', location: '', pattern: 'jira-webdriver-tests-runner.zip', shared: 'true')
        artifactDefinition(name: 'jira-webdriver-tests', location: 'jira-webdriver-tests/target', pattern: 'jira-webdriver-tests-*-jar-with-dependencies.jar', shared: 'true')
        artifactDefinition(name: 'prepare-webdriver-tests', location: 'jira-distribution/jira-webdriver-tests-runner', pattern: 'prepare*.sh', shared: 'true')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        prepareWebDriverArtifacts(jdk: '#jdk')
        excludeIntegrationTests()
    }
}

prepareWebDriverArtifacts(['jdk']) {
    task(
            type: 'maven3',
            description: 'Prepares the webdriver test artifacts',
            goal: '${bamboo.produce.artifacts}  -Denforcer.skip=true -Ddestruction.plugin.version=${bamboo.destruction.plugin.version}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}

excludeIntegrationTests() {
    task(
            type: 'script',
            description: 'Leaves out the integration tests because they depend on the JIRA project',
            script: 'bin/leaveOutIntegrationTests'
    )
}

webDriverTestsJobs(['jdk']) {
    webDriverTestsJob(index: '01', jdk: '#jdk')
    webDriverTestsJob(index: '02', jdk: '#jdk')
    webDriverTestsJob(index: '03', jdk: '#jdk')
    webDriverTestsJob(index: '04', jdk: '#jdk')
    webDriverTestsJob(index: '05', jdk: '#jdk')
    webDriverTestsJob(index: '06', jdk: '#jdk')
    webDriverTestsJob(index: '06', jdk: '#jdk')
    webDriverTestsJob(index: '07', jdk: '#jdk')
    webDriverTestsJob(index: '08', jdk: '#jdk')
    webDriverTestsJob(index: '09', jdk: '#jdk')
    webDriverTestsJob(index: '10', jdk: '#jdk')
    webDriverTestsJob(index: '11', jdk: '#jdk')
    webDriverTestsJob(index: '12', jdk: '#jdk')
    webDriverTestsJob(index: '13', jdk: '#jdk')
    webDriverTestsJob(index: '14', jdk: '#jdk')
    webDriverTestsJob(index: '15', jdk: '#jdk')
    webDriverTestsJob(index: '16', jdk: '#jdk')
}

webDriverTestsJob(['index', 'jdk']) {
    job(name: 'WebDriver Batch #index', key: 'WDB#index', description: '') {
        requireLinux()
        requireMaven2()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Jira Logs', location: 'jira-distribution/jira-webdriver-tests-runner/target/jirahome/log', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Logs, Screenshots and Movies', location: 'jira-distribution/jira-webdriver-tests-runner/target/test-reports', pattern: '**', shared: 'false')
        artifactDefinition(name: 'Test reports', location: 'jira-distribution/jira-webdriver-tests-runner/target/surefire-reports', pattern: '*', shared: 'false')

        artifactSubscription(name: 'prepare-webdriver-tests', destination: '')
        artifactSubscription(name: 'jira-webdriver-tests-runner', destination: '')
        artifactSubscription(name: 'jira-webdriver-tests', destination: 'jira-distribution/jira-webdriver-tests-runner/PassedArtifacts')
        artifactSubscription(name: 'jira-webapp-dist', destination: 'jira-distribution/jira-webdriver-tests-runner/PassedArtifacts')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')


        brokerUrlDiscovery()
        prepareWebDriverRunner(jdk: '#jdk')
        webDriverVncServerSetup(jdk: '#jdk')
        runWebDriverTests(jdk: '#jdk')
        webDriverVncServerTeardown(jdk: '#jdk')
    }
}

prepareWebDriverRunner(['jdk']) {
    task(
            type: 'script',
            description: 'Prepares the WebDriver runner',
            script: 'prepare-webdriver-runner.sh',
            environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

webDriverVncServerSetup(['jdk']) {
    task(
            type: 'script',
            description: 'VNC Server setup',
            script: 'jira-webdriver-tests-runner/vnc-setup.sh',
            environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'jira-distribution'
    )
}

runWebDriverTests(['jdk']) {
    task(
            type: 'maven3',
            description: 'Runs WebDriver tests',
            goal: '${bamboo.run.webdriver.test.1} ${bamboo.run.webdriver.test.2} -Djira.hallelujah.queueId=${bamboo.planKey}-${bamboo.buildNumber}-webdriver',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'jira-distribution',
            hasTests: 'false'
    )
}

webDriverVncServerTeardown(['jdk']) {
    task(
            type: 'script',
            description: 'VNC Server teardown',
            script: 'jira-webdriver-tests-runner/vnc-teardown.sh',
            environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'jira-distribution',
            final: 'true'
    )
}

webDriverTestsHallelujahServer(['jdk']) {
    job(name: 'WebDriver Hallelujah Server', key: 'WDHS', description: '') {
        requireLinux()
        requireMaven2()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Jira Logs', location: 'jira-distribution/jira-webdriver-tests-runner/target/jirahome/log', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Logs, Screenshots and Movies', location: 'jira-distribution/jira-webdriver-tests-runner/target/test-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Test reports', location: 'jira-distribution/jira-webdriver-tests-runner/target/surefire-reports', pattern: '*', shared: 'false')

        artifactSubscription(name: 'prepare-webdriver-tests', destination: '')
        artifactSubscription(name: 'jira-webdriver-tests-runner', destination: '')
        artifactSubscription(name: 'jira-webdriver-tests', destination: 'jira-distribution/jira-webdriver-tests-runner/PassedArtifacts')
        artifactSubscription(name: 'jira-webapp-dist', destination: 'jira-distribution/jira-webdriver-tests-runner/PassedArtifacts')

        brokerUrlDiscovery()
        prepareWebDriverRunner(jdk: '#jdk')
        webDriverVncServerSetup(jdk: '#jdk')
        runHallelujahWebDriverServer(jdk: '#jdk')
        webDriverTestsJUnitParser()
        webDriverVncServerTeardown(jdk: '#jdk')
        flakyTestsDiscovery()
    }
}

runHallelujahWebDriverServer(['jdk']) {
    task(
            type: 'maven3',
            description: 'Runs hallelujah server',
            goal: '${bamboo.run.webdriver.server} ${bamboo.run.webdriver.server.tests} -Djira.hallelujah.queueId=${bamboo.planKey}-${bamboo.buildNumber}-webdriver',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            workingSubDirectory: 'jira-distribution',
            hasTests: 'false'
    )
}

webDriverTestsJUnitParser() {
    task(type: 'jUnitParser', description: 'Parse JUnit results', resultsDirectory: '**/TEST-Hallelujah.xml', final: 'true')
}

flakyTestsDiscovery() {
    task(
            type: 'flakyTestDiscovery',
            description: 'Discovery of flaky tests',
            flakyTestFileName: 'jira-distribution/flakyTests.txt',
            final: 'true'
    )
}
