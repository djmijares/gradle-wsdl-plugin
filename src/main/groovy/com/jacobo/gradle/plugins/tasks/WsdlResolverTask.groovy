package com.jacobo.gradle.plugins.tasks

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.api.tasks.TaskAction

import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.DefaultTask

import org.gradle.api.GradleException

import com.jacobo.gradle.plugins.model.WsdlWarRelativePathResolver
import com.jacobo.gradle.plugins.model.WsdlDependencyResolver

/**
 * Resolve all wsdl dependencies, copy dependencies into build directory
 * @author djmijares
 * Created: Mon Jan 07 18:08:42 EST 2013
 */
class WsdlResolverTask extends DefaultTask {
    static final Logger log = Logging.getLogger(WsdlResolverTask.class)

    final WsdlWarRelativePathResolver wrpr = new WsdlWarRelativePathResolver()

    @Input
    File rootDir

    @OutputDirectory
    File resolvedWsdlDir

    @OutputDirectory
    File resolvedSchemaDir

    @OutputDirectory
    File resolvedWebServicesDir

    @TaskAction
    void resolveWsdlDocumentDependencies() {

        log.info("resolving all relative paths")

        def relativePathWarResolution = wrpr.resolveRelativePathsToWar(getRootDir(), dependencyList)

        log.debug("relativePathWarResolution list has a size of {} and contains {}", relativePathWarResolution.size(), relativePathWarResolution)

        log.debug("setting the extension point for war task to use with {}", relativePathWarResolution)
        project.wsdl.resolved = relativePathWarResolution

        log.debug("copying all web service dependent documents into {}", getResolvedWebServicesDir())
        relativePathWarResolution.each { resolved ->
            log.debug("resolving from {} into {} and including these file(s) {}", resolved.from, resolved.into, resolved.resolvedFiles.name)
            log.debug("copying into {}", getResolvedWebServicesDir().path + File.separator + resolved.into)
            ant.copy(toDir: getResolvedWebServicesDir().path + File.separator + resolved.into) {
                fileset(dir: resolved.from) {
                    resolved.resolvedFiles.each { file ->
                        include(name: file.name)
                    }
                }
            }
        }
    }

} 