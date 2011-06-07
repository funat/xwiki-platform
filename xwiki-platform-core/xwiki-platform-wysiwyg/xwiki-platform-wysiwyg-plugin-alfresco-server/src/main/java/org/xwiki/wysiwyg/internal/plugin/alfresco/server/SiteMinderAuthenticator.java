/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.wysiwyg.internal.plugin.alfresco.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.wysiwyg.plugin.alfresco.server.Authenticator;

/**
 * An {@link Authenticator} implementation based on SiteMinder cookies.
 * 
 * @version $Id$
 */
@Component("siteMinder")
public class SiteMinderAuthenticator implements Authenticator
{
    /**
     * The component used to access the current HTTP request, from where we copy the SiteMinder cookies.
     */
    @Requirement
    private Container container;

    /**
     * {@inheritDoc}
     * 
     * @see Authenticator#authenticate(HttpRequestBase)
     */
    public void authenticate(HttpRequestBase request)
    {
        javax.servlet.http.Cookie[] receivedCookies =
            ((ServletRequest) container.getRequest()).getHttpServletRequest().getCookies();
        List<Cookie> cookies = new ArrayList<Cookie>();
        // Copy cookies.
        for (int i = 0; i < receivedCookies.length; i++) {
            javax.servlet.http.Cookie receivedCookie = receivedCookies[i];
            // Skip JSESSIONID because it is used to authenticate the received servlet request.
            if ("JSESSIONID".equals(receivedCookie.getName())) {
                continue;
            }
            BasicClientCookie cookie = new BasicClientCookie(receivedCookie.getName(), receivedCookie.getValue());
            cookie.setVersion(receivedCookie.getVersion());
            cookie.setDomain(receivedCookie.getDomain());
            cookie.setPath(receivedCookie.getPath());
            cookie.setSecure(receivedCookie.getSecure());
            // Set attributes EXACTLY as sent by the browser.
            cookie.setAttribute(ClientCookie.VERSION_ATTR, String.valueOf(receivedCookie.getVersion()));
            cookie.setAttribute(ClientCookie.DOMAIN_ATTR, receivedCookie.getDomain());
            cookies.add(cookie);
        }
        BrowserCompatSpec cookieSpec = new BrowserCompatSpec();
        for (Header header : cookieSpec.formatCookies(cookies)) {
            request.addHeader(header);
        }
    }
}
