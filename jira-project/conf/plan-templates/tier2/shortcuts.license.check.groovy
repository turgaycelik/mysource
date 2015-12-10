notifications() {
    notification(type: 'Failed Builds and First Successful', recipient: 'responsible')
    notification(type: 'Failed Builds and First Successful', recipient: 'watchers')
}

licenseCheck(['repoName', 'jdk']) {
    job(name: 'Default Job', key: 'JOB1', description: '') {
        requireLinux()
        requireMaven30()
        requireJdk(jdk: '#jdk')
        requirePython()

        artifactDefinition(name: 'bom-csv.diff', location: '', pattern: '**/bom-csv.diff', shared: 'false')
        artifactDefinition(name: 'license-maven-plugin-bom.log', location: '', pattern: '**/license-maven-plugin-bom.log', shared: 'false')
        artifactDefinition(name: 'license-maven-plugin-files.log', location: '', pattern: '**/license-maven-plugin-files.log', shared: 'false')

        checkout(repoName: '#repoName', forceClean: 'false')
        runLicenseCheck()
    }
}

runLicenseCheck() {
    task(
            type: 'script',
            description: 'Running the maven license plugin',
            script: './bin/ci-third-party-licensing.sh',
            argument: 'verify',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"'
    )
}