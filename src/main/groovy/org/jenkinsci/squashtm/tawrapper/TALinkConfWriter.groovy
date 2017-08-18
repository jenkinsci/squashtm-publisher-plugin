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

import jenkins.model.Jenkins

import org.jenkinsci.squashtm.core.TMServer
import org.jenkinsci.squashtm.core.SquashTMPublisher.PublisherStepDescriptor

class TALinkConfWriter {
	
	private static final String FILENAME = "taLinkConf.properties"

	public void save(PublisherStepDescriptor descriptor){
		
		try{
			File conf = confFile
			
			def content = ""
			descriptor.tmServers.eachWithIndex { server, idx ->
				def entry = toEntry(server, (idx+1))
				content += entry
			}
			
			conf.write content
			
		}
		catch(Exception e){
			// TODO : maybe do something some day : logging, warning UI dialog, System.exit(666) etc
		}
	}
	
	private String toEntry(TMServer server, int counter){
"""
endpoint.${counter}=${server.url}
endpoint.${counter}.login=${server.login}
endpoint.${counter}.password=${server.password}

"""
	}
	
	private File getConfFile(){
		File root = rootDir
		File conf = new File(root, FILENAME)
		if (! conf.exists()){
			conf.createNewFile()
		}
		else{
			conf.delete()
			conf.createNewFile()
		}
		return conf
		
	}
	
	// that method should be overriden for testing purposes
	private File getRootDir(){
		return Jenkins.instance.rootDir
	}
	
}
