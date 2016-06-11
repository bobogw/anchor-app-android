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

package com.newname.http.impl.conn;


import java.net.InetAddress;

import com.newname.http.HttpException;
import com.newname.http.HttpHost;
import com.newname.http.HttpRequest;
import com.newname.http.annotation.ThreadSafe;
import com.newname.http.conn.params.ConnRouteParams;
import com.newname.http.conn.routing.HttpRoute;
import com.newname.http.conn.routing.HttpRoutePlanner;
import com.newname.http.conn.scheme.Scheme;
import com.newname.http.conn.scheme.SchemeRegistry;
import com.newname.http.protocol.HttpContext;
import com.newname.http.util.Args;
import com.newname.http.util.Asserts;

/**
 * Default implementation of an {@link HttpRoutePlanner}. This implementation
 * is based on {@link com.newname.http.conn.params.ConnRoutePNames parameters}.
 * It will not make use of any Java system properties, nor of system or
 * browser proxy settings.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link com.newname.http.conn.params.ConnRoutePNames#DEFAULT_PROXY}</li>
 *  <li>{@link com.newname.http.conn.params.ConnRoutePNames#LOCAL_ADDRESS}</li>
 *  <li>{@link com.newname.http.conn.params.ConnRoutePNames#FORCED_ROUTE}</li>
 * </ul>
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link DefaultRoutePlanner}
 */
@ThreadSafe
@Deprecated
public class DefaultHttpRoutePlanner implements HttpRoutePlanner {

    /** The scheme registry. */
    protected final SchemeRegistry schemeRegistry; // class is @ThreadSafe

    /**
     * Creates a new default route planner.
     *
     * @param schreg    the scheme registry
     */
    public DefaultHttpRoutePlanner(final SchemeRegistry schreg) {
        Args.notNull(schreg, "Scheme registry");
        schemeRegistry = schreg;
    }

    @Override
    public HttpRoute determineRoute(final HttpHost target,
                                    final HttpRequest request,
                                    final HttpContext context)
        throws HttpException {

        Args.notNull(request, "HTTP request");

        // If we have a forced route, we can do without a target.
        HttpRoute route =
            ConnRouteParams.getForcedRoute(request.getParams());
        if (route != null) {
            return route;
        }

        // If we get here, there is no forced route.
        // So we need a target to compute a route.

        Asserts.notNull(target, "Target host");

        final InetAddress local =
            ConnRouteParams.getLocalAddress(request.getParams());
        final HttpHost proxy =
            ConnRouteParams.getDefaultProxy(request.getParams());

        final Scheme schm;
        try {
            schm = this.schemeRegistry.getScheme(target.getSchemeName());
        } catch (final IllegalStateException ex) {
            throw new HttpException(ex.getMessage());
        }
        // as it is typically used for TLS/SSL, we assume that
        // a layered scheme implies a secure connection
        final boolean secure = schm.isLayered();

        if (proxy == null) {
            route = new HttpRoute(target, local, secure);
        } else {
            route = new HttpRoute(target, local, proxy, secure);
        }
        return route;
    }

}
