package com.jacobo.gradle.plugins

import com.jacobo.gradle.plugins.extension.WsdlPluginExtension
import com.jacobo.gradle.plugins.extension.WsImportExtension

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task

import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.War

import org.gradle.api.plugins.JavaPlugin

import com.jacobo.gradle.plugins.tasks.WsdlName
import com.jacobo.gradle.plugins.tasks.War
import com.jacobo.gradle.plugins.tasks.ParseWsdl
import com.jacobo.gradle.plugins.tasks.CopyWarFiles
import com.jacobo.gradle.plugins.tasks.ResolveWsdlDependencies
import com.jacobo.gradle.plugins.tasks.GroupWarFiles

/**
 * @author djmijares
 * Created: Mon Jan 07 18:08:42 EST 2013
 */
class WsdlPlugin implements Plugin<Project> {
  static final String WSDL_PLUGIN_TASK_GROUP			 = 'parse'
  static final String WSDL_PLUGIN_PARSE_WSDL_TASK		 = 'parseWsdl'
  static final String WSDL_PLUGIN_WSDL_NAME_TASK		 = 'wsdlName'
  static final String WSDL_PLUGIN_RESOLVE_WSDL_DEPENDENCIES_TASK = 'resolveWsdlDependencies'
  static final String WSDL_PLUGIN_GROUP_WAR_FILES_TASK		 = 'groupWsdlWarFiles'
  static final String WSDL_PLUGIN_COPY_WAR_FILES_TASK		 = 'copyWsdlWarFiles'
  static final String WSDL_CONFIGURATION_NAME			 = 'jaxws'

  private WsdlPluginExtension extension

   void apply (Project project) {
     project.plugins.apply(JavaPlugin)
     project.plugins.apply(WarPlugin)
     configureWsdlExtension(project)
     configureWsdlConfiguration(project)
     def nameTask		= configureWsdlNameTask(project)
     def dependenciesTask	= configureResolveWsdlDependenciesTask(project, nameTask)
     def groupWsdlWarFilesTask	= configureGroupWarFilesTask(project, dependenciesTask)
     def copyWsdlWarFilesTask	= configureCopyWarFilesTask(project, groupWsdlWarFilesTask)
     configureWarTask(project, copyWsdlWarFilesTask)
     configureParseWsdlTask(project, nameTask)
   }

   private void configureWsdlExtension(final Project project) { 
     extension = project.extensions.create("wsdl", WsdlPluginExtension, project)
     extension.with { 
       wsdlFolder		= "wsdl"
       schemaFolder		= "schema"
       episodeFolder		= "schema/episodes"
       webServiceCopyDir	= "web-service"
     }

     def wsimportExtension = project.wsdl.extensions.create("wsimport", WsImportExtension)
     wsimportExtension.with { 
       sourceDestinationDirectory	= "src/main/java"
       verbose				= true
       keep				= true
       xnocompile			= true
       fork				= false
       xdebug				= false
       target				= "2.1"
       wsdlLocation			= "FILL_IN_BY_SERVER"
     }
   }

   private void configureWsdlConfiguration(final Project project) { 
     project.configurations.create(WSDL_CONFIGURATION_NAME) { 
       visible		= true
       transitive	= true
       description	= "The JAXWS libraries to be used for parsing the WSDL"
     }
   }

   private configureParseWsdlTask(final Project project, Task wsdlNameTask) { 
     Task pwt = project.tasks.create(WSDL_PLUGIN_PARSE_WSDL_TASK, ParseWsdl)
     pwt.description = "parse the wsdl with jaxws and wsimport"
     pwt.group = WSDL_PLUGIN_TASK_GROUP
     pwt.dependsOn(wsdlNameTask)
     pwt.conventionMapping.wsdlFile		= { project.wsdl.wsdlPath }
     pwt.conventionMapping.destinationDirectory	= { project.file(new File(project.projectDir, project.wsdl.wsimport.sourceDestinationDirectory)) }
     pwt.conventionMapping.episodeDirectory     = { project.file(new File(project.rootDir, project.wsdl.episodeFolder)) }
     pwt.conventionMapping.episodes		= { project.wsdl.wsimport.episodes }
     pwt.conventionMapping.target		= { project.wsdl.wsimport.target }
     pwt.conventionMapping.wsdlLocation		= { project.wsdl.wsimport.wsdlLocation }
     pwt.conventionMapping.verbose		= { project.wsdl.wsimport.verbose }
     pwt.conventionMapping.keep			= { project.wsdl.wsimport.keep }
     pwt.conventionMapping.xnocompile		= { project.wsdl.wsimport.xnocompile }
     pwt.conventionMapping.fork			= { project.wsdl.wsimport.fork }
     pwt.conventionMapping.xdebug		= { project.wsdl.wsimport.xdebug }
   }

