package com.jacobo.gradle.plugins

import com.jacobo.gradle.plugins.BaseSpecification

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

class ProjectTaskSpecification extends BaseSpecification {
  
  def project
  def task

  def projectAtDir(File dir, String applyPlugin) {
    project = ProjectBuilder.builder().withProjectDir(dir).build()
    project.apply(plugin: applyPlugin)
  }

  def buildProject(String applyPlugin) {
    project = ProjectBuilder.builder().build()
    project.apply(plugin: applyPlugin)
  }
}