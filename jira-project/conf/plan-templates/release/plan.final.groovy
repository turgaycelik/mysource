// Depends on:
//     release/shortcuts.final.groovy
//     release/shortcuts.release.groovy
//     shortcuts.common.groovy

plan(key: 'RELEASE', name: 'Final Release Build', description: '', enabled: 'true') {
    def jdk6 = 'JDK 1.6'
    def jdk7 = 'JDK 1.7'
    def defaultRepo = 'JIRA 6.3.x Release Variable Branch stash'
    def stableReleaseRepo = 'JIRA Stable Release Variable Branch stash'
    def tacImporterRepo = 'TAC Importer'
    def releaseNotesRepo = 'Release Notes Automation Scripts'

    releaseProject()
    releaseLabels()

    repository(name: defaultRepo)
    repository(name: stableReleaseRepo)
    repository(name: tacImporterRepo)
    repository(name: releaseNotesRepo)
    finalReleaseNotifications()
    finalReleaseGlobalVariables()
    finalReleaseMiscellaneousConfig()

    stage(name: 'Build JIRA Project', manual: 'false') {
        buildAllJiraArtifacts(repoName: defaultRepo, jdk: jdk6)
    }

    stage(name: 'Verify No Snapshots', manual: 'false') {
        verifyNoSnapshots(repoName: defaultRepo, jdk: jdk7)
    }

    stage(name: 'Installer Dist', manual: 'false') {
        unix32Bit(repoName: defaultRepo, jdk: jdk6)
        windows32Bit(repoName: defaultRepo, jdk: jdk6)
        unix64Bit(repoName: defaultRepo, jdk: jdk6)
        windows64Bit(repoName: defaultRepo, jdk: jdk6)
        buildSourceWarDistro(repoName: defaultRepo, jdk: jdk6)
        functTestsAgainstWarDitro(repoName: defaultRepo, jdk: jdk6)
    }

    stage(name: 'Source Distro Func Tests', manual: 'false') {
        sourceDistroFuncTests(repoName: defaultRepo, jdk: jdk6)
    }

    stage(name: 'Prepare MPAC', manual: 'true') {
        unpublishedVersionToMarketPlace(repoName: defaultRepo, jdk: jdk6)
    }

    stage(name: 'Sandbox Promote to Maven', manual: 'true') {
        promoteSandboxToMaven(repoName: defaultRepo, jdk: jdk6)
    }

    stage(name: 'WAC JIRA Upload', manual: 'true') {
        uploadDocsToWac(repoName: defaultRepo, jdk: jdk6)
        uploadSandboxToWac(repoName: defaultRepo, jdk: jdk6)
        uploadSourcesToWac(repoName: defaultRepo, jdk: jdk6)
        uploadOpenSourceLibsSrc(repoName: defaultRepo, jdk: jdk7)
    }

    stage(name: 'WAC MPAC TAC Admin Tool Entry', manual: 'false') {
        customerMpacReleaseEntry(repoName: defaultRepo, jdk: jdk7)
        customerWacReleaseEntry(repoName: defaultRepo, jdk: jdk7)
        tacReleaseEntryAndImport(repoName: defaultRepo, jdk: jdk7)
    }

    stage(name: 'TAC to MPAC and Maven deployment', manual: 'false') {
        tac2PacAndMavenDeploy(repoName: defaultRepo, jdk: jdk7)
    }

    stage(name: 'Documentation and Translations', manual: 'true') {
        deployDocsToDac(repoName: defaultRepo, jdk: jdk6)
        docsUpgrade(repoName: defaultRepo)
        generateReleaseDocsCurrentSpace(repoName: releaseNotesRepo)
        generateReleaseDocsDevSpace(repoName: releaseNotesRepo)
    }

    stage(name: 'Tag JIRA', manual: 'false') {
        tagJira(repoName: defaultRepo)
    }
}