   private Task configureWsdlNameTask(final Project project) { 
     Task wnt = project.tasks.create(WSDL_PLUGIN_WSDL_NAME_TASK, WsdlName)
     wnt.description = "find the wsdl File name from the web service sub project name, as per the convention"
     wnt.group = WSDL_PLUGIN_TASK_GROUP
     wnt.conventionMapping.projectName   = { project.name }
     wnt.conventionMapping.wsdlDirectory = { project.file(new File(project.rootDir, project.wsdl.wsdlFolder)) }
     return wnt
   }

   private Task configureResolveWsdlDependenciesTask(final Project project, Task wsdlNameTask) { 
     Task resolveDeps = project.tasks.create(WSDL_PLUGIN_RESOLVE_WSDL_DEPENDENCIES_TASK, ResolveWsdlDependencies)
     resolveDeps.description = "determine all the wsdl dependencies, expected via import/include statements"
     resolveDeps.group = WSDL_PLUGIN_TASK_GROUP
     resolveDeps.dependsOn(wsdlNameTask)
     resolveDeps.conventionMapping.wsdlDocument = { project.wsdl.wsdlPath }
     return resolveDeps
   }

   private Task configureGroupWarFilesTask(final Project project, Task dependenciesTask) { 
     Task gwwf = project.tasks.create(WSDL_PLUGIN_GROUP_WAR_FILES_TASK, GroupWarFiles)
     gwwf.description = "group all the wsdl war dependency files by common direct parent Folder"
     gwwf.group = WSDL_PLUGIN_TASK_GROUP
     gwwf.dependsOn(dependenciesTask)
     gwwf.conventionMapping.wsdlDependencies = { project.wsdl.wsdlDependencies }
     return gwwf
   }

   private Task configureCopyWarFilesTask(final Project project, Task groupWsdlWarFilesTask) {
     Task cwwf = project.tasks.create(WSDL_PLUGIN_COPY_WAR_FILES_TASK, CopyWarFiles)
     cwwf.description = "copies all WSDL war files into the build directory for packaging use in the war task"
     cwwf.group = WSDL_PLUGIN_TASK_GROUP
     cwwf.dependsOn(groupWsdlWarFilesTask)
     cwwf.conventionMapping.projectRootDir	= { project.rootDir }
     cwwf.conventionMapping.warFiles		= { project.wsdl.warFiles }
     cwwf.conventionMapping.webServicesCopyDir	= { project.file(new File(project.buildDir, project.wsdl.webServiceCopyDir)) }
     return cwwf
   }

   private void configureWarTask(final Project project, Task copyWsdlWarFilesTask) {
     Task oldWar = project.tasks.getByName('war')
     Task wsdlWar = project.tasks.replace(WarPlugin.WAR_TASK_NAME, War)
     wsdlWar.group = oldWar.group
     wsdlWar.description = oldWar.description + " Also bundles the xsd and wsdl files this service depends on"
     wsdlWar.dependsOn(copyWsdlWarFilesTask)
     wsdlWar.conventionMapping.wsdlFolder             = { project.wsdl.wsdlFolder }
     wsdlWar.conventionMapping.schemaFolder           = { project.wsdl.schemaFolder }
     wsdlWar.conventionMapping.wsdlDirectory          = { project.file(new File(new File(project.buildDir, project.wsdl.webServiceCopyDir), project.wsdl.wsdlFolder)) }
     wsdlWar.conventionMapping.schemaDirectory        = { project.file(new File(new File(project.buildDir, project.wsdl.webServiceCopyDir), project.wsdl.schemaFolder)) }
     project.build.dependsOn(wsdlWar)
   }
}
