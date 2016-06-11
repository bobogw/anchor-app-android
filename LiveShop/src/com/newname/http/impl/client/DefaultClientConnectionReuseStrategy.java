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

import com.newname.http.Header;
import com.newname.http.HttpHeaders;
import com.newname.http.HttpRequest;
import com.newname.http.HttpResponse;
import com.newname.http.TokenIterator;
import com.newname.http.impl.DefaultConnectionReuseStrategy;
import com.newname.http.message.BasicHeaderIterator;
import com.newname.http.message.BasicTokenIterator;
import com.newname.http.protocol.HTTP;
import com.newname.http.protocol.HttpContext;
import com.newname.http.protocol.HttpCoreContext;

public class DefaultClientConnectionReuseStrategy extends DefaultConnectionReuseStrategy {

    public static final DefaultClientConnectionReuseStrategy INSTANCE = new DefaultClientConnectionReuseStrategy();

    @Override
    public boolean keepAlive(final HttpResponse response, final HttpContext context) {

        final HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        if (request != null) {
            final Header[] connHeaders = request.getHeaders(HttpHeaders.CONNECTION);
            if (connHeaders.length != 0) {
                final TokenIterator ti = new BasicTokenIterator(new BasicHeaderIterator(connHeaders, null));
                while (ti.hasNext()) {
                    final String token = ti.nextToken();
                    if (HTTP.CONN_CLOSE.equalsIgnoreCase(token)) {
                        return false;
                    }
                }
            }
        }
        return super.keepAlive(response, context);
    }

}
