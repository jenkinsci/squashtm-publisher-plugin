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

import java.nio.file.Files;

import org.jenkinsci.squashtm.core.TestResult
import org.jenkinsci.squashtm.testutils.NullOutputStream;
import org.jenkinsci.squashtm.core.JobInformations

import groovy.json.JsonSlurper;
import hudson.FilePath;
import spock.lang.Specification;

class TestListSaverTest extends Specification{

	
	def "should save a bunch of test as a test list"(){
		
		given :
			// Files.createTempDirectory fails, so I'm doing it the old way
			File testDir = File.createTempFile 'test', 'tmp'
			testDir.delete()
			testDir.mkdir()
			testDir.deleteOnExit()
			
		
		and :
			def results = ['/project/folder/bob', '/project/folder/mike', '/project/a'].collect{
				new TestResult(path : it)
			}
			
		and : "we need to mock the printstream"
			PrintStream logger = new PrintStream(new NullOutputStream())
			
		and :
			JobInformations infos = new JobInformations(buildPath : testDir)
			
			def saver = new TestListSaver(infos, logger)
			
		when :
			saver.saveTestList results
		
		then :
			def txtSuite = new File(testDir, 'testTree.json').text
			def savedSuite = new JsonSlurper().parseText txtSuite 
			
			savedSuite.contents[0].name == 'a'
			savedSuite.contents[1].name == 'folder'
			
			savedSuite.contents[1].contents[0].name == 'bob'
			savedSuite.contents[1].contents[1].name == 'mike'

		cleanup :
			testDir.delete()	// with deleteOnExit()
	}
	
	
}
