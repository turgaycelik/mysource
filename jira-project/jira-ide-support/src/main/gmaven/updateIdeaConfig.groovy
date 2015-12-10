/**
 *
 * This Groovy script is executed using GMaven. See http://docs.codehaus.org/display/GMAVEN/Executing+Groovy+Code
 */

/**
 * Returns the configuration directory for a given version of IntelliJ IDEA.
 *
 * @param versionString a String specifying the IDEA version (e.g. IntelliJIDEA90, IntelliJIDEA10).
 * @return
 */
File getConfigDir(String versionString) {
    String userHome = session.executionProperties['user.home']
    String operatingSys = session.executionProperties['os.name']

    if (operatingSys.contains("windows")) {
        // e.g. C:\Users\<user_dir>\.IntelliJIdea60\config	user_dir is your account
        return new File("$userHome\\\\$versionString\\\\config")
    }
    else if (operatingSys.contains("Mac")) {
        // e.g. ~/Library/Preferences/IntelliJIDEA60
        return new File("$userHome/Library/Preferences/$versionString")
    }
    else {
        // e.g. ~/.IntelliJIdea60/config
        return new File("$userHome/.$versionString/config")
    }
}

File getCustomConfigDir() {
    String path = project.properties['idea.custom.config.dir.path']
    if (path) {
        log.info "Custom path for IDEA config is ${path}"
        return new File(path)
    } else {
        log.info "Custom path for IDEA config not configured"
        return null
    }
}

/**
 * Copies all the files in the given sub-directory of the ideaConfig directory into the IDEA configuration.
 *
 * @param subdir a String containing the IDEA subdirectory
 * @param todir the IDEA config dir
 */
void copySubdirTo(String subdir, String todir) {
    String baseDir = project['basedir']
    String fileSetDir = "$baseDir/src/main/resources/ideaConfig/$subdir"
    String targetDir = "$todir/$subdir"

    log.info "\tCopying $fileSetDir to $targetDir"

    ant.copy(todir: targetDir, overwrite: true) {
        fileset(dir: fileSetDir)
    }
}

void copyIdeaConfiguration(File configDir) {
    log.info "\t Updating IDEA templates in $configDir"

    copySubdirTo("templates", "$configDir.absolutePath")
    copySubdirTo("fileTemplates", "$configDir.absolutePath")
    copySubdirTo("codestyles", "$configDir.absolutePath")
}

boolean tryToUpdateIDEAConfig(String versionString) {
    File configDir = getConfigDir(versionString)
    log.info "\tLooking to update IDEA templates in $configDir"
    if (configDir.exists()) {
        copyIdeaConfiguration(configDir);
        log.info "\tUpdated IDEA templates in $configDir"
        return true;
    }
    return false;
}

File tryToUpdateIDEAConfigInCustomDir() {
    File configDir = getCustomConfigDir()
    if (!configDir || !configDir.exists()) {
        return null
    }
    log.info "\tLooking to update IDEA templates in $configDir"
    copyIdeaConfiguration(configDir);
    log.info "\tUpdated IDEA templates in $configDir"
    return configDir;
}

log.info "IDEA update config groovy script running.."

// try IDEA 9 through 13...
versionStrings = [ '90', '10', '11', '12', '13' ]
updatedVersions = versionStrings.inject([], { updatedVersions, version ->
    if (tryToUpdateIDEAConfig("IntelliJIdea" + version)) {
        return updatedVersions+version // accumulates results
    }

    return updatedVersions
})

updatedCustomDir = tryToUpdateIDEAConfigInCustomDir()

if (updatedVersions) {
    log.info("\tUpdated IDEA configuration for versions: ${updatedVersions.join(', ')}")
}
if (updatedCustomDir) {
    log.info("\tUpdated IDEA configuration in custom dir: ${updatedCustomDir.absolutePath}")
}
if (!updatedVersions && !updatedCustomDir) {
    log.info("\tUnable to find your IDEA config directory")
}

log.info "IDEA update config groovy script finished"
