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
package org.annotopia.grails.controllers.annotation.openannotation

import grails.converters.JSON
import grails.test.mixin.TestFor

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
@TestFor(OpenAnnotationController)
class OpenAnnotationControllerPostTests extends GroovyTestCase {

	def grailsApplication = new org.codehaus.groovy.grails.commons.DefaultGrailsApplication()
	
	def jenaVirtuosoStoreService;
	
	void testPublicEmbeddedCommentOnFullResourceNoGraph() {
		
		/*	 
		 	curl -i -X POST http://localhost:8080/s/annotation  \
		 	-H 'Content-Type: application/json' \
		 	-d '{"apiKey":"testkey", "validate":"ON", "flavor":"OA", "item":{"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json" ,"@id" : "urn:temp:001","@type" : "http://www.w3.org/ns/oa#Annotation","motivatedBy":"oa:commenting","annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},"annotatedAt":"2014-02-17T09:46:11EST","serializedBy":"urn:application:domeo","serializedAt":"2014-02-17T09:46:51EST", "hasBody" : {"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "This is Paolo Ciccarese''s CV","dc:format": "text/plain"},"hasTarget" : "http://paolociccarese.info"}}'
		 */
		
		String content = 
			'{' +
				'"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",' +
				'"@id" : "urn:temp:001",' +
				'"@type" : "http://www.w3.org/ns/oa#Annotation",' +
				'"motivatedBy":"oa:commenting",' +
				'"annotatedBy":{' +
				 	'"@id":"http://orcid.org/0000-0002-5156-2703",' +
					'"@type":"foaf:Person","foaf:name":"Paolo Ciccarese"' + 
				'},' +
				'"annotatedAt":"2014-02-17T09:46:11EST",' +
				'"serializedBy":"urn:application:domeo",' +
				'"serializedAt":"2014-02-17T09:46:51EST",' + 
				'"hasBody" : {' +
					'"@type" : ["cnt:ContentAsText", "dctypes:Text"],' +
					'"cnt:chars": "This is Paolo Ciccarese CV",' + 
					'"dc:format": "text/plain"' +
				'},' + 
				'"hasTarget" : "http://paolociccarese.info"}' + 
			'}';
		
		def c = new OpenAnnotationController()
		c.request.method = "POST"
		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
		c.save();
		
		assertEquals 200, response.status
		
		def json = JSON.parse(response.contentAsString);
		assertEquals 'saved', json.status
		
		// Verifies that one item with two graphs are returned: one for the annotation and one with metadata
		assertEquals 1, json.result.item.size()
		assertEquals 3, json.result.item[0]['@graph'].size()

		def anns = [] as Set
		json.result.item[0]['@graph'].each { subgraph ->	
			log.info("Graph " + subgraph);
			log.info("Detecting annotation type " + subgraph['@type']);
			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
				anns.add(subgraph['@id'])
				assertEquals 'urn:temp:001', subgraph['previousVersion']
				assertEquals 'http://www.w3.org/ns/oa#commenting', subgraph['motivatedBy']
				assertEquals 'urn:application:domeo', subgraph['serializedBy']
			} else if(subgraph['@type'].contains('http://www.w3.org/2011/content#ContentAsText')) {
				assertEquals 'blank', subgraph['previousVersion']
			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
				assertNotNull subgraph['lastUpdateOn']
			}
		}
		
		c.response.reset();
		c.request.JSON.clear();
		
		log.info("Removing annotations " + anns);
		
