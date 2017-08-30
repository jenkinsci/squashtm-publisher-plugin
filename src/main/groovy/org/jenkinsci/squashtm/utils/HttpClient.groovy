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
package org.jenkinsci.squashtm.utils

import net.sf.json.JSONObject
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.jenkinsci.squashtm.core.TMServer

/**
 * <p>This class wraps around the Apache httpclient library shipped with Jenkins.
 * The more groovy-friendly library 'http-builder' would have been great, however
 * it causes subtle runtime issues due to conflicting dependencies.</p>
 * 
 * <p>
 * 	Remember to close it once the job is done.
 * </p>
 * 
 * 
 * 
 */
/*
 * Much of the code has been taken from https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientPreemptiveBasicAuthentication.java
 */
class HttpClient implements Closeable{
	
	PrintStream logger
	
	CloseableHttpClient client
	HttpHost host
	HttpClientContext context 
	
	
	HttpClient(){
		
	}
	
	HttpClient(TMServer server, PrintStream logger){
		
		this.logger = logger
		
		def (hostname, port, scheme) = parseUrl(server.url)
		
		host = new HttpHost(hostname, port, scheme)
		
		// credentials : 
		CredentialsProvider credsProvider = new BasicCredentialsProvider()

		// TODO : use Jenkins credentials manager instead ?
		credsProvider.setCredentials(
			new AuthScope(host.hostName, host.port),
			new UsernamePasswordCredentials(server.login, server.password)
		)
		
		// auth cache :
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicScheme = new BasicScheme()
		
		authCache.put host, basicScheme 
		
		// context : 
		context = HttpClientContext.create()
		context.authCache = authCache
		
		// init
		client = HttpClients.custom()
							.setDefaultCredentialsProvider(credsProvider)
							.build()
							
	
	}
	
	
	/**
	 * Will post the given data to the given URL. This method will take care of serializing the object jsonData
	 * to json. Note that jsonData must be serializable.
	 *  
	 * @param url
	 * @param jsonData
	 */
	void post(String url, Object jsonData){
		
		HttpPost request = prepareRequest(url, jsonData)
		
		CloseableHttpResponse response = client.execute host, request, context
		
		try{
			logger.println response.statusLine
		}
		finally{
			response?.close()
		}
	}
	
	private HttpPost prepareRequest(String url, Object jsonData){
		
		JSONObject json = JSONObject.fromObject jsonData
		
		HttpPost request = new HttpPost(url)
		StringEntity body = new StringEntity(json.toString())
		
		request.addHeader 'Content-Type', 'application/json'
		request.entity = body
		
		return request
	}

	
	// ***************** utilities *********************
	
	Tuple parseUrl(String strUrl){
		URL url = new URL(strUrl)
		
		String host = url.host
		int port = url.port
		String scheme = url.protocol
		
		return new Tuple(host, port, scheme)
	}



	@Override
	public void close() throws IOException {
		client?.close()
	}
	
	
	
}
