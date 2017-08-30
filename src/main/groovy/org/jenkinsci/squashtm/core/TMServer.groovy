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
package org.jenkinsci.squashtm.core

import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Descriptor
import hudson.util.FormValidation
import hudson.util.FormValidation.URLCheck
import org.jenkinsci.squashtm.lang.Messages
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter

import javax.servlet.ServletException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This class represents the Squash TM servers that the plugin is aware of, at the global level.
 * Items of this type are created and persisted at the global configuration page. 
 * 
 * @author bsiri
 *
 */
// TODO : delegate the login and password to Jenkins credential manager instead ?
public final class TMServer extends AbstractDescribableImpl<TMServer>{
	
	private final static Logger LOGGER = Logger.getLogger(TMServer.class.getName());
	
	String identifier
	String url
	String login
	String password
	
	@DataBoundConstructor
	public TMServer(String identifier, String url, String login, String password){
		this.identifier = identifier;
		this.url = url;
		this.login = login;
		this.password = password;
	}
	
	
	public boolean isValid(){							
		validate() == FormValidation.OK			
	}
	

	public FormValidation validate(){
		return new TMServerValidator(this).check();
	}	
	
	@Extension
	public static final class DescriptorImpl extends Descriptor<TMServer>{
		
		@Override
		public String getDisplayName(){
			"Enter the coordinates of a TM server"
		}
		
		public FormValidation doValidateTMServer(@QueryParameter("identifier") final String identifier,
												@QueryParameter("url") final String url,
												@QueryParameter("login") final String login,
												@QueryParameter("password") final String password){
			
			TMServer serv = new TMServer(identifier, url, login, password)
			
			FormValidation validation = new TMServerValidator(serv).check()
			
			// if the validation is ok, let's return a message anyway
			if (validation == FormValidation.OK){
				return FormValidation.ok(Messages.tmpublisher_globalconf_configok())
			}
			// else return what had bee computed.
			else{				
				return validation;
			}
		}
												
												
	}
	
	
	/**
	 * That class checks that an instance of TMServer is well formed :
	 * - non empty property, 
	 * - URL can be reached 
	 */
	private static final class TMServerValidator extends URLCheck{

		private static final String SQUASH_IS_ALIVE = "Squash is Alive!"
		TMServer toCheck;
		
		public TMServerValidator(){
			super();
		}
		
		public TMServerValidator(TMServer server){
			super();
			toCheck = server; 
		}
		
		
		public FormValidation check() throws IOException, ServletException{
						
			try{
				// first check : are all properties defined ?
				arePropertiesSet()
				
				// second check : can the URL be reached ?
				isServerReachable()
			}
			catch(FormValidation validationFailed){
				return validationFailed;
			}

			/*
			 * If we reach here the remote TM server config is ok.
			 *
			 * Note : we need to return the singleton OK, not a new instance with a message.
			 * Otherwise we would break TMServer.validate(), and a proper implementation would be more verbose.
			 */
			FormValidation.OK
		}
		
		
		private void arePropertiesSet(){
			def properties = ["identifier", "url", "login", "password"]
			
			def emptyProperties = properties.findAll { 
				def val = toCheck."$it"
				(val == null) || (val.trim() == "")
			}
			
			if (! emptyProperties.empty){
				throw FormValidation.error(Messages.("tmpublisher_globalconf_errors_empty"+emptyProperties[0]) ());
			}
		}
		
		private void isServerReachable(){
			BufferedReader reader = null;
			boolean isAlive =false;
			try{
				URL url = new URL(toCheck.url+'/isSquashAlive')
				reader = open(url)
				isAlive = findText(reader, SQUASH_IS_ALIVE) 
			}
			catch(MalformedURLException ex){
				LOGGER.log(Level.CONFIG, "[TM-PLUGIN] : malformed URL exception : "+toCheck.url)
				throw FormValidation.error(Messages.tmpublisher_globalconf_errors_malformedurl())
			}
			catch(IOException ex){
				LOGGER.log(Level.CONFIG, "[TM-PLUGIN] : unreachable url : "+toCheck.url)
				throw FormValidation.warning(Messages.tmpublisher_globalconf_errors_cannotreachurl() + " : $ex.message")
			}
			finally{
				if (reader != null){
					reader.close();
				}
			}
			
			if (! isAlive){
				throw FormValidation.warning(Messages.tmpublisher_globalconf_errors_serverdown())
			}
		}

		
	}
	
}