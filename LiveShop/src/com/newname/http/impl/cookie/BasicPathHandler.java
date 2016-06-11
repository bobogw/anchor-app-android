/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.newname.http.impl.cookie;

import com.newname.http.annotation.Immutable;
import com.newname.http.cookie.ClientCookie;
import com.newname.http.cookie.CommonCookieAttributeHandler;
import com.newname.http.cookie.Cookie;
import com.newname.http.cookie.CookieOrigin;
import com.newname.http.cookie.CookieRestrictionViolationException;
import com.newname.http.cookie.MalformedCookieException;
import com.newname.http.cookie.SetCookie;
import com.newname.http.util.Args;
import com.newname.http.util.TextUtils;

/**
 *
 * @since 4.0
 */
@Immutable
public class BasicPathHandler implements CommonCookieAttributeHandler {

    public BasicPathHandler() {
        super();
    }

    @Override
    public void parse(
            final SetCookie cookie, final String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        cookie.setPath(!TextUtils.isBlank(value) ? value : "/");
    }

    @Override
    public void validate(final Cookie cookie, final CookieOrigin origin)
            throws MalformedCookieException {
        if (!match(cookie, origin)) {
            throw new CookieRestrictionViolationException(
                "Illegal 'path' attribute \"" + cookie.getPath()
                + "\". Path of origin: \"" + origin.getPath() + "\"");
        }
    }

    static boolean pathMatch(final String uriPath, final String cookiePath) {
        String normalizedCookiePath = cookiePath;
        if (normalizedCookiePath == null) {
            normalizedCookiePath = "/";
        }
        if (normalizedCookiePath.length() > 1 && normalizedCookiePath.endsWith("/")) {
            normalizedCookiePath = normalizedCookiePath.substring(0, normalizedCookiePath.length() - 1);
        }
        if (uriPath.startsWith(normalizedCookiePath)) {
            if (normalizedCookiePath.equals("/")) {
                return true;
            }
            if (uriPath.length() == normalizedCookiePath.length()) {
                return true;
            }
            if (uriPath.charAt(normalizedCookiePath.length()) == '/') {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        return pathMatch(origin.getPath(), cookie.getPath());
    }

    @Override
    public String getAttributeName() {
        return ClientCookie.PATH_ATTR;
    }

}