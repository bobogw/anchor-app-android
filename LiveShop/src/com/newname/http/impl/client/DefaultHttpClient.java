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

import com.newname.http.params.HttpConnectionParams;
import com.newname.http.params.HttpParams;

import com.newname.http.HttpVersion;
import com.newname.http.annotation.ThreadSafe;
import com.newname.http.client.protocol.RequestAddCookies;
import com.newname.http.client.protocol.RequestAuthCache;
import com.newname.http.client.protocol.RequestClientConnControl;
import com.newname.http.client.protocol.RequestDefaultHeaders;
import com.newname.http.client.protocol.RequestProxyAuthentication;
import com.newname.http.client.protocol.RequestTargetAuthentication;
import com.newname.http.client.protocol.ResponseProcessCookies;
import com.newname.http.conn.ClientConnectionManager;
import com.newname.http.params.HttpProtocolParams;
import com.newname.http.params.SyncBasicHttpParams;
import com.newname.http.protocol.BasicHttpProcessor;
import com.newname.http.protocol.HTTP;
import com.newname.http.protocol.RequestContent;
import com.newname.http.protocol.RequestExpectContinue;
import com.newname.http.protocol.RequestTargetHost;
import com.newname.http.protocol.RequestUserAgent;
import com.newname.http.util.VersionInfo;

/**
 * Default implementation of {@link com.newname.http.client.HttpClient} pre-configured
 * for most common use scenarios.
 * <p>
 * Please see the Javadoc for {@link #createHttpProcessor()} for the details of the interceptors
 * that are set up by default.
 * <p>
 * Additional interceptors can be added as follows, but
 * take care not to add the same interceptor more than once.
 * <pre>
 * DefaultHttpClient httpclient = new DefaultHttpClient();
 * httpclient.addRequestInterceptor(new RequestAcceptEncoding());
 * httpclient.addResponseInterceptor(new ResponseContentEncoding());
 * </pre>
 * <p>
 * This class sets up the following parameters if not explicitly set:
 * <ul>
 * <li>Version: HttpVersion.HTTP_1_1</li>
 * <li>ContentCharset: HTTP.DEFAULT_CONTENT_CHARSET</li>
 * <li>NoTcpDelay: true</li>
 * <li>SocketBufferSize: 8192</li>
 * <li>UserAgent: Apache-HttpClient/release (java 1.5)</li>
 * </ul>
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link com.newname.http.params.CoreProtocolPNames#PROTOCOL_VERSION}</li>
 *  <li>{@link com.newname.http.params.CoreProtocolPNames#STRICT_TRANSFER_ENCODING}</li>
 *  <li>{@link com.newname.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
 *  <li>{@link com.newname.http.params.CoreProtocolPNames#USE_EXPECT_CONTINUE}</li>
 *  <li>{@link com.newname.http.params.CoreProtocolPNames#WAIT_FOR_CONTINUE}</li>
 *  <li>{@link com.newname.http.params.CoreProtocolPNames#USER_AGENT}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#TCP_NODELAY}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#SO_LINGER}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#SO_REUSEADDR}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#MAX_HEADER_COUNT}</li>
 *  <li>{@link com.newname.http.params.CoreConnectionPNames#STALE_CONNECTION_CHECK}</li>
 *  <li>{@link com.newname.http.conn.params.ConnRoutePNames#FORCED_ROUTE}</li>
 *  <li>{@link com.newname.http.conn.params.ConnRoutePNames#LOCAL_ADDRESS}</li>
 *  <li>{@link com.newname.http.conn.params.ConnRoutePNames#DEFAULT_PROXY}</li>
 *  <li>{@link com.newname.http.cookie.params.CookieSpecPNames#DATE_PATTERNS}</li>
 *  <li>{@link com.newname.http.cookie.params.CookieSpecPNames#SINGLE_COOKIE_HEADER}</li>
 *  <li>{@link com.newname.http.auth.params.AuthPNames#CREDENTIAL_CHARSET}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#COOKIE_POLICY}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#HANDLE_AUTHENTICATION}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#HANDLE_REDIRECTS}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#MAX_REDIRECTS}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#ALLOW_CIRCULAR_REDIRECTS}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#VIRTUAL_HOST}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#DEFAULT_HOST}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#DEFAULT_HEADERS}</li>
 *  <li>{@link com.newname.http.client.params.ClientPNames#CONN_MANAGER_TIMEOUT}</li>
 * </ul>
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link HttpClientBuilder} see also {@link CloseableHttpClient}.
 */
