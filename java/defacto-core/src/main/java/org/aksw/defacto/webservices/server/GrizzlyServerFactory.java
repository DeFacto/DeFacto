/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.aksw.defacto.webservices.server;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * Factory for creating Grizzly 2 {@link HttpServer} instances.
 * The copy of {@link com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory},
 * but this factory doesn't automatically start the Grizzly {@link HttpServer},
 * so developers still have a chance to make configuration changes.
 *
 * @author Matt Swift
 * @author Grizzly team
 */
public final class GrizzlyServerFactory {

    /**
     * {@link ResourceConfig feature to enable encoded slashes in URIs.
     * If set to false (the default behavior), requests containing encoded slashes
     * will get rejected by Grizzly and will never make it to the Jersey runtime.
     */
    public final static String FEATURE_ALLOW_ENCODED_SLASH = "com.sun.jersey.api.container.grizzly.AllowEncodedSlashFeature";

    /**
     * Creates a new {@link HttpServer} which will manage all root resource and
     * provider classes found by searching the classes referenced in the java
     * classpath.
     * <p>
     * The returned implementation defers to the
     * {@link ContainerFactory#createContainer(Class)} method for creation of the
     * {@code HttpServer}.
     *
     * @param u
     *          The URI to create the {@code HttpServer}. The URI scheme must be
     *          equal to "http". The URI user information and host are ignored. If
     *          the URI port is not present then port 80 will be used. The URI
     *          query and fragment components are ignored.
     * @return The new {@link HttpServer} which will manage all root resource and
     *         provider classes found by searching the classes referenced in the
     *         java classpath.
     * @throws IOException
     *           If an error occurs while creating the container.
     * @throws IllegalArgumentException
     *           If {@code u} does not have the scheme "http" or if the path does
     *           not begin with a "/".
     * @throws NullPointerException
     *           If {@code u} was {@code null}.
     */
    public static HttpServer createHttpServer(final String u) throws IOException,
            IllegalArgumentException, NullPointerException {
        if (u == null) {
            throw new NullPointerException("The URI must not be null");
        }

        return createHttpServer(URI.create(u));
    }

    /**
     * Creates a new {@link HttpServer} which will manage all root resource and
     * provider classes declared by the resource configuration.
     * <p>
     * The returned implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method for
     * creation of the {@code HttpServer}.
     *
     * @param u
     *          The URI to create the {@code HttpServer}. The URI scheme must be
     *          equal to "http". The URI user information and host are ignored. If
     *          the URI port is not present then port 80 will be used. The URI
     *          query and fragment components are ignored.
     * @param rc
     *          The resource configuration.
     * @return The new {@link HttpServer} which will manage all root resource and
     *         provider classes found by searching the classes referenced in the
     *         java classpath.
     * @throws IOException
     *           If an error occurs while creating the container.
     * @throws IllegalArgumentException
     *           If {@code u} does not have the scheme "http" or if the path does
     *           not begin with a "/".
     * @throws NullPointerException
     *           If {@code u} was {@code null}.
     */
    public static HttpServer createHttpServer(
            final String u,
            final ResourceConfig rc) throws IOException, IllegalArgumentException,
            NullPointerException {
        if (u == null) {
            throw new NullPointerException("The URI must not be null");
        }

        return createHttpServer(URI.create(u), rc);
    }

    /**
     * Creates a new {@link HttpServer} which will manage all root resource and
     * provider classes declared by the resource configuration.
     * <p>
     * The returned implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig,
     * IoCComponentProviderFactory)}
     * method for creation of the {@code HttpServer}.
     *
     * @param u
     *          The URI to create the {@code HttpServer}. The URI scheme must be
     *          equal to "http". The URI user information and host are ignored. If
     *          the URI port is not present then port 80 will be used. The URI
     *          query and fragment components are ignored.
     * @param rc
     *          The resource configuration.
     * @param factory
     *          The IoC component provider factory the web application delegates
     *          to for obtaining instances of resource and provider classes. May
     *          be {@code null} if the web application is responsible for
     *          instantiating resource and provider classes.
     * @return The new {@link HttpServer} which will manage all root resource and
     *         provider classes found by searching the classes referenced in the
     *         java classpath.
     * @throws IOException
     *           If an error occurs while creating the container.
     * @throws IllegalArgumentException
     *           If {@code u} does not have the scheme "http" or if the path does
     *           not begin with a "/".
     * @throws NullPointerException
     *           If {@code u} was {@code null}.
     */
    public static HttpServer createHttpServer(
            final String u,
            final ResourceConfig rc,
            final IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException, NullPointerException {
        if (u == null) {
            throw new NullPointerException("The URI must not be null");
        }

        return createHttpServer(URI.create(u), rc, factory);
    }

    /**
     * Creates a new {@link HttpServer} which will manage all root resource and
     * provider classes found by searching the classes referenced in the java
     * classpath.
     * <p>
     * The returned implementation defers to the
     * {@link ContainerFactory#createContainer(Class)} method for creation of the
     * {@code HttpServer}.
     *
     * @param u
     *          The URI to create the {@code HttpServer}. The URI scheme must be
     *          equal to "http". The URI user information and host are ignored. If
     *          the URI port is not present then port 80 will be used. The URI
     *          query and fragment components are ignored.
     * @return The new {@link HttpServer} which will manage all root resource and
     *         provider classes found by searching the classes referenced in the
     *         java classpath.
     * @throws IOException
     *           If an error occurs while creating the container.
     * @throws IllegalArgumentException
     *           If {@code u} does not have the scheme "http" or if the path does
     *           not begin with a "/".
     * @throws NullPointerException
     *           If {@code u} was {@code null}.
     */
    public static HttpServer createHttpServer(final URI u) throws IOException,
            IllegalArgumentException, NullPointerException {
        final HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class);
        return createHttpServer(u, handler);
    }

