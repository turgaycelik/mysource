notifications() {
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JBAC Dev', notify: 'true')
    notification(type: 'Failed Jobs and First Successful', recipient: 'responsible')
}

globalVariables() {
    releaseGlobalVariables()
    variable(key: 'AUTHFILE', value: '/opt/bamboo-agent/credentials.xml')
    variable(key: 'batch.max', value: '20')
    variable(key: 'deploySourceJars', value: 'false')
    variable(key: 'isrelease', value: 'false')
    variable(key: 'maven.opts', value: '-Xmx2048m -Xms128m -XX:MaxPermSize=512m')
}

miscellaneousConfig() {
    planMiscellaneous(){
        concurrentBuilds(enabled:"true", max:"1")
        sandbox(enabled:"true", automaticPromotion:"false")
    }
}

buildAllJira(['repoName', 'jdk']) {
    job(name: 'Build ALL JIRA', key: 'JOB1', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'JIRA Source Distribution', location: 'jira-distribution/jira-source-distribution/target', pattern: '*.tar.gz', shared: 'true')
        artifactDefinition(name: 'Release Report', location: '', pattern: '${bamboo.jira.fe.release.report.filename}', shared: 'true')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        deployJiraProject(jdk: '#jdk')
        deployNightlyTestsPoms(jdk: '#jdk')
        cleanMavenCache()
    }
}

deployJiraProject(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploy JIRA Project',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml -B clean deploy -Pcreate-rest-apidocs,distribution,installer -Djira.generateDocs -P-dependency-tracking -Djira.screenshotapplet.signscreenshotapplet=true -Datlassian.keystore.storepass=${bamboo.sign.password} -Dinstall4j.home=${bamboo.capability.install4j5} -Djira.installer.signinstaller=true -Dsigncode.keystore.password=${bamboo.sign.password} -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true -e',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}" MAVEN_OPTS="-Xmx2048m -Xms128m -XX:MaxPermSize=512m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true'
    )
}

deployNightlyTestsPoms(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploy Nightly Tests POMs',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml clean deploy -B -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true -pl jira-nightly-tests -am -Drelease-func-tests',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms128m -XX:MaxPermSize=256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            workingSubDirectory: 'jira-distribution'
    )
}

unix32Bit(['repoName', 'jdk']) {
    job(name: '32 bit Unix Installer 01', key: 'UNIXINST32', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'All Logs Go To Heaven', location: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/target', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'installer log', location: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/target/jira_home/.install4j', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/target/jira_home/log/', pattern: '*.log', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runTestsAgainsUnix32BitInstaller(jdk: '#jdk')
    }
}

runTestsAgainsUnix32BitInstaller(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run tests against Unix 32b installer',
            goal: '-f jira-distribution/jira-nightly-tests/pom.xml clean verify -B -Datlassian.test.suite.batch=6 -Datlassian.test.suite.numbatches=50 -pl jira-nightly-installer -Dinstaller.bitness.classifier=x32 -am',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}

windows32Bit(['repoName', 'jdk']) {
    job(name: '32 bit Windows Installer 01', key: 'WINTINST32', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireWindows()

        artifactDefinition(name: 'buildlogs', location: '', pattern: '**/buildlogs/**', shared: 'false')
        artifactDefinition(name: 'Failsafe Reports', location: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/target/failsafe-reports', pattern: '*', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runTestsAgainsWindows32BitInstaller(jdk: '#jdk')
        cleanJiraInstaller()
    }
}

runTestsAgainsWindows32BitInstaller(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run tests against Windows 32b installer',
            goal: '-e -f jira-distribution/jira-nightly-tests/pom.xml clean verify -Datlassian.test.suite.numbatches=50 -Datlassian.test.suite.batch=1 -pl jira-nightly-installer -Dinstaller.bitness.classifier=x32 -am -B',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}

cleanJiraInstaller() {
    task(
            type: 'script',
            description: 'JIRA Installer Cleanup',
            script: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/windowsCleanup.bat',
            final: 'true'
    )
}

unix64Bit(['repoName', 'jdk']) {
    job(name: '64 bit Unix Installer 01', key: 'UNIXINST64', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'buildlogs', location: '', pattern: '**/buildlogs/**', shared: 'false')
        artifactDefinition(name: 'installer log', location: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/target/jira_home/.install4j', pattern: '*.log', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-installer/target/jira_home/log/', pattern: '*.log', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runTestsAgainsUnix64BitInstaller(jdk: '#jdk')
    }
}

runTestsAgainsUnix64BitInstaller(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run tests agains Unix 64b installer',
            goal: '-f jira-distribution/jira-nightly-tests/pom.xml clean verify -B -Datlassian.test.suite.batch=1 -Datlassian.test.suite.numbatches=50 -pl jira-nightly-installer -Dinstaller.bitness.classifier=x64 -am',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}

windows64Bit(['repoName', 'jdk']) {
    job(name: '64 bit Windows Installer 01', key: 'WINTINST64', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireWindows()

        artifactDefinition(name: 'buildlogs', location: 'buildlogs', pattern: '*.*', shared: 'false')
        artifactDefinition(name: 'Failsafe Reports', location: 'jira-distribution/jira-nightly-tests/target/failsafe-reports', pattern: '*', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runTestsAgainsWindows64BitInstaller(jdk: '#jdk')
        cleanJiraInstaller()
    }
}

runTestsAgainsWindows64BitInstaller(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run tests against Windows 64b installer',
            goal: '-f jira-distribution/jira-nightly-tests/pom.xml clean verify -B -Datlassian.test.suite.numbatches=50 -Datlassian.test.suite.batch=1 -pl jira-nightly-installer -Dinstaller.bitness.classifier=x64 -am',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}

buildSourceWarDistro(['repoName', 'jdk']) {
    job(name: 'Build Source Distribution WAR', key: 'DEPLOYSOURCEWAR', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'JIRA logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-source/target/', pattern: '**/*.log', shared: 'false')
        artifactDefinition(name: 'Tomcat logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-source/target/', pattern: '**/*.out', shared: 'false')
        artifactDefinition(name: 'Webapp Distribution Built By Source', location: 'jira-distribution/jira-nightly-tests/jira-nightly-source/target/', pattern: '**/jira-webapp-dist*.war', shared: 'true')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runWarBuild(jdk: '#jdk')
        passWarArtifact()
    }
}

runWarBuild(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build WAR distro',
            goal: 'verify -B -DskipTests=true -pl jira-distribution/jira-nightly-tests/jira-nightly-source -Pdistribution -Drelease-func-tests',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'false'
    )
}

passWarArtifact() {
    task(
            type: 'script',
            description: 'Copy source distro generated war for artifact passing',
            scriptBody: '''
cp jira-distribution/jira-nightly-tests/jira-nightly-source/target/atlassian-jira-*-source/jira-project/jira-distribution/jira-webapp-dist/target/jira-webapp-dist*.war jira-distribution/jira-nightly-tests/jira-nightly-source/target'''
    )
}

functTestsAgainstWarDitro(['repoName', 'jdk']) {
    job(name: 'Func Test WAR Batch 01', key: 'WARBATCH', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'JIRA Logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-war/target/jira_home/log/', pattern: '*.log', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runFuncTestsAgainstWarDistro(jdk: '#jdk')
    }
}

runFuncTestsAgainstWarDistro(['jdk']) {
    task(
            type: 'maven2',
            description: 'Run func tests',
            goal: '-B -f jira-distribution/jira-nightly-tests/pom.xml clean verify -Datlassian.test.suite.batch=1 -Datlassian.test.suite.numbatches=${bamboo.batch.max} -pl jira-nightly-war -am',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}
