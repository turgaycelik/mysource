// Depends on:
//     release/shortcuts.cut.branch.groovy
//     release/shortcuts.release.groovy
//     shortcuts.common.groovy

plan(key: 'BRANCHJIRA', name: 'Branch Release', description: 'Cuts the Release Branch for a new stable JIRA release', enabled: 'true') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    releaseProject()
    releaseLabels()

    repository(name: repoName)
    notifications()
    globalVariables()

    stage(name: 'Create Release Branch', manual: 'false') {
        createReleaseBranch(repoName: repoName, jdk: jdk)
    }

    stage(name: 'Update Stable', manual: 'false') {
        prepareNextDevVersion(repoName: repoName, jdk: jdk)
    }
}
