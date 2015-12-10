finalReleaseNotifications() {
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JIRA Dev', notify: 'true')
    notification(type: 'Failed Jobs and First Successful', recipient: 'responsible')
}

finalReleaseGlobalVariables() {
    releaseGlobalVariables()
    variable(key: 'AUTHFILE', value: '/opt/bamboo-agent/credentials.xml')
    variable(key: 'batch.max', value: '20')
    variable(key: 'confluence.server', value: 'https://confluence.atlassian.com')
    variable(key: 'deploySourceJars', value: 'true')
    variable(key: 'goal', value: 'wac-release')
    variable(key: 'isrelease', value: 'true')
    variable(key: 'maven.opts', value: '-Xmx2048m -Xms128m -XX:MaxPermSize=512m')
    variable(key: 'old.stable.version', value: '6.2')
    variable(key: 'parent.page.title', value: 'JIRA 6.3 Release Notes')
    variable(key: 'planKey', value: 'J63STABLEPT-RELEASE')
    variable(key: 'release.notes.current.space', value: 'JIRA063')
    variable(key: 'release.notes.dev.space', value: 'JIRA')   
}

finalReleaseMiscellaneousConfig() {
    planMiscellaneous(){
        concurrentBuilds(enabled:"true", max:"3")
        sandbox(enabled:"true", automaticPromotion:"false")
    }
}

buildAllJiraArtifacts(['repoName', 'jdk']) {
    job(name: 'Build ALL JIRA', key: 'JOB1', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'JIRA Source Distribution', location: 'jira-distribution/jira-source-distribution/target', pattern: '*.tar.gz', shared: 'true')
        artifactDefinition(name: 'Release Report', location: '', pattern: '${bamboo.jira.fe.release.report.filename}', shared: 'true')

        // most of these tasks are defined on shortcuts.nightly.groovy
        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        generateBomAndDownloadLicenses()
        deployJiraProject(jdk: '#jdk')
        deployNightlyTestsPoms(jdk: '#jdk')
        cleanMavenCache()
        createReleaseReport()
    }
}

deployJiraProject(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploy JIRA Project',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml -B clean deploy -Pcreate-rest-apidocs,deploy-source-jars,dependency-tracking,distribution,installer -Pthird-party-licensing -Djira.generateDocs -Djira.screenshotapplet.signscreenshotapplet=true -Datlassian.keystore.storepass=${bamboo.atlassian.keystore.password} -DskipTests -Dinstall4j.home=${bamboo.capability.install4j5} -Djira.installer.signinstaller=true -Dsigncode.keystore.password=${bamboo.atlassian.keystore.password}',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.JDK 1.6}" MAVEN_OPTS="-Xmx2048m -Xms128m -XX:MaxPermSize=512m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'false'
    )
}

deployNightlyTestsPoms(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploy Nightly Tests POMs',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml deploy -B -Pthird-party-licensing -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true -pl jira-nightly-tests -am -Drelease-func-tests',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms128m -XX:MaxPermSize=256m"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            workingSubDirectory: 'jira-distribution'
    )
}

createReleaseReport() {
    task(
            type: 'script',
            description: 'Create release report',
            script: 'bin/create-release-report.sh',
            argument: '${bamboo.jira.fe.release.report.repo} ${bamboo.jira.fe.stable.release.report.start.branch} ${bamboo.jira.6.3.release.branch} ${bamboo.jira.fe.release.report.filename}'
    )
}

verifyNoSnapshots(['repoName', 'jdk']) {
    job(name: 'Verify No Snapshots', key: 'VNS', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLinux()

        checkout(repoName: '#repoName', forceClean: 'false')
        validateRelease(jdk: '#jdk')
    }
}