		anns.each { ann ->
			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
			c.delete();
		}
	}
	
	void testPublicEmbeddedCommentOnTextualFragmentOfResourceNoGraph() {
		
		/*
		 	curl -i -X POST http://localhost:8080/s/annotation \
    		-H "Content-Type: application/json" \
    		-d '{"apiKey": "testkey","item": {"@context": "https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json","@id": "urn:temp:7","@type": "oa:Annotation","motivatedBy": "oa:commenting","annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},"annotatedAt":"2014-02-17T09:46:11EST","serializedBy":"urn:application:domeo","serializedAt":"2014-02-17T09:46:51EST","hasTarget": {"@id": "urn:temp:8","@type": "oa:SpecificResource","hasSelector": {"@type": "oa:TextQuoteSelector","exact": "senior scientist and software engineer", "prefix": "I am a","suffix": ", working in the bio-medical informatics field since the year 2000"},"hasBody" : {"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "This is Paolo Ciccarese''s CV","dc:format": "text/plain"},"hasSource": {"@id": "http://paolociccarese.info","@type": "dctypes:Text"}}}}'
		 */
		
		String content = 
			'{' + 
				'"@context": "https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",' +
				'"@id": "urn:temp:7",' + 
				'"@type": "oa:Annotation",' + 
				'"motivatedBy": "oa:commenting",' + 
				'"annotatedBy":{' +
				 	'"@id":"http://orcid.org/0000-0002-5156-2703",' +
					'"@type":"foaf:Person","foaf:name":"Paolo Ciccarese"' + 
				'},' +
				'"annotatedAt":"2014-02-17T09:46:11EST",' +
				'"serializedBy":"urn:application:domeo",' +
				'"serializedAt":"2014-02-17T09:46:51EST",' +
				'"hasBody" : {"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "This is Paolo Ciccarese CV","dc:format": "text/plain"},' +
				'"hasTarget": {"@id": "urn:temp:8","@type": "oa:SpecificResource",' + 
					'"hasSelector": {' +
						'"@type": "oa:TextQuoteSelector",' +
						'"exact": "senior scientist and software engineer", ' +
						'"prefix": "I am a",' +
						'"suffix": ", working in the bio-medical informatics field since the year 2000"' +
					'},' +
					'"hasSource": {' +
						'"@id": "http://paolociccarese.info",' +
						'"@type": "dctypes:Text"' +
					'}' +
				'}' +
			'}'
				
		def c = new OpenAnnotationController()
		c.request.method = "POST"
		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
		c.save();
		
		assertEquals 200, response.status
		
		def json = JSON.parse(response.contentAsString);
		assertEquals 'saved', json.status	
		
		log.info(json.result.item);
		
		// Verifies that one item with two graphs are returned: one for the annotation and one with metadata
		assertEquals 1, json.result.item.size()
		assertEquals 6, json.result.item[0]['@graph'].size()
		
		def anns = [] as Set
		json.result.item[0]['@graph'].each { subgraph ->
			log.info("Graph " + subgraph);
			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
				anns.add(subgraph['@id'])
				assertEquals 'urn:temp:7', subgraph['previousVersion']
				assertEquals 'http://www.w3.org/ns/oa#commenting', subgraph['motivatedBy']
				assertEquals 'urn:application:domeo', subgraph['serializedBy']
			} else if(subgraph['@type'].contains('http://www.w3.org/2011/content#ContentAsText')) {
				assertEquals 'blank', subgraph['previousVersion']
			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
				assertNotNull subgraph['lastUpdateOn']
			}  else if(subgraph['@type'].contains('http://www.w3.org/ns/oa#TextQuoteSelector')) {
				assertNotNull subgraph['exact']
				assertNotNull subgraph['prefix']
				assertNotNull subgraph['suffix']
			}  else if(subgraph['@type'].contains('http://www.w3.org/ns/oa#SpecificResource')) {
				assertEquals 'urn:temp:8', subgraph['previousVersion']
				assertNotNull subgraph['hasSelector']
				assertNotNull subgraph['hasSource']
			}
		}
		
		c.response.reset();
		c.request.JSON.clear();
		
		log.info("Removing annotations " + anns);
		
		anns.each { ann ->
			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
			c.delete();
		}
	}
	
	void testPublicHighlightOnTextualFragmentOfResourceNoGraph() {
		
		/*
			curl -i -X POST http://localhost:8080/s/annotation \
			-H "Content-Type: application/json" \
			-d '{"apiKey": "testkey","item": {"@context": "https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json","@id": "urn:temp:7","@type": "oa:Annotation","motivatedBy": "oa:highlighting","annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},"annotatedAt":"2014-02-17T09:46:11EST","serializedBy":"urn:application:domeo","serializedAt":"2014-02-17T09:46:51EST","hasTarget": {"@id": "urn:temp:8","@type": "oa:SpecificResource","hasSelector": {"@type": "oa:TextQuoteSelector","exact": "senior scientist and software engineer", "prefix": "I am a","suffix": ", working in the bio-medical informatics field since the year 2000"},"hasSource": {"@id": "http://paolociccarese.info","@type": "dctypes:Text"}}}}'
		 */
		
		String content = 
			'{' +
				'"@context": "https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",' +
				'"@id": "urn:temp:7",' + 
				'"@type": "oa:Annotation",' +
				'"motivatedBy": "oa:highlighting",' +
				'"annotatedBy":{' +
				 	'"@id":"http://orcid.org/0000-0002-5156-2703",' +
					'"@type":"foaf:Person","foaf:name":"Paolo Ciccarese"' + 
				'},' +
				'"annotatedAt":"2014-02-17T09:46:11EST",' +
				'"serializedBy":"urn:application:domeo",' +
				'"serializedAt":"2014-02-17T09:46:51EST",' +
				'"hasTarget": {"@id": "urn:temp:8","@type": "oa:SpecificResource",' + 
					'"hasSelector": {' +
						'"@type": "oa:TextQuoteSelector",' +
						'"exact": "senior scientist and software engineer", ' +
						'"prefix": "I am a",' +
						'"suffix": ", working in the bio-medical informatics field since the year 2000"' +
					'},' +
					'"hasSource": {' +
						'"@id": "http://paolociccarese.info",' +
						'"@type": "dctypes:Text"' +
					'}' +
				'}' +
			'}'

		def c = new OpenAnnotationController()
		c.request.method = "POST"
		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
		c.save();
		
		assertEquals 200, response.status
		
		def json = JSON.parse(response.contentAsString);
		assertEquals 'saved', json.status
		
		def anns = [] as Set
		boolean foundBody = false;
		json.result.item[0]['@graph'].each { subgraph ->
			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
				anns.add(subgraph['@id'])
				assertEquals 'urn:temp:7', subgraph['previousVersion']
				assertEquals 'http://www.w3.org/ns/oa#highlighting', subgraph['motivatedBy']
				assertEquals 'urn:application:domeo', subgraph['serializedBy']
			} else if(subgraph['@type'].contains('http://www.w3.org/2011/content#ContentAsText')) {
				foundBody = true;
			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
				assertNotNull subgraph['lastUpdateOn']
			}  else if(subgraph['@type'].contains('http://www.w3.org/ns/oa#TextQuoteSelector')) {
				assertNotNull subgraph['exact']
				assertNotNull subgraph['prefix']
				assertNotNull subgraph['suffix']
			}  else if(subgraph['@type'].contains('http://www.w3.org/ns/oa#SpecificResource')) {
				assertEquals 'urn:temp:8', subgraph['previousVersion']
				assertNotNull subgraph['hasSelector']
				assertNotNull subgraph['hasSource']
			}
		}

		assertFalse foundBody;
		
		c.response.reset();
		c.request.JSON.clear();
		
		log.info("Removing annotations " + anns);
		
		anns.each { ann ->
			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
			c.delete();
		}
	}
	
	void testPublicEmbeddedCommentOnFullResourceWithNamedGraph() {
		/*
		 	curl -i -X POST http://localhost:8080/s/annotation \
    		-H "Content-Type: application/json" \
    		-d '{"apiKey":"testkey", "validate":"ON", "flavor":"OA", "item":{"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json" , "@graph": [{"@id" : "urn:temp:001","@type" : "http://www.w3.org/ns/oa#Annotation","motivatedBy":"oa:commenting","annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},"annotatedAt":"2014-02-17T09:46:11EST","serializedBy":"urn:application:domeo","serializedAt":"2014-02-17T09:46:51EST", "hasBody" : {"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "This is Paolo Ciccarese''s CV","dc:format": "text/plain"},"hasTarget" : "http://paolociccarese.info"}],"@id" : "urn:temp:003"}}'
		 */
		
		String content =
			'{' + 
				'"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",' +
				'"@graph": [' + 
					'{' +
						'"@id" : "urn:temp:001",' + 
						'"@type" : "http://www.w3.org/ns/oa#Annotation",' +
						'"motivatedBy":"oa:commenting",' +
						'"annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},' +
						'"annotatedAt":"2014-02-17T09:46:11EST",' +
						'"serializedBy":"urn:application:domeo",' +
						'"serializedAt":"2014-02-17T09:46:51EST",' +
						'"hasBody" : {' +
							'"@type" : ["cnt:ContentAsText", "dctypes:Text"],' +
							'"cnt:chars": "This is Paolo Ciccarese CV",' + 
							'"dc:format": "text/plain"' +
						'},' + 						
						'"hasTarget" : "http://paolociccarese.info"' +
					'}' +
				'],"@id" : "urn:temp:003"}' +
			'}'	
			
		def c = new OpenAnnotationController()
		c.request.method = "POST"
		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
		c.save();
		
		assertEquals 200, response.status
		
		def json = JSON.parse(response.contentAsString);
		assertEquals 'saved', json.status
		
		def anns = [] as Set
		int graphsCounter = 0;
		boolean foundBody = false;
		json.result.item[0]['@graph'].each { subgraph ->
			graphsCounter++;
			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
				anns.add(subgraph['@id'])
				assertEquals 'urn:temp:001', subgraph['previousVersion']
				assertEquals 'http://www.w3.org/ns/oa#commenting', subgraph['motivatedBy']
				assertEquals 'urn:application:domeo', subgraph['serializedBy']
			} else if(subgraph['@type'].contains('http://www.w3.org/2011/content#ContentAsText')) {
				foundBody = true;
				assertEquals 'blank', subgraph['previousVersion']
			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
				assertNotNull subgraph['lastUpdateOn']
			} 
		}
		assertEquals 3, graphsCounter;
		assertTrue foundBody;
		
		c.response.reset();
		c.request.JSON.clear();
		
		log.info("Removing annotations " + anns);
		
		anns.each { ann ->
			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
			c.delete();
		}
	}
	
	void testPublicHighlightOnTextualFragmentOfResourceWithNamedGraph() {
		/*
		 	curl -i -X POST http://localhost:8080/s/annotation \
			-H "Content-Type: application/json" \
			-d '{"apiKey": "testkey","item": {"@context": "https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json","@graph": [{"@id": "urn:temp:7","@type": "oa:Annotation","motivatedBy": "oa:highlighting","annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},"annotatedAt":"2014-02-17T09:46:11EST","serializedBy":"urn:application:domeo","serializedAt":"2014-02-17T09:46:51EST","hasTarget": {"@id": "urn:temp:8","@type": "oa:SpecificResource","hasSelector": {"@type": "oa:TextQuoteSelector","exact": "senior scientist and software engineer", "prefix": "I am a","suffix": ", working in the bio-medical informatics field since the year 2000"},"hasSource": {"@id": "http://paolociccarese.info","@type": "dctypes:Text"}}}],"@id" : "urn:temp:003"}}'
		 */
		
		String content =
			'{' +
				'"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",' +
				'"@graph": [' +
					'{' +
						'"@id": "urn:temp:7",' +
						'"@type": "oa:Annotation",' +
						'"motivatedBy": "oa:highlighting",' +
						'"annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},' +
						'"annotatedAt":"2014-02-17T09:46:11EST",' +
						'"serializedBy":"urn:application:domeo",' +
						'"serializedAt":"2014-02-17T09:46:51EST",' +
						'"hasTarget": {' + 
							'"@id": "urn:temp:8","@type": "oa:SpecificResource",' +
							'"hasSelector": {' +
								'"@type": "oa:TextQuoteSelector",' +
								'"exact": "senior scientist and software engineer", ' +
								'"prefix": "I am a",' +
								'"suffix": ", working in the bio-medical informatics field since the year 2000"' +
							'},' +
							'"hasSource": {' +
								'"@id": "http://paolociccarese.info",' +
								'"@type": "dctypes:Text"' +
							'}' +
						'}' +
					'}' +
				'],"@id" : "urn:temp:003"' +
			'}'
				
		def c = new OpenAnnotationController()
		c.request.method = "POST"
		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
		c.save();
		
		assertEquals 200, response.status
		
		def json = JSON.parse(response.contentAsString);
		assertEquals 'saved', json.status
		
		log.info(json.result.item);
		
		def anns = [] as Set
		int graphsCounter = 0;
		boolean foundBody = false;
		json.result.item[0]['@graph'].each { subgraph ->
			graphsCounter++;
			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
				anns.add(subgraph['@id'])
				assertEquals 'urn:temp:7', subgraph['previousVersion']
				assertEquals 'http://www.w3.org/ns/oa#highlighting', subgraph['motivatedBy']
				assertEquals 'urn:application:domeo', subgraph['serializedBy']
			} else if(subgraph['@type'].contains('http://www.w3.org/2011/content#ContentAsText')) {
				foundBody = true;
				assertEquals 'blank', subgraph['previousVersion']
			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
				assertNotNull subgraph['lastUpdateOn']
			}
		}
		assertEquals 5, graphsCounter;
		assertFalse foundBody;	
		
		c.response.reset();
		c.request.JSON.clear();
		
		log.info("Removing annotations " + anns);
		
		anns.each { ann ->
			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
			c.delete();
		}
	}
	
	void testPublicTwoEmbeddedCommentsOnFullResourceWithNamedGraph() {
		/*
			 curl -i -X POST http://localhost:8080/s/annotation \
			-H "Content-Type: application/json" \
			-d '{"apiKey":"testkey", "validate":"ON", "flavor":"OA", "item":{"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json" , "@graph": [{"@id" : "urn:temp:001","@type" : "http://www.w3.org/ns/oa#Annotation","motivatedBy":"oa:commenting","annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},"annotatedAt":"2014-02-17T09:46:11EST","serializedBy":"urn:application:domeo","serializedAt":"2014-02-17T09:46:51EST", "hasBody":[{"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "This is Paolo Ciccarese CV","dc:format": "text/plain"},{"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "Paolo Ciccarese - Homepage","dc:format": "text/plain"}],"hasTarget" : "http://paolociccarese.info"}],"@id" : "urn:temp:003"}}}'
		 */
		
		String content =
			'{' +
				'"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",'+
				'"@graph": [' +
					'{' +
						'"@id" : "urn:temp:001",' +
						'"@type" : "http://www.w3.org/ns/oa#Annotation",' +
						'"motivatedBy":"oa:commenting",' +
						'"annotatedBy":{"@id":"http://orcid.org/0000-0002-5156-2703","@type":"foaf:Person","foaf:name":"Paolo Ciccarese"},' +
						'"annotatedAt":"2014-02-17T09:46:11EST",' +
						'"serializedBy":"urn:application:domeo",' +
						'"serializedAt":"2014-02-17T09:46:51EST",' +
						'"hasBody" : [' +
							'{"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "This is Paolo Ciccarese CV","dc:format": "text/plain"},' +
							'{"@type" : ["cnt:ContentAsText", "dctypes:Text"],"cnt:chars": "Paolo Ciccarese - Homepage","dc:format": "text/plain"}' +
						'],' +
						'"hasTarget" : "http://paolociccarese.info"' +
					'}' +
				'],"@id" : "urn:temp:003"' +
			'}'
	
		def c = new OpenAnnotationController()
		c.request.method = "POST"
		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
		c.save();
		
		assertEquals 200, response.status
		
		def json = JSON.parse(response.contentAsString);
		assertEquals 'saved', json.status
						
		def anns = [] as Set
		int graphsCounter = 0;
		int bodyCounter = 0;
		json.result.item[0]['@graph'].each { subgraph ->
			log.info("Graph " + subgraph);
			graphsCounter++;
			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
				anns.add(subgraph['@id'])
				assertEquals 'urn:temp:001', subgraph['previousVersion']
				assertEquals 'http://www.w3.org/ns/oa#commenting', subgraph['motivatedBy']
				assertEquals 'urn:application:domeo', subgraph['serializedBy']
			} else if(subgraph['@type'].contains('http://www.w3.org/2011/content#ContentAsText')) {
				bodyCounter++ ;
				assertEquals 'blank', subgraph['previousVersion']
			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
				assertNotNull subgraph['lastUpdateOn']
			}
		}
		assertEquals 4, graphsCounter;
		assertEquals 2, bodyCounter;
		
		c.response.reset();
		c.request.JSON.clear();
		
		log.info("Removing annotations " + anns);
		
		anns.each { ann ->
			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
			c.delete();
		}
	}	
	
