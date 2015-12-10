// Depends on:
//     release/shortcuts.nightly.groovy
//     release/shortcuts.release.groovy
//     shortcuts.common.groovy

plan(key: 'NRB', name: 'Nightly Release Build', description: '', enabled: 'true') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    releaseProject()
    releaseLabels()

    repository(name: repoName)
    onceADay(time: '20:00')
    notifications()
    globalVariables()
    miscellaneousConfig()

    stage(name: 'Build JIRA Project', manual: 'false') {
        buildAllJira(repoName: repoName, jdk: jdk)
    }

    stage(name: 'Installer Dist', manual: 'false') {
        unix32Bit(repoName: repoName, jdk: jdk)
        windows32Bit(repoName: repoName, jdk: jdk)
        unix64Bit(repoName: repoName, jdk: jdk)
        windows64Bit(repoName: repoName, jdk: jdk)
        buildSourceWarDistro(repoName: repoName, jdk: jdk)
        functTestsAgainstWarDitro(repoName: repoName, jdk: jdk)
    }
}
