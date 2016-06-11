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
package com.newname.http.client.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import com.newname.http.Header;
import com.newname.http.HeaderElement;
import com.newname.http.HttpEntity;
import com.newname.http.HttpException;
import com.newname.http.HttpResponse;
import com.newname.http.HttpResponseInterceptor;
import com.newname.http.annotation.Immutable;
import com.newname.http.client.config.RequestConfig;
import com.newname.http.client.entity.DecompressingEntity;
import com.newname.http.client.entity.DeflateInputStream;
import com.newname.http.client.entity.InputStreamFactory;
import com.newname.http.config.Lookup;
import com.newname.http.config.RegistryBuilder;
import com.newname.http.protocol.HttpContext;

/**
 * {@link HttpResponseInterceptor} responsible for processing Content-Encoding
 * responses.
 * <p>
 * Instances of this class are stateless and immutable, therefore threadsafe.
 *
 * @since 4.1
 *
 */
@Immutable
public class ResponseContentEncoding implements HttpResponseInterceptor {

    public static final String UNCOMPRESSED = "http.client.response.uncompressed";

    private final static InputStreamFactory GZIP = new InputStreamFactory() {

        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new GZIPInputStream(instream);
        }
    };

    private final static InputStreamFactory DEFLATE = new InputStreamFactory() {

        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new DeflateInputStream(instream);
        }

    };

    private final Lookup<InputStreamFactory> decoderRegistry;
    private final boolean ignoreUnknown;

    /**
     * @since 4.5
     */
    public ResponseContentEncoding(final Lookup<InputStreamFactory> decoderRegistry, final boolean ignoreUnknown) {
        this.decoderRegistry = decoderRegistry != null ? decoderRegistry :
            RegistryBuilder.<InputStreamFactory>create()
                    .register("gzip", GZIP)
                    .register("x-gzip", GZIP)
                    .register("deflate", DEFLATE)
                    .build();
        this.ignoreUnknown = ignoreUnknown;
    }

    /**
     * @since 4.5
     */
    public ResponseContentEncoding(final boolean ignoreUnknown) {
        this(null, ignoreUnknown);
    }

    /**
     * @since 4.4
     */
    public ResponseContentEncoding(final Lookup<InputStreamFactory> decoderRegistry) {
        this(decoderRegistry, true);
    }

    /**
     * Handles {@code gzip} and {@code deflate} compressed entities by using the following
     * decoders:
     * <ul>
     * <li>gzip - see {@link GZIPInputStream}</li>
     * <li>deflate - see {@link DeflateInputStream}</li>
     * </ul>
     */
    public ResponseContentEncoding() {
        this(null);
    }

    @Override
    public void process(
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();

        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final RequestConfig requestConfig = clientContext.getRequestConfig();
        // entity can be null in case of 304 Not Modified, 204 No Content or similar
        // check for zero length entity.
        if (requestConfig.isContentCompressionEnabled() && entity != null && entity.getContentLength() != 0) {
            final Header ceheader = entity.getContentEncoding();
            if (ceheader != null) {
                final HeaderElement[] codecs = ceheader.getElements();
                for (final HeaderElement codec : codecs) {
                    final String codecname = codec.getName().toLowerCase(Locale.ROOT);
                    final InputStreamFactory decoderFactory = decoderRegistry.lookup(codecname);
                    if (decoderFactory != null) {
                        response.setEntity(new DecompressingEntity(response.getEntity(), decoderFactory));
                        response.removeHeaders("Content-Length");
                        response.removeHeaders("Content-Encoding");
                        response.removeHeaders("Content-MD5");
                    } else {
                        if (!"identity".equals(codecname) && !ignoreUnknown) {
                            throw new HttpException("Unsupported Content-Encoding: " + codec.getName());
                        }
                    }
                }
            }
        }
    }

}