//	void testPublicTwoBodiesAsNamedGraphsOnFullResourceWithNamedGraph() {
//		/*
//		 	curl -i -X POST http://localhost:8080/s/annotation \
//			-H "Content-Type: application/json" \
//			-d '{"apiKey":"testkey", "item":{"@context": "https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json","@graph": [{"@id": "urn:temp:1","@type": "rdf:Graph","@graph": {"@id": "http://www.example.org/artifact1","label": "yolo body 1"}},{"@id": "urn:temp:2","@type": "rdf:Graph","@graph": {"@id": "http://www.example.org/artifact2","label": "yolo body 2"}},{"@id": "urn:temp:3","@type": "rdf:Graph","@graph": {"@id": "urn:temp:7","@type": "oa:Annotation","motivatedBy":"oa:describing","hasBody": ["urn:temp:1","urn:temp:2"],"hasTarget": "http://paolociccarese.info"}}]}}'
//		 */	
//		
//		String content =
//			'{' +
//				'"@context":"https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/OAContext.json",'+
//				'"@graph": [' +
//					'{' +
//						'"@id": "urn:temp:1",' +
//						'"@type": "rdf:Graph",' +
//						'"@graph": {' +
//							'"@id": "http://www.example.org/artifact1",' +
//							'"label": "yolo body 1"' +
//						'}' +
//					'},' +
//					'{' +
//						'"@id": "urn:temp:2",' +
//						'"@type": "rdf:Graph",' +
//						'"@graph": {' + 
//							'"@id": "http://www.example.org/artifact2",' +
//							'"label": "yolo body 2"' +
//						'}' +
//					'},' +
//					'{' +
//						'"@id": "urn:temp:3",' + 
//						'"@type": "rdf:Graph",' + 
//						'"@graph": {' +
//							'"@id": "urn:temp:7",' +
//							'"@type": "oa:Annotation",' + 
//							'"motivatedBy":"oa:describing",' +
//							'"hasBody": ["urn:temp:1","urn:temp:2"],' + 
//							'"hasTarget": "http://paolociccarese.info"' +
//						'}' +
//					'}' + 
//				']' + 
//			'}'
//
//		def c = new OpenAnnotationController()
//		c.request.method = "POST"
//		c.request.JSON = '{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","item":' + content+ '}'
//		c.save();
//		
//		assertEquals 200, response.status
//		
//		log.info(response.contentAsString);
//		
//		def json = JSON.parse(response.contentAsString);
//		assertEquals 'saved', json.status
//		
//		log.info("Items: " + json.result.item);
//		
//		def anns = [] as Set
//		int graphsCounter = 0;
//		int bodyGraphCounter = 0;
//		int bodyCounter = 0;
//		
//		log.info(json.result.item);
//		
//		json.result.item[0]['@graph'].each { subgraph ->
//			graphsCounter++;
//			if(subgraph['@type']=='http://www.w3.org/ns/oa#Annotation') {
//				anns.add(subgraph['@id'])
//				assertEquals 'urn:temp:7', subgraph['previousVersion']
//				assertEquals 'http://www.w3.org/ns/oa#describing', subgraph['motivatedBy']
//			} else if(subgraph['@type']==null) {
//				bodyCounter++ ;
//			} else if(subgraph['@type'].contains('http://purl.org/annotopia#AnnotationGraph')) {
//				assertNotNull subgraph['lastUpdateOn']
//			} else if(subgraph['@type'].contains('http://purl.org/annotopia#BodyGraph')) {
//				bodyGraphCounter++
//			}
//		}
//		assertEquals 3, graphsCounter;
//		assertEquals 2, bodyCounter;
//		assertEquals 2, bodyGraphCounter
//		
//		c.response.reset();
//		c.request.JSON.clear();
//		
//		log.info("Removing annotations " + anns);
//		
//		anns.each { ann ->
//			c.request.JSON << JSON.parse('{"apiKey":"' + grailsApplication.config.annotopia.storage.testing.apiKey + '","uri":"' + ann + '"}')
//			c.delete();
//		}
//	}
}
