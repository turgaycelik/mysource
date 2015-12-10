notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'Comment Added', recipient: 'user', user: 'pkuo')
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JBAC Notifications', notify: 'true')
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JIRA Dev', notify: 'true')
    notification(type: 'Failed Jobs and First Successful', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JBAC Notifications', notify: 'true')
    notification(type: 'Job Error', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'Build Engineering Alerts', notify: 'true')
    notification(type: 'Job Hung', recipient: 'responsible')
    notification(type: 'All Builds Completed', recipient: 'stash')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

globalVariables() {
    variable(key: 'destruction.plugin.version', value: '"not-relevant-for-master-branch"')
    variable(key: 'max.batches', value: '20')
    variable(key: 'prepare.artifacts', value: 'package -B -pl jira-distribution/jira-func-tests-runner -am -Pdistribution,maven3-statistics -Dmaven.test.skip=true -Djira.minify.skip=true -Dfunc.mode.plugins -Dreference.plugins -Djira.func.tests.runner.create -DobrRepository=NONE -DskipSources -e')
    variable(key: 'run.server', value: 'test -B -PhallelujahServer,maven3-statistics')
    variable(key: 'run.tests', value: 'verify -B -X -e -PhallelujahClient,maven3-statistics -Djira.security.disabled=true -Djira.scheduler.RamJobStore=true')
}

produceFuncArtifacts(['repoName', 'jdk']) {
    job(name: 'Produce artifacts', key: 'PRODART', description: '') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'hallelujah-dist', location: 'jira-distribution/jira-func-tests-runner/target', pattern: 'jira-*-dist.zip', shared: 'true')
        artifactDefinition(name: 'prepare hallelujah', location: 'jira-distribution/jira-func-tests-runner/src/main/dist', pattern: '*.sh', shared: 'true')
        artifactDefinition(name: 'Release report', location: '', pattern: '${bamboo.jira.fe.release.report.filename}', shared: 'true')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        prepareFuncArtifacts(jdk: '#jdk')
        createReleaseReport()
    }
}

prepareFuncArtifacts(['jdk']) {
    task(
            type: 'maven3',
            description: 'Prepares the functional test artifacts',
            goal: '${bamboo.prepare.artifacts} -Denforcer.skip=true -Ddestruction.plugin.version=${bamboo.destruction.plugin.version}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}

createReleaseReport() {
    task(
            type: 'script',
            description: 'Creates release report',
            script: 'bin/create-release-report.sh',
            argument: '${bamboo.jira.fe.release.report.repo} ${bamboo.jira.fe.release.report.start.branch} master ${bamboo.jira.fe.release.report.filename}'
    )
}

findbugs(['repoName', 'jdk']) {
    job(name: 'Findbugs', key: 'FINDBUGS', description: 'Run Findbugs') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        checkout(repoName: '#repoName', forceClean: 'false')
        findbugsChecks(jdk: '#jdk')
    }
}

findbugsChecks(['jdk']) {
    task(
            type: 'maven3',
            description: 'Run Findbugs checks',
            goal: '-B test -pl jira-components/jira-plugins/jira-bundled-plugins -am -DskipTests -Pfindbugs,maven3-statistics',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}

funcTestsJobs(['jdk']) {
    funcTestsJob(index: '01', jdk: '#jdk')
    funcTestsJob(index: '02', jdk: '#jdk')
    funcTestsJob(index: '03', jdk: '#jdk')
    funcTestsJob(index: '04', jdk: '#jdk')
    funcTestsJob(index: '05', jdk: '#jdk')
    funcTestsJob(index: '06', jdk: '#jdk')
    funcTestsJob(index: '07', jdk: '#jdk')
    funcTestsJob(index: '08', jdk: '#jdk')
    funcTestsJob(index: '09', jdk: '#jdk')
    funcTestsJob(index: '10', jdk: '#jdk')
    funcTestsJob(index: '11', jdk: '#jdk')
    funcTestsJob(index: '12', jdk: '#jdk')
    funcTestsJob(index: '13', jdk: '#jdk')
    funcTestsJob(index: '14', jdk: '#jdk')
    funcTestsJob(index: '15', jdk: '#jdk')
    funcTestsJob(index: '16', jdk: '#jdk')
    funcTestsJob(index: '17', jdk: '#jdk')
    funcTestsJob(index: '18', jdk: '#jdk')
    funcTestsJob(index: '19', jdk: '#jdk')
    funcTestsJob(index: '20', jdk: '#jdk')
}

funcTestsJob(['index', 'jdk']) {
    job(name: 'Func #index', key: 'F#index', description: 'Func #index') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Cargo Logs', location: 'target/test-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'HeapDump', location: 'target', pattern: 'heapdump.tgz', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'target/jirahome/log', pattern: '**/*.log*', shared: 'false')

        artifactSubscription(name: 'hallelujah-dist', destination: '')
        artifactSubscription(name: 'prepare hallelujah', destination: '')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        brokerUrlDiscovery()
        prepareRunner()
        runFuncTests(jdk: '#jdk')
        finalTask()
    }
}

