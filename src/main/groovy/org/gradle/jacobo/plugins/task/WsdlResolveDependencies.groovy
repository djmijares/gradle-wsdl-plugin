package org.gradle.jacobo.plugins.task

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import org.gradle.jacobo.plugins.resolver.DependencyResolver

/**
 * Resolves WSDL dependencies starting with the WSDL file
 *
 * @author djmijares
 */
class WsdlResolveDependencies extends DefaultTask {
  static final Logger log = Logging.getLogger(WsdlResolveDependencies.class)

  /**
   * wsdl file (absolute path)
   */
  File wsdlFile

  /**
   * Resolves all wsdl Dependencies through XmlSlurping
   */
  DependencyResolver dependencyResolver

  @TaskAction
  void start() {
    def wsdlDependencies = getDependencyResolver().resolveDependencies(getWsdlFile())
    project.wsdl.wsdlDependencies = project.files(wsdlDependencies)
  }
}