/*
 *     The MIT License
 *
 *     Copyright (C) 2016-2017 Henix, henix.fr
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *     THE SOFTWARE.
 */
package org.jenkinsci.squashtm.tawrapper

import groovy.json.JsonBuilder
import hudson.FilePath
import hudson.remoting.VirtualChannel
import jenkins.SlaveToMasterFileCallable
import org.jenkinsci.squashtm.core.JobInformations
import org.jenkinsci.squashtm.core.TestResult

class TestListSaver {
	
	private static final String TEST_LIST_FILENAME = 'testTree.json'

	JobInformations infos
	PrintStream logger
	
	TestListSaver(){
		super();
	}
	
	TestListSaver(JobInformations infos, PrintStream logger){
		super();
		this.infos = infos
		this.logger = logger
	}
	
	
	// will print the list of tests to file, as a structured format
	public void saveTestList(Collection<TestResult> results){
	
		TestListBean testList = new TestListBean()
		testList.populateWith results
		
		FilePath outfile = new FilePath(new FilePath(infos.buildPath), TEST_LIST_FILENAME)
		
		TestListCallable callable = new TestListCallable(testList : testList, logger : logger)
		
		outfile.act callable
		
		
	}	
	
	// this is already executed on the Master (normally)
	// but let implement a FileCallable because better safe than sorry
	private static final class TestListCallable extends SlaveToMasterFileCallable<Void>{
		
		TestListBean testList
		PrintStream logger

		@Override
		public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
			if (f.exists()){
				f.delete()
			}
			
			if (f.createNewFile()){
				JsonBuilder jsonbuilder = new JsonBuilder(testList)
				f.write jsonbuilder.toPrettyString()
			}
			else{
				logger.println "[TM-PLUGIN] : error : could not create file ${TEST_LIST_FILENAME}. "+
								"Squash TM will not be able to retrieve the test list until this file is created."
			}
		}
		
	}
	
	// ************************************ inner classes **********************************
	
	
	private static final class TestListBean implements Serializable{
		String timestamp
		String name
		Collection<SubNode> contents = []
		
		TestListBean(){
			
		}
		
		void populateWith(Collection<TestResult> results){
			
			timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			name = 'tests'
			
			if (results.isEmpty()){
				return
			}
			
			// our task is now to un-flatten the path of each result and build a tree-like hierarchy
	
			def allpaths =	stripPrefixes results.collect{ it.path }
			allpaths = allpaths.sort()
			
			SubNode root = new SubNode()
			
			allpaths.each{
				def components = it.split '/'
				def currentNode = root
				components.each{
					currentNode = currentNode.findOrAddChild it
				}
			}
			
			// now we assign our contents
			contents = root.contents
		}
		
		private List stripPrefixes(Collection<String> paths){
			
			// here we sample our prefix
			def prefixSize = paths[0].find(/^\/[^\/]+\//).size()
			
			// and now we strip it from every pathes
			paths.collect{ it.drop prefixSize }
			
		}

		
	}
	
	
	
	private static final class SubNode{
		String name
		Collection<SubNode> contents
		
		private SubNode findOrAddChild(String name){
			if (contents == null)
				contents = []
				
			SubNode child = contents.find { it.name == name }
			
			if (child == null){
				child = new SubNode(name : name)
				contents << child
			}
			
			return child
		}
	}
	
}
