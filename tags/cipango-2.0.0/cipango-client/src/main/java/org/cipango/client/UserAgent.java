// ========================================================================
// Copyright 2011 NEXCOM Systems
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

package org.cipango.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.cipango.sip.NameAddr;
import org.cipango.sip.SipHeaders;
import org.cipango.sip.SipMethods;

public class UserAgent
{
	private Address _contact;
	private Address _localAddress;
	private SipFactory _factory;
	
	private SipSession _registerSession;
	
	private String _user = "thomas22";
	private String _passwd = "thomas22300";
	
	public UserAgent(SipURI aor)
	{
		_localAddress = new NameAddr(aor);
	}
	
	public Address getLocalAddress()
	{
		return _localAddress;
	}
	
	public void setFactory(SipFactory factory)
	{
		_factory = factory;
	}
	
	public void setContact(Address contact)
	{
		_contact = contact;
	}
	
	public void handleResponse(SipServletResponse response)
	{
		if (response.getMethod().equalsIgnoreCase(SipMethods.REGISTER))
		{
			handleRegisterResponse(response);
		}
	}
	
	public void handleRequest(SipServletRequest request)
	{
		
	}
	
	public SipServletRequest createRequest(String method, Address destination)
	{
		SipApplicationSession appSession = _factory.createApplicationSession();
		SipServletRequest request = _factory.createRequest(appSession, method, _localAddress, destination);
		request.addHeader(SipHeaders.USER_AGENT, "Cipango-Client");
		return request;
	}

	public SipServletRequest createRequest(String method, String destination) throws ServletParseException
	{
		return createRequest(method, _factory.createAddress(destination));
	}

	public SipServletRequest createRequest(SipSession session, String method)
	{
		SipServletRequest request = session.createRequest(method);
		request.addHeader(SipHeaders.USER_AGENT, "Cipango-Client");
		return request;
	}
	
	public boolean isRegistered()
	{
		if (_registerSession != null)
		{
			Long expiryTime = (Long) _registerSession.getAttribute("expiryTime");
			if (expiryTime != null)
				return expiryTime.longValue() > System.currentTimeMillis();
		}
		return false;
	}
	
	public synchronized void register() throws IOException
	{
		if (_registerSession == null)
		{
			SipServletRequest register = createRegister(null);
			_registerSession = register.getSession();
			register.send();
		}
	}
	
	protected SipServletRequest createRegister(SipSession session) 
	{
		SipServletRequest register;
		if (session == null)
			register = createRequest(SipMethods.REGISTER, _localAddress);
		else
			register = createRequest(session, SipMethods.REGISTER);
			
		SipURI registrar = _factory.createSipURI(null, ((SipURI) _localAddress.getURI()).getHost());
		register.setRequestURI(registrar);
		register.setAddressHeader(SipHeaders.CONTACT, _contact);
		register.setExpires(3600);
		
		return register;
	}
	
	public synchronized void handleRegisterResponse(SipServletResponse response)
	{
		if (_registerSession != null)
		{
			int status = response.getStatus();
			if (status == SipServletResponse.SC_OK)
			{
				try
				{
					int expires = response.getExpires();
				
					if (expires == -1)
					{
						Address contact = response.getAddressHeader(SipHeaders.CONTACT);
						expires = contact.getExpires();
					}
					long expiryTime = System.currentTimeMillis() + expires * 1000l;
					_registerSession.setAttribute("expiryTime", expiryTime);
					
					System.out.println("expires " + expires);
				}
				catch (Exception e)
				{
					// registration failure
				}
			}
			else if (status == SipServletResponse.SC_UNAUTHORIZED)
			{
				if (response.getRequest().getHeader(SipHeaders.AUTHORIZATION) == null)
				{
					SipServletRequest register = createRegister(_registerSession);
					register.addAuthHeader(response, _user, _passwd);
					try
					{
						register.send();
					}
					catch (Exception e)
					{
						// registration failed
					}
				}
				else 
				{
					System.out.println(status);
					// stale ?
					System.out.println("registration failed");
				}
			}
		}
	}
	
	public String toString()
	{
		return _localAddress + "[" + _contact + "]";
	}
}