    /**
     * Creates a new {@link HttpServer} which will manage all root resource and
     * provider classes declared by the resource configuration.
     * <p>
     * The returned implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig)} method for
     * creation of the {@code HttpServer}.
     *
     * @param u
     *          The URI to create the {@code HttpServer}. The URI scheme must be
     *          equal to "http". The URI user information and host are ignored. If
     *          the URI port is not present then port 80 will be used. The URI
     *          query and fragment components are ignored.
     * @param rc
     *          The resource configuration.
     * @return The new {@link HttpServer} which will manage all root resource and
     *         provider classes found by searching the classes referenced in the
     *         java classpath.
     * @throws IOException
     *           If an error occurs while creating the container.
     * @throws IllegalArgumentException
     *           If {@code u} does not have the scheme "http" or if the path does
     *           not begin with a "/".
     * @throws NullPointerException
     *           If {@code u} was {@code null}.
     */
    public static HttpServer createHttpServer(final URI u, final ResourceConfig rc)
            throws IOException, IllegalArgumentException, NullPointerException {
        final HttpHandler handler = ContainerFactory.createContainer(
                HttpHandler.class, rc);
        return createHttpServer(u, handler);
    }

    /**
     * Creates a new {@link HttpServer} which will manage all root resource and
     * provider classes declared by the resource configuration.
     * <p>
     * The returned implementation defers to the
     * {@link ContainerFactory#createContainer(Class, ResourceConfig,
     * IoCComponentProviderFactory)}
     * method for creation of the {@code HttpServer}.
     *
     * @param u
     *          The URI to create the {@code HttpServer}. The URI scheme must be
     *          equal to "http". The URI user information and host are ignored. If
     *          the URI port is not present then port 80 will be used. The URI
     *          query and fragment components are ignored.
     * @param rc
     *          The resource configuration.
     * @param factory
     *          The IoC component provider factory the web application delegates
     *          to for obtaining instances of resource and provider classes. May
     *          be {@code null} if the web application is responsible for
     *          instantiating resource and provider classes.
     * @return The new {@link HttpServer} which will manage all root resource and
     *         provider classes found by searching the classes referenced in the
     *         java classpath.
     * @throws IOException
     *           If an error occurs while creating the container.
     * @throws IllegalArgumentException
     *           If {@code u} does not have the scheme "http" or if the path does
     *           not begin with a "/".
     * @throws NullPointerException
     *           If {@code u} was {@code null}.
     */
    public static HttpServer createHttpServer(
            final URI u,
            final ResourceConfig rc,
            final IoCComponentProviderFactory factory)
            throws IOException, IllegalArgumentException, NullPointerException {
        final HttpHandler processor = ContainerFactory.createContainer(
                HttpHandler.class, rc, factory);
        return createHttpServer(u, processor);
    }

    public static HttpServer createHttpServer(
            final URI u,
            final HttpHandler handler,
            final boolean secure,
            final SSLEngineConfigurator sslEngineConfigurator
    ) throws IOException,
            IllegalArgumentException, NullPointerException {

        if (u == null) {
            throw new NullPointerException("The URI must not be null");
        }

        final String scheme = u.getScheme();
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("The URI scheme, of the URI " + u
                    + ", must be equal (ignoring case) to 'http' or 'https'");
        }

        final String host = (u.getHost() == null) ? NetworkListener.DEFAULT_NETWORK_HOST
                : u.getHost();
        final int port = (u.getPort() == -1) ? 80 : u.getPort();

        // Create the server.
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly", host, port);
        listener.setSecure(secure);
        if(sslEngineConfigurator != null) {
            listener.setSSLEngineConfig(sslEngineConfigurator);
        }

        server.addListener(listener);

        // Map the path to the processor.
        final ServerConfiguration config = server.getServerConfiguration();
        if (handler != null) {
            config.addHttpHandler(handler, u.getPath());
        }

        // Start the server.
//        server.start();
        return server;
    }

    public static HttpServer createHttpServer(
            final URI u,
            final HttpHandler handler,
            final boolean secure)
            throws IOException, IllegalArgumentException, NullPointerException {
        return createHttpServer(u, handler, secure, null);
    }


    public static HttpServer createHttpServer(
            final URI u,
            final HttpHandler handler)
            throws IOException, IllegalArgumentException, NullPointerException {
        return createHttpServer(u, handler, false, null);
    }

    // Prevent instantiation.
    private GrizzlyServerFactory() {
        // Nothing to do.
    }
}