// Depends on:
//     release/shortcuts.docs.groovy
//     release/shortcuts.release.groovy
//     shortcuts.common.groovy

plan(key: 'PFD', name: 'Publish Fancy Docs', description: '', enabled: 'false') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x Release Variable Branch stash'

    releaseProject()
    releaseLabels()

    repository(name: repoName)
    notifications()
    globalVariables()
    miscellaneousConfig()

    stage(name: 'Build and Deploy Fancy Docs', manual: 'false') {
        buildAndDeployDocs(repoName: repoName, jdk: jdk)
    }
}
