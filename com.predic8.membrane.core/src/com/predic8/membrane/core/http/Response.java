/* Copyright 2009 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.predic8.membrane.core.Constants;
import com.predic8.membrane.core.util.EndOfStreamException;
import com.predic8.membrane.core.util.HttpUtil;

public class Response extends Message {

	protected static Log log = LogFactory.getLog(Response.class.getName());

	private int statusCode;

	private String statusMessage;

	private static Pattern pattern = Pattern.compile("HTTP/(.+?) (.+?) (.+?)$");

	@Override
	public String getStartLine() {
		return "HTTP/" + version + " " + statusCode + " " + statusMessage
				+ Constants.CRLF;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public void parseStartLine(InputStream in) throws IOException, EndOfStreamException {

		Matcher matcher = pattern.matcher(HttpUtil.readLine(in));
		boolean find = matcher.find();

		if (!find) {
			return;
		}
		version = matcher.group(1);
		statusCode = Integer.parseInt(matcher.group(2));
		statusMessage = matcher.group(3);

	}

	public void read(InputStream in, boolean createBody) throws IOException, EndOfStreamException {
		parseStartLine(in);
		
		if (getStatusCode() == 100) {
			HttpUtil.readLine(in);
			return;
		}

		header = new Header(in, new StringBuffer());
		
		if (createBody)
			createBody(in);
	}

	protected void createBody(InputStream in) throws IOException {
		if (isRedirect())
			return;
		super.createBody(in);
	}

	public boolean isRedirect() {
		return statusCode >= 300 && statusCode < 400;
	}

	@Override
	public String getName() {
		return " " + statusCode;
	}
	
	@Override
	public boolean hasBody() {
		if (statusCode == 100) 
			return false;
		
		return super.hasBody();
	}
	
}