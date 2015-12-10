notifications() {
    notification(type: 'Comment Added', recipient: 'hipchat', apiKey: '${bamboo.atlassian.hipchat.apikey.password}', room: 'JBAC Dev', notify: 'true')
}

globalVariables() {
    releaseGlobalVariables()
    variable(key: 'AUTHFILE', value: '/opt/bamboo-agent/jira-release-svn.xml')
    variable(key: 'batch.max', value: '10')
    variable(key: 'branch.url', value: 'TODO')
    variable(key: 'deploySourceJars', value: 'true')
    variable(key: 'goal', value: 'wac-eap')
    variable(key: 'isrelease', value: 'true')
    variable(key: 'jira.milestone.point.release.branch', value: 'TODO')
    variable(key: 'jira.milestone.point.release.version', value: 'TODO')
    variable(key: 'jira.promote.version', value: 'TODO')
    variable(key: 'planKey', value: 'TODO')
    variable(key: 'promote.buildNumber', value: 'TODO')
    variable(key: 'promote.planKey', value: 'TODO')
}

miscellaneousConfig() {
    planMiscellaneous(){
        concurrentBuilds(enabled:"true", max:"2")
        sandbox(enabled:"true", automaticPromotion:"false")
    }
}

buildAndDeployDocs(['repoName', 'jdk']) {
    job(name: 'Build and Fancy Docs', key: 'DFD', description: '') {
        requireJdk(jdk: '#jdk')
        requireMaven2()
        requireLocalLinux()

        miscellaneousConfiguration() {
            jobHungDetectionCriteria(buildTimeMultiplier: '100', logQuietTime: '1000', buildQueueTimeout: '100')
        }

        checkout(repoName: '#repoName', forceClean: 'false')
        cleanMavenCache()
        buildJavadoc(jdk: '#jdk')
        uploadJavadoc(jdk: '#jdk')
    }
}

buildJavadoc(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build javadoc for developer.atlassian.com',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml -B install javadoc:aggregate-jar -Pall-javadoc -Pcreate-rest-apidocs -Djira.generateDocs -B -DskipTests=true -P-dependency-tracking',
            environmentVariables: 'MAVEN_OPTS="-Xms512m -Xmx2048m -XX:MaxPermSize=512m" M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1'
    )
}

uploadJavadoc(['jdk']) {
    task(
            type: 'script',
            description: 'Upload javadoc to developer.atlassian.com',
            script: 'bin/jira-fancy-docs-upload.sh',
            argument: '${bamboo.jira.6.3.release.version}'
    )
}
