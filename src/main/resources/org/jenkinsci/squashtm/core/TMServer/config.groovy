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
package org.jenkinsci.squashtm.core.TMServer


import lib.FormTagLib
import org.jenkinsci.squashtm.lang.Messages

def f = namespace(FormTagLib)

// ************************ the four fields ***********************

f.entry(title: Messages.tmpublisher_globalconf_identifier(), field:"identifier"){
	f.textbox()
}

f.entry(title : Messages.tmpublisher_globalconf_url(), field:"url"){
	f.textbox()
}

f.entry(title : Messages.tmpublisher_globalconf_login(), field : "login"){
	f.textbox()
}

f.entry(title : Messages.tmpublisher_globalconf_password(), field : "password"){
	f.password()
}

// ********************** the button pane *********************

f.entry(title:""){
	div(align:"right"){
		// see function comment below
		rawValidateButton([title : Messages.tmpublisher_globalconf_validate(),
						progress : "validating...", 
						method : "validateTMServer", 
						with : "identifier,url,login,password"])

		f.repeatableDeleteButton()
	}
	
	div(style:"display:none") {
		img(src:"${imagesURL}/spinner.gif")
		raw("validating")
		
	}
	
	div{
		
	}
}




/*
 * f.validateButton() will generate a validation button, but also a div, a validation
 * error zone etc.
 *
 *  We don't want that because we also need the f.repeatableDeleteButton in the same div.
 *
 *  The only solution is to copypasta jenkins core/src/main/resources/lib/form/validateButton.jelly
 *  and incorporate the delete button in its div container.
 *
 */
def rawValidateButton(Map args){
	input(type:"button",
		class:"yui-button validate-button",
		value:"${args.title}",
		onclick : "validateButton('${descriptor.descriptorFullUrl}/${args.method}', '${args.with}', this)"
		)
}
