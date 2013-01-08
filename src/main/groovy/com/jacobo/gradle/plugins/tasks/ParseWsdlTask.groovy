package com.jacobo.gradle.plugins.tasks

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory

/**
 * @author djmijares
 * Created: Mon Jan 07 18:08:42 EST 2013
 */
class ParseWsdlTask extends DefaultTask { 
  static final Logger log = Logging.getLogger(ParseWsdlTask.class)

  final WsdlExtension extension = project.extensions.wsdl

  @InputFile
  File wsdlPath = extension.wsdlPath
  
  @OutputDirectory
  String destinationDirectory = extension.sourceDestinationDirectory

  @TaskAction
  void parseWsdl() { 
    ant.taskdef (name : 'wsimport', classname: 'com.sun.tools.ws.ant.WsImport', classpath: project.configurations[WsdlPlugin.WSDL_CONFIGURATION_NAME].asPath)

    ant.wsimport (
    wsdl: wsdlPath,
    verbose: extension.verbose,
    sourcedestdir : destinationDirectory,
    keep: extension.keep,
    wsdlLocation : extension.wsdlLocation,
    xnocompile : extension.xnocompile,
    if(extension.target) { target : extension.target, } //this may not work in the end
    fork : extension.fork) { 
      episode.each { episode ->
	binding(dir : extension.episodeDirectory.path, includes : "${episode}.episode")
      }
    }
  }
}