validateRelease(['jdk']) {
    task(
            type: 'maven2',
            description: 'Validate release',
            goal: 'validate -B -Prelease',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1'
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
            goal: '-X -f jira-distribution/jira-nightly-tests/pom.xml clean verify -Datlassian.test.suite.numbatches=50 -Datlassian.test.suite.batch=1 -pl jira-nightly-installer -Dinstaller.bitness.classifier=x32 -am -B',
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
            goal: '-X -f jira-distribution/jira-nightly-tests/pom.xml clean verify -B -Datlassian.test.suite.numbatches=50 -Datlassian.test.suite.batch=1 -pl jira-nightly-installer -Dinstaller.bitness.classifier=x64 -am',
            environmentVariables: 'MAVEN_OPTS="-Xmx2304m -Xms128m -XX:MaxPermSize=768m"',
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

sourceDistroFuncTests(['repoName', 'jdk']) {
    job(name: 'Artifact PAssing - Func Test Source Batch 1', key: 'SOURCEARTIFACTBATCH01', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'Cargo Logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-source/target', pattern: '**/logs/**/*.*', shared: 'false')
        artifactDefinition(name: 'JIRA Logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-source/target/jira_home/log', pattern: '**/*.*', shared: 'false')
        artifactDefinition(name: 'More Logs', location: 'jira-distribution/jira-nightly-tests/jira-nightly-source/target/test-reports', pattern: '**/*.*', shared: 'false')

        artifactSubscription(name: 'JIRA Source Distribution', destination: 'jira-distribution/jira-nightly-tests/jira-nightly-source/jira-prebuilt-artifacts')
        artifactSubscription(name: 'Webapp Distribution Built By Source', destination: 'jira-distribution/jira-nightly-tests/jira-nightly-source/jira-prebuilt-artifacts')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runDistroFuncTests(jdk: '#jdk')
    }
}

runDistroFuncTests(['jdk']) {
    task(
            type: 'maven2',
            description: 'Runs functional tests agains the distro',
            goal: '-f jira-distribution/pom.xml clean verify -Datlassian.test.suite.batch=1 -Datlassian.test.suite.numbatches=100 -pl jira-nightly-tests/jira-nightly-source -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true -Dmaven.test.unit.skip=true -Pdistribution -Drelease-func-tests -Djira.use.artifact.passing=true',
            environmentVariables: 'MAVEN_OPTS="${bamboo.maven.opts}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            hasTests: 'true',
            testDirectory: '**/target/failsafe-reports/*.xml'
    )
}

unpublishedVersionToMarketPlace(['repoName', 'jdk']) {
    job(name: 'Unpublished version to MPAC', key: 'PREPMPAC', description: '', enabled: 'false') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        pacUnpublishedEntry(jdk: '#jdk')
    }
}

