notifications() {
    notification(type: 'Comment Added', recipient: 'responsible')
    notification(type: 'All Builds Completed', recipient: 'stash')
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

globalVariables() {
    variable(key: 'deploySourceJars', value: 'true')
    variable(key: 'isrelease', value: 'false')

}

deployMavenArtifacts(['repoName', 'jdk']) {
    job(name: 'Deploy Artifacts', key: 'DEPLOY', description: '') {
        requireLocalLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        deployJiraProject(jdk: '#jdk')
        deployJiraDistributions(jdk: '#jdk')
        deployNightlyTestsPoms(jdk: '#jdk')
        cleanMavenCache()
    }
}

deployJiraProject(['jdk']) {
    task(
            type: 'script',
            description: 'Deploy JIRA Project',
            script: 'bin/builds/deploy-jira-project.sh',
            argument: '${bamboo.deploySourceJars} ${bamboo.sign.password} ${bamboo.isrelease}',
            environmentVariables: 'M2_HOME="${bamboo.capability.system.builder.mvn2.Maven 2.1}" JAVA_HOME="${bamboo.capability.system.jdk.#jdk}"'
    )
}

deployJiraDistributions(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploy JIRA Distributions',
            goal: '-B deploy -pl jira-standalone-distribution,jira-war-distribution,jira-installer-distribution -am -Pdistribution -Pdeploy-rest-apidocs -DskipTests=true -Dinstall4j.home=${bamboo.capability.install4j5} -Djira.installer.signinstaller=true -Dsigncode.keystore.password=${bamboo.sign.password}',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx1536m -Xms256m -XX:MaxPermSize=256m"',
            workingSubDirectory: 'jira-distribution'
    )
}

deployNightlyTestsPoms(['jdk']) {
    task(
            type: 'maven2',
            description: 'Deploy Nightly Tests POMs',
            goal: '-s /opt/bamboo-agent/.m2/jira-deploy-settings.xml clean deploy -B -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true -pl jira-nightly-tests -am -Drelease-func-tests',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -Xms128m -XX:MaxPermSize=256m"',
            workingSubDirectory: 'jira-distribution'
    )
}
