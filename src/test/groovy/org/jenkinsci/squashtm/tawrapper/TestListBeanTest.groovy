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

import org.jenkinsci.squashtm.core.TestResult;
import org.jenkinsci.squashtm.tawrapper.TestListSaver.TestListBean
import org.jenkinsci.squashtm.tawrapper.TestListSaver.SubNode

import spock.lang.Specification;

class TestListBeanTest extends Specification{
	
	
	def "should remove the project-part of a bunch of paths"(){
		
		
		given :
			def paths = [
				'/project-name/folder/bob',
				'/project-name/folder/mike',
				'/project-name/a'
			]
		
		and : 
			def stripped = [
				'folder/bob',
				'folder/mike',
				'a'
			]
		
		when :
			def bean = new TestListBean()
			def res = bean.stripPrefixes paths
		
		then :
			res as Set == stripped as Set
	}
	
	
	def "should build a (sorted) test list using a collection of results"(){
		
		given :
			def results = ['/project/folder/mike', '/project/folder/bob', '/project/a'].collect{
				new TestResult(path:it)
			}
		
		when :
			def bean = new TestListBean()
			bean.populateWith results	
		
		then :
			
			def test_a = bean.contents[0]
			def test_folder = bean.contents[1]
			def test_bob = test_folder.contents[0]
			def test_mike = test_folder.contents[1]
			 
			bean.name == 'tests'
			
			test_a.name == 'a'
			test_folder.name == 'folder'
			test_bob.name == 'bob'
			test_mike.name == 'mike'
			
			test_bob.contents == null
			test_mike.contents == null
	}
	
}