pacUnpublishedEntry(['jdk']) {
    task(
            type: 'script',
            description: 'PAC JIRA Unpublished Version Entry',
            script: 'bin/jira-pac-unpublished-entry.sh',
            argument: '${bamboo.jira.6.1.release.version} ${bamboo.jira.6.1.branch.raw} pac ${bamboo.AUTHFILE}',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=128m -Xms256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

promoteSandboxToMaven(['repoName', 'jdk']) {
    job(name: 'Promote Sandbox to Maven', key: 'PROMOTESANDBOX', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runSandboxPromotion(jdk: '#jdk')
    }
}

runSandboxPromotion(['jdk']) {
    task(
            type: 'script',
            description: 'Sandbox Promote',
            script: 'bin/jira-promote-sandbox.sh',
            argument: '${bamboo.buildNumber} ${bamboo.planKey}',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

uploadDocsToWac(['repoName', 'jdk']) {
    job(name: 'Upload Docs to WAC', key: 'DOCS2WAC', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'Deploy Log', location: '.', pattern: '*.log', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runDocsUpload(jdk: '#jdk')
        cleanMavenCacheAsFinal()
    }
}

runDocsUpload(['jdk']) {
    task(
            type: 'script',
            description: 'Deploy JIRA Docs to WAC',
            script: 'bin/jira-upload-docs.sh',
            argument: '${bamboo.jira.6.3.release.version}',
            environmentVariables: 'MAVEN_OPTS="-Xmx2048m -Xms128m -XX:MaxPermSize=512m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

uploadSandboxToWac(['repoName', 'jdk']) {
    job(name: 'Upload Sandbox to WAC', key: 'SANDBOXWAC', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'Deploy Log', location: '.', pattern: '*.log', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runSandboxUploadToWac(jdk: '#jdk')
    }
}

runSandboxUploadToWac(['jdk']) {
    task(
            type: 'script',
            description: 'Deploy JIRA Bits to WAC',
            script: 'bin/jira-upload-sandbox-wac.sh',
            argument: '${bamboo.jira.6.3.release.version} ${bamboo.planKey} ${bamboo.buildNumber}',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

uploadSourcesToWac(['repoName', 'jdk']) {
    job(name: 'Upload Sources to WAC', key: 'SRC2WAC', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'Deploy Log', location: '.', pattern: '*.log', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        runSourcesUploadToWac(jdk: '#jdk')
    }
}

runSourcesUploadToWac(['jdk']) {
    task(
            type: 'script',
            description: 'Deploy JIRA Sources to WAC',
            script: 'bin/jira-upload-sandbox-sources.sh',
            argument: '${bamboo.jira.6.3.release.version} ${bamboo.planKey} ${bamboo.buildNumber}',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

customerMpacReleaseEntry(['repoName', 'jdk']) {
    job(name: 'Customer MPAC Release Entry', key: 'PACENTRY', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        checkout(repoName: '#repoName', forceClean: 'false')
        createJiraReleaseVersionOnMpac(jdk: '#jdk')
    }
}

createJiraReleaseVersionOnMpac(['jdk']) {
    task(
            type: 'maven3',
            description: 'MPAC Jira Release Version Entry',
            goal: 'clean --non-recursive com.atlassian.maven.plugins:product-deployment-maven-plugin:3.3-rc1:pac -Dcredentials.pac.serverid=mpac -Dpac.publish=true -Dproduct.buildNumber.property=jira.build.number -Dproduct.tag=${bamboo.jira.6.3.branch.raw}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0'
    )
}

customerWacReleaseEntry(['repoName', 'jdk']) {
    job(name: 'Customer WAC Release Entry', key: 'RELEASEWAC', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        checkout(repoName: '#repoName', forceClean: 'false')
        createJiraReleaseVersionOnWac(jdk: '#jdk')
    }
}

createJiraReleaseVersionOnWac(['jdk']) {
    task(
            type: 'maven3',
            description: 'Release to Customer WAC',
            goal: 'clean --non-recursive com.atlassian.maven.plugins:product-deployment-maven-plugin:3.3-rc1:wac-release -Dcredentials.aac.serverid=wac-admin -Dproduct.tag=${bamboo.jira.6.3.branch.raw} -Dproduct.buildNumber.property=jira.build.number',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0'
    )
}

tacReleaseEntryAndImport(['repoName', 'jdk']) {
    job(name: 'TAC Release Entry and Import', key: 'TACIMPORT', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        checkout(repoName: '#repoName', forceClean: 'false')
        createJiraReleaseVersionOnTac(jdk: '#jdk')
    }
}

createJiraReleaseVersionOnTac(['jdk']) {
    task(
            type: 'maven3',
            description: 'Create Jira Release in TAC',
            goal: 'clean --non-recursive com.atlassian.maven.plugins:product-deployment-maven-plugin:3.3-rc1:tac-import -Dcredentials.tac.serverid=tac-admin',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0'
    )
}

tac2PacAndMavenDeploy(['repoName', 'jdk']) {
    job(name: 'TAC to PAC and Maven Deployment', key: 'TACTOPAC', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven30()
        requireLocalLinux()

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        checkout(repoName: '#repoName', forceClean: 'false')
        publishTacArtifactsToPacAndMaven(jdk: '#jdk')
    }
}

publishTacArtifactsToPacAndMaven(['jdk']) {
    task(
            type: 'maven3',
            description: 'Publish TAC artifacts to PAC and Maven',
            goal: 'clean --non-recursive com.atlassian.maven.plugins:product-deployment-maven-plugin:3.3-rc1:tac-publish -Dcredentials.tac.serverid=tac-admin',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 3.0'
    )
}

deployDocsToDac(['repoName', 'jdk']) {
    job(name: 'Build and deploy developer.atlassian.com fancy docs', key: 'BUILDDOCS', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        artifactDefinition(name: 'JIRA Source Distribution', location: 'jira-distribution/jira-source-distribution/target', pattern: '*.tar.gz', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true') {
            jobHungDetectionCriteria(buildTimeMultiplier: '5', logQuietTime: '40', buildQueueTimeout: '60')
        }

        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        buildJavaDocs(jdk: '#jdk')
        uploadJavaDocs()
        cleanMavenCacheAsFinal()
    }
}

buildJavaDocs(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build javadoc for developer.atlassian.com',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml -B install javadoc:aggregate-jar -Pall-javadoc -Pcreate-rest-apidocs -Djira.generateDocs -B -DskipTests=true -P-dependency-tracking',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1'
    )
}

uploadJavaDocs() {
    task(
            type: 'script',
            description: 'Fast upload fancy docs',
            script: 'bin/jira-fancy-docs-upload.sh',
            argument: '${bamboo.jira.6.3.release.version}'
    )
}

docsUpgrade(['repoName']) {
    job(name: 'Docs Upgrade (Disable for Milestones)', key: 'DOCUP', description: '') {
        requireLocalLinux()

        checkout(repoName: '#repoName', forceClean: 'false')
        toggleLinksOnAtlassian04()
    }
}

toggleLinksOnAtlassian04() {
    task(
            type: 'script',
            description: 'Toggle links on atlassian04',
            script: 'bin/jira-docs-change-latestlink.sh',
            argument: '${bamboo.jira.6.3.release.version}'
    )
}

generateReleaseDocsCurrentSpace(['repoName']) {
    job(name: 'Generate Release Docs - Current Space', key: 'GRDCS', description: '') {
        requireLocalLinux()

        checkout(repoName: '#repoName', forceClean: 'false')
        createMinorReleaseDocsInCac()
    }
}

createMinorReleaseDocsInCac() {
    task(
            type: 'script',
            description: 'Create New Minor Release DOCS in CAC',
            scriptBody: 'python src/jira_release_notes.py --credentials_file="/opt/bamboo-agent/credentials.xml" --confluence_server="${bamboo.confluence.server}" --space="${bamboo.release.notes.current.space}" --release_version="${bamboo.jira.6.3.release.version}" --old_version="${bamboo.old.stable.version}" --test_title="Automated Release Notes" --final_title="${bamboo.parent.page.title}"'
    )
}

generateReleaseDocsDevSpace(['repoName']) {
    job(name: 'Generate Release Docs - Dev Space', key: 'GRDDS', description: 'Generates the Release Notes in the Development Space') {
        requireLocalLinux()

        checkout(repoName: '#repoName', forceClean: 'false')
        createBoilerPlateMinorReleaseNotesInConfluence()
    }
}

createBoilerPlateMinorReleaseNotesInConfluence() {
    task(
            type: 'script',
            description: 'Create boilerplate minor Release/Upgrade Notes in Confluence',
            scriptBody: 'python src/jira_release_notes.py --credentials_file="/opt/bamboo-agent/credentials.xml" --confluence_server="${bamboo.confluence.server}" --space="${bamboo.release.notes.dev.space}" --release_version="${bamboo.jira.6.3.release.version}" --old_version="${bamboo.old.stable.version}" --test_title="Automated Release Notes" --final_title="${bamboo.parent.page.title}"'
    )
}

tagJira(['repoName']) {
    job(name: 'Tag JIRA', key: 'TAGJIRA', description: '') {
        cleanCheckout(repoName: '#repoName')
        runTagJiraScript()
    }
}

runTagJiraScript() {
    task(
            type: 'script',
            description: 'tag',
            scriptBody: '''
            echo "Git remotes:"
git remote -vv

if [ `git remote | grep "origin$" | wc -l` -ne 0 ]; then
    echo "origin is already defined, deleting it"
    git remote rm origin
fi

git remote add origin ssh://git@stash.atlassian.com:7997/jira/jira.git

git checkout ${bamboo.jira.6.3.release.branch}
git tag -a -m "Tagging JIRA ${bamboo.jira.6.3.tag}" ${bamboo.jira.6.3.tag}
git push --tags origin''',
            argument: '"Tagging JIRA ${bamboo.jira.6.3.tag}" ${bamboo.jira.6.3.tag}'
    )
}

uploadOpenSourceLibsSrc(['repoName', 'jdk']) {
    job(name: 'Upload Open Source Libs Sources', key: 'OSLIBS', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()
        cleanCheckout(repoName: '#repoName')        
        task(
            type: 'maven2',
            description: 'Upload sources of open source libraries',
            goal: 'license:open-source-distro -P\'distribution,!build-source-distribution\' -Dmaven.test.skip=true',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1'
        )
    }
}