@ThreadSafe
@Deprecated
public class DefaultHttpClient extends AbstractHttpClient {

    /**
     * Creates a new HTTP client from parameters and a connection manager.
     *
     * @param params    the parameters
     * @param conman    the connection manager
     */
    public DefaultHttpClient(
            final ClientConnectionManager conman,
            final HttpParams params) {
        super(conman, params);
    }


    /**
     * @since 4.1
     */
    public DefaultHttpClient(
            final ClientConnectionManager conman) {
        super(conman, null);
    }


    public DefaultHttpClient(final HttpParams params) {
        super(null, params);
    }


    public DefaultHttpClient() {
        super(null, null);
    }


    /**
     * Creates the default set of HttpParams by invoking {@link DefaultHttpClient#setDefaultHttpParams(HttpParams)}
     *
     * @return a new instance of {@link SyncBasicHttpParams} with the defaults applied to it.
     */
    @Override
    protected HttpParams createHttpParams() {
        final HttpParams params = new SyncBasicHttpParams();
        setDefaultHttpParams(params);
        return params;
    }

    /**
     * Saves the default set of HttpParams in the provided parameter.
     * These are:
     * <ul>
     * <li>{@link com.newname.http.params.CoreProtocolPNames#PROTOCOL_VERSION}:
     *   1.1</li>
     * <li>{@link com.newname.http.params.CoreProtocolPNames#HTTP_CONTENT_CHARSET}:
     *   ISO-8859-1</li>
     * <li>{@link com.newname.http.params.CoreConnectionPNames#TCP_NODELAY}:
     *   true</li>
     * <li>{@link com.newname.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}:
     *   8192</li>
     * <li>{@link com.newname.http.params.CoreProtocolPNames#USER_AGENT}:
     *   Apache-HttpClient (Java 1.5)</li>
     * </ul>
     */
    public static void setDefaultHttpParams(final HttpParams params) {
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEF_CONTENT_CHARSET.name());
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, VersionInfo.getUserAgent("Apache-HttpClient",
                "com.newname.http.client", DefaultHttpClient.class));
    }

    /**
    * Create the processor with the following interceptors:
    * <ul>
    * <li>{@link RequestDefaultHeaders}</li>
    * <li>{@link RequestContent}</li>
    * <li>{@link RequestTargetHost}</li>
    * <li>{@link RequestClientConnControl}</li>
    * <li>{@link RequestUserAgent}</li>
    * <li>{@link RequestExpectContinue}</li>
    * <li>{@link RequestAddCookies}</li>
    * <li>{@link ResponseProcessCookies}</li>
    * <li>{@link RequestAuthCache}</li>
    * <li>{@link RequestTargetAuthentication}</li>
    * <li>{@link RequestProxyAuthentication}</li>
    * </ul>
    * <p>
    * @return the processor with the added interceptors.
    */
    @Override
    protected BasicHttpProcessor createHttpProcessor() {
        final BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new RequestDefaultHeaders());
        // Required protocol interceptors
        httpproc.addInterceptor(new RequestContent());
        httpproc.addInterceptor(new RequestTargetHost());
        // Recommended protocol interceptors
        httpproc.addInterceptor(new RequestClientConnControl());
        httpproc.addInterceptor(new RequestUserAgent());
        httpproc.addInterceptor(new RequestExpectContinue());
        // HTTP state management interceptors
        httpproc.addInterceptor(new RequestAddCookies());
        httpproc.addInterceptor(new ResponseProcessCookies());
        // HTTP authentication interceptors
        httpproc.addInterceptor(new RequestAuthCache());
        httpproc.addInterceptor(new RequestTargetAuthentication());
        httpproc.addInterceptor(new RequestProxyAuthentication());
        return httpproc;
    }

}
