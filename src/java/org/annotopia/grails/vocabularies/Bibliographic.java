/*
 * Copyright 2014 Massachusetts General Hospital
 *
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
package org.annotopia.grails.vocabularies;

/**
 * This is a class collecting the properties and classes proper of 
 * the RDF vocabulary.
 * 
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
public interface Bibliographic {

	public static final String IS_MANIFESTATION_OF = "http://purl.org/spar/fabio#isManifestationOf";
	public static final String PPI = "http://purl.org/spar/fabio#hasPII";
	public static final String PMCID = "http://purl.org/spar/fabio#hasPubMedCentralId";
	public static final String PMID = "http://purl.org/spar/fabio#hasPubMedId";
	public static final String DOI 	= "http://prismstandard.org/namespaces/basic/2.0/doi";
	
}
