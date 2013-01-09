package com.jacobo.gradle.plugins.model

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import spock.lang.Specification

/**
 * @author djmijares
 * Created: Mon Jan 07 18:08:42 EST 2013
 */
class WsdlDependencyResolverSpec extends Specification { 

  def WsdlDependencyResolver wdr = new WsdlDependencyResolver()

  def "is Already in List works out well" () { 

  when:
  def list = [new File("schema.xsd"), new File("test.wsdl")]

  then:
  result == wdr.isAlreadyInList(list, file)

  where:
  file                | result
  new File("schema.xsd")  | true
  new File("test.wsdl")   | true
  new File("nothing.xsd") | false

  }

  def "add schema to Parse to list"() {   
  
  when:
  wdr.schemaLocationsToParse = [new File("test.wsdl")]
  wdr.addSchemaLocationToParse(file)

  then:
  result == wdr.schemaLocationsToParse

  where:
  file                    | result
  new File("schema.xsd")  | [new File("test.wsdl"), new File("schema.xsd")]
  new File("nothing.xsd") | [new File("test.wsdl"), new File("nothing.xsd")]
  new File("test.wsdl")   | [new File("test.wsdl")]

  }

  def "add absolute path dependencies" () { 

  when:
  wdr.absolutePathDependencies = [new File("test.wsdl")]
  wdr.addAbsolutePathDependencies(file)

  then:
  result == wdr.absolutePathDependencies

  where:
  file                    | result
  new File("schema.xsd")  | [new File("test.wsdl"), new File("schema.xsd")]
  new File("nothing.xsd") | [new File("test.wsdl"), new File("nothing.xsd")]
  new File("test.wsdl")   | [new File("test.wsdl")]


  }
 
  def "test absolute schema location"() { 
  expect:
  result == wdr.getAbsoluteSchemaLocation(schemaLocale, parent)

  where:
  parent             | schemaLocale        | result
  new File("schema") | "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  new File("wsdl")   | "../blah/something"      | new File("blah/something").absoluteFile
  new File("nothing")| "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  }


  def "test the full resolver with WSDLs and XSDs" () { 
    
  when:
  wdr.wsdlFile = new File(url.toURI())

  then:
  result*.absoluteFile == wdr.resolveWSDLDependencies()
  //TODO this expectation result could be a little more dynamic, in other words the list should HAVE to be in order, just contain the same elements.  see what you can do later
  where:
  url | result
  this.getClass().getResource("/wsdl/noXsdImport.wsdl") | [new File("build/resources/test/wsdl/noXsdImport.wsdl")]
  this.getClass().getResource("/wsdl/OneXsdImport.wsdl") | [new File("build/resources/test/wsdl/OneXsdImport.wsdl"), new File("build/resources/test/schema/Messages/Messages.xsd")]
  this.getClass().getResource("/wsdl/TwoXsdImports.wsdl") | [new File("build/resources/test/wsdl/TwoXsdImports.wsdl"), new File("build/resources/test/schema/PO/PurchaseOrder.xsd"), new File("build/resources/test/schema/Messages/Messages.xsd")]
  this.getClass().getResource("/wsdl/XsdImportXsdAndIncludesXsd.wsdl") | [new File("build/resources/test/wsdl/XsdImportXsdAndIncludesXsd.wsdl"), new File("build/resources/test/schema/Include/OrderNumber.xsd"), new File("build/resources/test/schema/Include/include2.xsd"), new File("build/resources/test/schema/Include/include.xsd"), new File("build/resources/test/schema/Include/Product.xsd"), new File("build/resources/test/schema/Messages/Messages.xsd")]
  this.getClass().getResource("/wsdl/ImportsAnotherWsdl.wsdl") | [new File("build/resources/test/wsdl/ImportsAnotherWsdl.wsdl"), new File("build/resources/test/wsdl/abstract.wsdl"), new File("build/resources/test/schema/Messages/Messages.xsd")] 
  }

}