prepareRunner() {
    task(
            type: 'script',
            description: 'Prepares the runner',
            script: 'prepare-runner.sh'
    )
}

runFuncTests(['jdk']) {
    task(
            type: 'maven3',
            description: 'Maven functional tests',
            goal: '${bamboo.run.tests}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            hasTests: 'false'
    )
}

finalTask() {
    task(type: 'script', description: 'Final script', script: 'final-task.sh', final: 'true')
}

funcTestsHallelujahServer(['jdk']) {
    job(name: 'Hallelujah Test Server', key: 'HTS', description: 'Hallelujah test server') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Cargo Logs', location: 'target/test-reports', pattern: '*', shared: 'false')

        artifactSubscription(name: 'hallelujah-dist', destination: '')
        artifactSubscription(name: 'prepare hallelujah', destination: '')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        brokerUrlDiscovery()
        prepareRunner()
        runHallelujahFuncServer(jdk: '#jdk')
        funcTestsJUnitParser()
    }
}

runHallelujahFuncServer(['jdk']) {
    task(
            type: 'maven3',
            description: 'Runs hallelujah server',
            goal: '${bamboo.run.server}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'false'
    )
}

funcTestsJUnitParser() {
    task(type: 'jUnitParser', description: 'Parse JUnit results', final: 'true', resultsDirectory: 'TEST-Hallelujah.xml')
}

onDemandPluginUnitTests(['repoName', 'jdk']) {
    job(name: 'OnDemand Plugin Unit Tests', key: 'ODPUT', description: 'Runs OnDemand plugin unit tests') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        cleanCheckout(repoName: '#repoName')
        runOnDemandPluginUnitTests(jdk: '#jdk')
    }
}

runOnDemandPluginUnitTests(['jdk']) {
    task(
            type: 'maven3',
            description: 'Runs OnDemand plugin unit tests',
            goal: '-B -Pondemand,maven3-statistics -pl jira-ondemand-project/jira-ondemand-webapp clean verify -am -Dmaven.test.unit.skip=true',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'true'
    )
}

platformCompatibilityTest(['repoName', 'jdk']) {
    job(name: 'Platform Compatibility Test', key: 'PCT', description: 'Platform compatibility test') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Jira Logs', location: 'jira-distribution/jira-func-tests-runner/target/jirahome/log', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Logs Screenshots and Movies', location: 'jira-distribution/jira-func-tests-runner/target/test-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'SeleniumTestHarness', location: 'jira-distribution/jira-func-tests-runner/target/surefire-reports', pattern: '*', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runPlatformCompatibilityTest(jdk: '#jdk')
    }
}

runPlatformCompatibilityTest(['jdk']) {
    task(
            type: 'maven3',
            description: 'Runs the platform compatibility test',
            goal: 'clean verify -pl jira-distribution/jira-func-tests-runner -am -Pdistribution,platform-ctk,maven3-statistics -Dmaven.test.unit.skip=true -Ddev.mode.plugins -Djira.security.disabled=true  -Drmi.port=${bamboo.capability.rmi.port} -Djira.functest.single.testclass=com.atlassian.jira.webtests.ztests.misc.TestPlatformCompatibility -Dfunc.mode.plugins -Dsystem.bamboo.agent.home=${bamboo.agentWorkingDirectory} -B',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx768m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'true'
    )
}

qUnitTests(['repoName', 'jdk']) {
    job(name: 'QUnit Tests', key: 'QUT', description: 'Runs QUnit tests') {
        requireLocalLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Jira Logs', location: 'jira-distribution/jira-webdriver-tests-runner/target/jirahome/log', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Logs Screenshots and Movies', location: 'jira-distribution/jira-webdriver-tests-runner/target/test-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'SeleniumTestHarness', location: 'jira-distribution/jira-webdriver-tests-runner/target/surefire-reports', pattern: '*', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        qUnitVncServerSetup()
        runQUnitTests(jdk: '#jdk')
        qUnitVncServerTeardown()
    }
}

