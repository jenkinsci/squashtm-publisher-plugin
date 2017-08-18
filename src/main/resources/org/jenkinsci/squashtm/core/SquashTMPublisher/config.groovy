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
package org.jenkinsci.squashtm.core.SquashTMPublisher


import lib.FormTagLib
import org.jenkinsci.squashtm.lang.Messages



/*
 * TODO : the following will be used for the active mode (once implemented). If cannot be implemented soon, move this to a
 * feature branch. Until then, the generate command is commented
 */
//displayConfPanel()




// **************** utilities *************************

def displayConfPanel(){

	def f = namespace(FormTagLib)

/*
 * the descriptor is gracefully provided by Jenkins in the page context,
 * see https://wiki.jenkins-ci.org/display/JENKINS/Basic+guide+to+Jelly+usage+in+Jenkins#BasicguidetoJellyusageinJenkins-Otherpredefinedobjects
 */
	def availableServers = descriptor.tmServers

	/*
	 * The following leverages https://wiki.jenkins-ci.org/display/JENKINS/Structured+Form+Submission
	 */
	f.entry(title: Messages.tmpublisher_job_serverstonotify(), field : "selectedServers") {

		div(style:"padding-left:2em;",{
			/*
             *  first, a fake checkbox that will always be there, and ensure that
             *  the data for attribute 'selectedServers' will always be an array.
             */
			div(name:"selectedServers", style : "display:none", {
				input(type:"text", name : "identifier", value : "whatever")
				input(type:"checkbox", name : "selected")
			})

			/*
             * Now, the actual checkboxes
             */
			availableServers.each{
				def server = it
				div(name:"selectedServers", {
					input(type:"hidden", name : "identifier", value : server.identifier)
					f.checkbox(name:"selected", title: server.identifier, checked : shouldCheck(server))
				})
			}
		})
	}
}

def shouldCheck(server){
	// case of a new instance
	if (instance == null){
		return false
	}
	else {
		def selected = instance.selectedServers
		def identifiers = selected?.collect{
			it.identifier
		}
		def isIn = (server.identifier in identifiers)
		return isIn
	}
}
