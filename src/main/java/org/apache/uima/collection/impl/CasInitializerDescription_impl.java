/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.collection.impl;

import java.util.ArrayList;

import org.w3c.dom.Element;

import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CasInitializer;
import org.apache.uima.collection.CasInitializerDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.impl.ResourceCreationSpecifier_impl;
import org.apache.uima.resource.metadata.OperationalProperties;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.resource.metadata.impl.ProcessingResourceMetaData_impl;
import org.apache.uima.resource.metadata.impl.PropertyXmlInfo;
import org.apache.uima.resource.metadata.impl.XmlizationInfo;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLParser;
import org.apache.uima.util.XMLParser.ParsingOptions;

/**
 * @deprecated As of v2.0, CAS Initializers are deprecated.
 */
public class CasInitializerDescription_impl extends ResourceCreationSpecifier_impl implements
                CasInitializerDescription {
  
  private static final long serialVersionUID = -4559482063745943204L;

  /**
   * Creates a new CasInitializerDescription_impl. Initializes the MetaData and
   * FrameworkImplementation attributes.
   */
  public CasInitializerDescription_impl() {
    setMetaData(new ProcessingResourceMetaData_impl());
    setFrameworkImplementation("org.apache.uima.java");
    // set default operational properties (may be overrriden during parsing)
    OperationalProperties opProps = UIMAFramework.getResourceSpecifierFactory()
                    .createOperationalProperties();
    opProps.setModifiesCas(true);
    opProps.setMultipleDeploymentAllowed(true);
    opProps.setOutputsNewCASes(false);
    getCasInitializerMetaData().setOperationalProperties(opProps);
  }

  /**
   * @see org.apache.uima.collection.processing.CasInitializerDescription#getCasInitializerMetaData()
   */
  public ProcessingResourceMetaData getCasInitializerMetaData() {
    return (ProcessingResourceMetaData) getMetaData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.resource.ResourceCreationSpecifier#doFullValidation(org.apache.uima.resource.ResourceManager)
   */
  public void doFullValidation(ResourceManager aResourceManager)
                  throws ResourceInitializationException {
    // check that user class was specified
    if (getImplementationName() == null || getImplementationName().length() == 0) {
      throw new ResourceInitializationException(
                      ResourceInitializationException.MISSING_IMPLEMENTATION_CLASS_NAME,
                      new Object[] { getSourceUrlString() });
    }
    // try to load user class
    // ust UIMA extension ClassLoader if available
    Class implClass;
    ClassLoader cl = aResourceManager.getExtensionClassLoader();
    try {
      if (cl != null) {
        implClass = cl.loadClass(getImplementationName());
      } else {
        implClass = Class.forName(getImplementationName());
      }
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(ResourceInitializationException.CLASS_NOT_FOUND,
                      new Object[] { getImplementationName(), getSourceUrlString() }, e);
    }
    // verify the user class implements CasInitializer
    if (!CasInitializer.class.isAssignableFrom(implClass)) {
      throw new ResourceInitializationException(
                      ResourceInitializationException.RESOURCE_DOES_NOT_IMPLEMENT_INTERFACE,
                      new Object[] { getImplementationName(), CasInitializer.class.getName(),
                          getSourceUrlString() });
    }
    // try to create a CAS
    ArrayList metadata = new ArrayList();
    metadata.add(getCasInitializerMetaData());
    CasCreationUtils.createCas(metadata);
  }

  /**
   * Overridden to set default operational properties if they are not specified in descriptor.
   */
  public void buildFromXMLElement(Element aElement, XMLParser aParser, ParsingOptions aOptions)
                  throws InvalidXMLException {
    super.buildFromXMLElement(aElement, aParser, aOptions);
    if (getCasInitializerMetaData().getOperationalProperties() == null) {
      OperationalProperties opProps = UIMAFramework.getResourceSpecifierFactory()
                      .createOperationalProperties();
      opProps.setModifiesCas(true);
      opProps.setMultipleDeploymentAllowed(true);
      opProps.setOutputsNewCASes(false);
      getCasInitializerMetaData().setOperationalProperties(opProps);
    }
  }

  protected XmlizationInfo getXmlizationInfo() {
    return XMLIZATION_INFO;
  }

  static final protected XmlizationInfo XMLIZATION_INFO = new XmlizationInfo(
                  "casInitializerDescription", new PropertyXmlInfo[] {
                      new PropertyXmlInfo("frameworkImplementation"),
                      new PropertyXmlInfo("implementationName"),
                      new PropertyXmlInfo("metaData", null),
                      new PropertyXmlInfo("externalResourceDependencies"),
                      new PropertyXmlInfo("resourceManagerConfiguration", null) });
}
