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

package com.newname.http.impl.client;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.newname.http.Header;
import com.newname.http.HttpHost;
import com.newname.http.HttpResponse;
import com.newname.http.annotation.Immutable;
import com.newname.http.auth.AuthOption;
import com.newname.http.auth.AuthScheme;
import com.newname.http.auth.AuthScope;
import com.newname.http.auth.AuthenticationException;
import com.newname.http.auth.Credentials;
import com.newname.http.auth.MalformedChallengeException;
import com.newname.http.client.AuthCache;
import com.newname.http.client.AuthenticationHandler;
import com.newname.http.client.AuthenticationStrategy;
import com.newname.http.client.CredentialsProvider;
import com.newname.http.client.params.AuthPolicy;
import com.newname.http.client.protocol.ClientContext;
import com.newname.http.protocol.HttpContext;
import com.newname.http.util.Args;

/**
 * @deprecated (4.2) do not use
 */
@Immutable
@Deprecated
class AuthenticationStrategyAdaptor implements AuthenticationStrategy {

    private final Log log = LogFactory.getLog(getClass());

    private final AuthenticationHandler handler;

    public AuthenticationStrategyAdaptor(final AuthenticationHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public boolean isAuthenticationRequested(
            final HttpHost authhost,
            final HttpResponse response,
            final HttpContext context) {
        return this.handler.isAuthenticationRequested(response, context);
    }

    @Override
    public Map<String, Header> getChallenges(
            final HttpHost authhost,
            final HttpResponse response,
            final HttpContext context) throws MalformedChallengeException {
        return this.handler.getChallenges(response, context);
    }

    @Override
    public Queue<AuthOption> select(
            final Map<String, Header> challenges,
            final HttpHost authhost,
            final HttpResponse response,
            final HttpContext context) throws MalformedChallengeException {
        Args.notNull(challenges, "Map of auth challenges");
        Args.notNull(authhost, "Host");
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");

        final Queue<AuthOption> options = new LinkedList<AuthOption>();
        final CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                ClientContext.CREDS_PROVIDER);
        if (credsProvider == null) {
            this.log.debug("Credentials provider not set in the context");
            return options;
        }

        final AuthScheme authScheme;
        try {
            authScheme = this.handler.selectScheme(challenges, response, context);
        } catch (final AuthenticationException ex) {
            if (this.log.isWarnEnabled()) {
                this.log.warn(ex.getMessage(), ex);
            }
            return options;
        }
        final String id = authScheme.getSchemeName();
        final Header challenge = challenges.get(id.toLowerCase(Locale.ROOT));
        authScheme.processChallenge(challenge);

        final AuthScope authScope = new AuthScope(
                authhost.getHostName(),
                authhost.getPort(),
                authScheme.getRealm(),
                authScheme.getSchemeName());

        final Credentials credentials = credsProvider.getCredentials(authScope);
        if (credentials != null) {
            options.add(new AuthOption(authScheme, credentials));
        }
        return options;
    }

    @Override
    public void authSucceeded(
            final HttpHost authhost, final AuthScheme authScheme, final HttpContext context) {
        AuthCache authCache = (AuthCache) context.getAttribute(ClientContext.AUTH_CACHE);
        if (isCachable(authScheme)) {
            if (authCache == null) {
                authCache = new BasicAuthCache();
                context.setAttribute(ClientContext.AUTH_CACHE, authCache);
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Caching '" + authScheme.getSchemeName() +
                        "' auth scheme for " + authhost);
            }
            authCache.put(authhost, authScheme);
        }
    }

    @Override
    public void authFailed(
            final HttpHost authhost, final AuthScheme authScheme, final HttpContext context) {
        final AuthCache authCache = (AuthCache) context.getAttribute(ClientContext.AUTH_CACHE);
        if (authCache == null) {
            return;
        }
        if (this.log.isDebugEnabled()) {
            this.log.debug("Removing from cache '" + authScheme.getSchemeName() +
                    "' auth scheme for " + authhost);
        }
        authCache.remove(authhost);
    }

    private boolean isCachable(final AuthScheme authScheme) {
        if (authScheme == null || !authScheme.isComplete()) {
            return false;
        }
        final String schemeName = authScheme.getSchemeName();
        return schemeName.equalsIgnoreCase(AuthPolicy.BASIC) ||
                schemeName.equalsIgnoreCase(AuthPolicy.DIGEST);
    }

    public AuthenticationHandler getHandler() {
        return this.handler;
    }

}
