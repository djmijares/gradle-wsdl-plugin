package com.jacobo.gradle.plugins.util

import spock.lang.Specification

class FileHelperSpec extends Specification {
  
  def "test absolute schema location"() { 
  expect:
  result == FileHelper.getAbsoluteSchemaLocation(schemaLocale, parent)

  where:
  parent             | schemaLocale        | result
  new File("schema") | "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  new File("wsdl")   | "../blah/something" | new File("blah/something").absoluteFile
  new File("nothing")| "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  }
}