// ========================================================================
// Copyright 2010 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.annotations;

import junit.framework.TestCase;

import org.cipango.annotations.resources.AnnotedServlet;
import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlProcessor;
import org.eclipse.jetty.annotations.AnnotationParser;

public class SipServletAnnotationHandlerTest extends TestCase
{
	private SipAppContext _sac;
	private AnnotationParser _parser;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_sac = new SipAppContext();
		_sac.setServletHandler(new org.cipango.plus.servlet.SipServletHandler());
		_parser = new AnnotationParser();
        _parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipServlet",
        		new SipServletAnnotationHandler(_sac, new SipXmlProcessor(_sac)));
	}
	
	public void testAnnotedServlet() throws Exception
	{	
        _parser.parse(AnnotedServlet.class.getName(), new SimpleResolver());
        SipServletHandler handler = (SipServletHandler) _sac.getServletHandler();
        SipServletHolder[] holders = handler.getSipServlets();
        assertEquals(1, holders.length);
        assertEquals("AnnotedServlet", holders[0].getName());
        assertFalse(holders[0].isInitOnStartup());
	}
	
}
