package com.jacobo.gradle.plugins.reader

import com.jacobo.gradle.plugins.model.WsdlSlurper
import com.jacobo.gradle.plugins.model.XsdSlurper

import spock.lang.Specification

class DocumentReaderSpec extends Specification {
  
  def "slurp Document get the right type of slurper to return" () {

  when:
  def result = DocumentReader.slurpDocument(new File(url.toURI()))

  then:
  result.class in [clazz]
  result.currentDir == dir.absoluteFile
  result.documentName == name
  
  where:
  url | clazz | dir | name
  this.getClass().getResource("/wsdl/noXsdImport.wsdl") | WsdlSlurper | new File("build/resources/test/wsdl") | "noXsdImport.wsdl"
  this.getClass().getResource("/schema/PO/PurchaseOrder.xsd") | XsdSlurper | new File("build/resources/test/schema/PO") | "PurchaseOrder.xsd"

  }
}