package com.jacobo.gradle.plugins.tasks

import groovy.util.AntBuilder

import org.gradle.api.file.FileCollection

import spock.lang.Unroll

import com.jacobo.gradle.plugins.ProjectTaskSpecification
import com.jacobo.gradle.plugins.WsdlPlugin
import com.jacobo.gradle.plugins.ant.AntExecutor
import com.jacobo.gradle.plugins.convert.NameToFileConverter

class WsdlWsImportSpec extends ProjectTaskSpecification {

  def executor = Mock(AntExecutor)
  def someWsdl = new File("some-wsdl.wsdl")
  def projectDir = getFileFromResourcePath("/fake-project")
  
  def setup() {
    projectAtDir(projectDir, "wsdl")
    task = project.tasks[WsdlPlugin.WSIMPORT_TASK_NAME] as WsdlWsImport
  }

  @Unroll
  def "execute wsimport on the wsdl file, binding episode files '#episodeFiles'"() {
    given: "set up the classpath and task variables"
    def classpath = project.configurations[WsdlPlugin.WSDL_CONFIGURATION_NAME]
    task.with { 
      antExecutor = executor
      wsdlFile = someWsdl
    }

    and: "the episode directory and files to bind are"
    project.wsdl.episodeFolder = "episodes"
    project.wsdl.episodes = episodeFiles
    
    and: "output directory is"
    def destinationDir = new File(project.projectDir, "src/main/java")

    and: "the set of episode files is"
    def episodes = project.files(episodeFiles.collect { new File(project.rootDir, "${project.wsdl.episodeFolder}/${it}") })

    when:
    task.start()

    then:
    1 * executor.execute(_ as AntBuilder, ["wsdl": someWsdl,
					   "extension": project.wsdl.wsimport,
					   "destinationDir": destinationDir,
					   "episodeFiles": episodes.files,
					   "classpath": classpath.asPath])
    where:
    episodeFiles << [[],
		     ["fake-episode.episode", "another-fake-episode.episode"]]
  }
}