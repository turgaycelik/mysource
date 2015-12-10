notifications() {
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
}

sourceDistribution(['repoName', 'jdk']) {
    job(name: 'Default Job', key: 'JOB1', description: '') {
        requireLocalLinux()
        requireMaven2()
        requireJdk(jdk: '#jdk')

        artifactDefinition(name: 'Log files', location: '', pattern: '**/catalina.out', shared: 'false')
        artifactDefinition(name: 'Source-Tarball', location: '/opt/bamboo-agent/.m2/repository/com/atlassian/jira/jira-source-distribution/6.3-SNAPSHOT/', pattern: '**/*.*', shared: 'false')

        miscellaneousConfiguration(cleanupWorkdirAfterBuild: 'true')

        cleanCheckout(repoName: '#repoName')
        cleanMavenCache()
        buildSourceDistro(jdk: '#jdk')
        buildSourceDistroWar(jdk: '#jdk')
        cleanMavenCacheAsFinal()
    }
}

buildSourceDistro(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build Source Distro',
            goal: '-Pdistribution -pl jira-distribution/jira-source-distribution/ -am -B clean install -Dmaven.test.func.skip=true -Dmaven.test.selenium.skip=true',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=128m -Xms256m"'
    )
}

buildSourceDistroWar(['jdk']) {
    task(
            type: 'maven2',
            description: 'Build Source Distro WAR',
            goal: 'verify -e -B -DskipTests=true -pl jira-distribution/jira-nightly-tests/jira-nightly-source -Pdistribution -Drelease-func-tests',
            buildJdk: '#jdk',
            mavenExecutable: 'Maven 2.1',
            environmentVariables: 'MAVEN_OPTS="-Xmx1300m -XX:MaxPermSize=128m -Xms256m"'
    )
}