qUnitVncServerSetup() {
    task(
            type: 'script',
            description: 'VNC server setup',
            script: 'jira-distribution/jira-webdriver-tests-runner/vnc-setup.sh'
    )
}

qUnitVncServerTeardown() {
    task(
            type: 'script',
            description: 'VNC server teardown',
            script: 'jira-distribution/jira-webdriver-tests-runner/vnc-teardown.sh',
            final: 'true'
    )
}

runQUnitTests(['jdk']) {
    task(
            type: 'maven3',
            description: 'QUnit tests',
            goal: 'clean verify -B -Dmaven.test.func.skip=true -Dmaven.test.unit.skip=true -pl jira-distribution/jira-webdriver-tests-runner -am -Pdistribution,maven3-statistics -Djira.security.disabled=true -Dsystem.bamboo.agent.home=${bamboo.system.bamboo.agent.home} -Ddev.mode.plugins -Djira.functest.single.testclass=com.atlassian.jira.webtest.webdriver.qunit.TestQunit -Djava.awt.headless=true -Djira.minify.skip=true -Dfunc.mode.plugins',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}"',
            hasTests: 'true',
            testDirectory: '**/target/surefire-reports/*.xml'
    )
}

unitTestsAndFunctionalUnitTests(['repoName', 'jdk']) {
    job(name: 'Unit Tests plus Functional Unit Tests', key: 'UFUT', description: 'Our regular Unit Test build plus the Unit Tests that test the Functional Tests') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')


        artifactDefinition(name: 'Cargo Logs', location: 'jira-distribution/jira-func-tests-runner/target/test-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'jira-distribution/jira-func-tests-runner/target/jirahome/log', pattern: '**/*.log*', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runUnitTestsAndFunctionalUnitTests(jdk: '#jdk')
    }
}

runUnitTestsAndFunctionalUnitTests(['jdk']) {
    task(
            type: 'maven3',
            description: 'Runs the unit tests plus the unit tests that test the functional tests',
            goal: '-B clean verify -pl jira-distribution/jira-func-tests-runner,jira-webdriver-tests,jira-components/jira-tests-parent/jira-tests-legacy,jira-components/jira-tests-parent/jira-tests-unit -am -Pdistribution,maven3-statistics -Dmaven.test.func.skip=true -Djira.security.disabled=true  -Drmi.port=${bamboo.capability.rmi.port} -Djira.minify.skip=true -Dmaven.test.failure.ignore=true -Dsystem.bamboo.agent.home=${bamboo.system.bamboo.agent.home}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx512m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'true'
    )
}

singleTest(['name', 'testClass', 'index', 'jdk']) {
    job(name: '#name', key: 'STP#index', description: 'Setup test on pristine JIRA') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Jira Logs', location: 'jira-distribution/jira-func-tests-runner/target/jirahome/log', pattern: '*', shared: 'false')
        artifactDefinition(name: 'Logs Screenshots and Movies', location: 'jira-distribution/jira-func-tests-runner/target/test-reports', pattern: '*', shared: 'false')
        artifactDefinition(name: 'SeleniumTestHarness', location: 'jira-distribution/jira-func-tests-runner/target/surefire-reports', pattern: '*', shared: 'false')

        artifactSubscription(name: 'hallelujah-dist', destination: '')
        artifactSubscription(name: 'prepare hallelujah', destination: '')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')
        prepareRunner()

        runSingleTest(testClass: '#testClass', jdk: '#jdk')
    }
}

runSingleTest(['testClass', 'jdk']) {
    task(
            type: 'maven3',
            description: 'Runs Setup test',
            goal: 'clean verify -PdefaultBuild,maven3-statistics -Dmaven.test.unit.skip=true -Ddev.mode.plugins -Djira.security.disabled=true  -Drmi.port=${bamboo.capability.rmi.port} -Djira.functest.single.testclass=#testClass -Dfunc.mode.plugins -Dsystem.bamboo.agent.home=${bamboo.agentWorkingDirectory} -B',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0',
            environmentVariables: 'MAVEN_OPTS="-Xmx768m -Xms128m -XX:MaxPermSize=256m"',
            hasTests: 'true'
    )
}
