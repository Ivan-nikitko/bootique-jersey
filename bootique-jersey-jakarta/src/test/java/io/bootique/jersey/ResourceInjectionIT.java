/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
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
 */

package io.bootique.jersey;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class ResourceInjectionIT {

    private static final String TEST_PROPERTY = "bq.test.label";
    private static final InjectedService service = new InjectedService();
    private static final InjectedServiceInterface serviceA = new InjectedServiceImplA();
    private static final InjectedServiceInterface serviceB = new InjectedServiceImplB();

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique.app("-s")
            .autoLoadModules()
            .module(b -> b.bind(InjectedService.class).toInstance(service))
            .module(b -> b.bind(InjectedServiceInterface.class, "A").toInstance(serviceA))
            .module(b -> b.bind(InjectedServiceInterface.class, CustomQualifier.class).toInstance(serviceB))
            .module(b -> b.bind(UnInjectedResource.class).toProviderInstance(() -> new UnInjectedResource(service)))
            .module(b -> JerseyModule.extend(b)
                    .addFeature(ctx -> {
                        ctx.property(TEST_PROPERTY, "x");
                        return false;
                    })
                    .addResource(FieldInjectedResource.class)
                    .addResource(NamedFieldInjectedResourceWithJakartaAnnotations.class)
                    .addResource(NamedFieldInjectedResourceCustomJakartaAnnotations.class)
                    .addResource(ConstructorInjectedResource.class)
                    .addResource(UnInjectedResource.class))
            .module(jetty.moduleReplacingConnectors())
            .createRuntime();

    @BeforeEach
    public void before() {
        service.reset();
        serviceA.reset();
        serviceB.reset();
    }

    @Test
    public void testFieldInjected() {

        Response r1 = jetty.getTarget().path("f").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("f_1_x", r1.readEntity(String.class));
        r1.close();

        Response r2 = jetty.getTarget().path("f").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("f_2_x", r2.readEntity(String.class));
        r2.close();
    }

    @Test
    public void testNamedFieldInjectedJakartaAnnotations() {

        Response r1 = jetty.getTarget().path("nfJakartaInject").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("nf_1x", r1.readEntity(String.class));
        r1.close();

        Response r2 = jetty.getTarget().path("nfJakartaInject").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("nf_2x", r2.readEntity(String.class));
        r2.close();
    }

    @Test
    public void testNamedFieldInjectedCustomJakartaAnnotations() {

        Response r1 = jetty.getTarget().path("nfCustomInjectJakarta").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("nfB_1x", r1.readEntity(String.class));
        r1.close();

        Response r2 = jetty.getTarget().path("nfCustomInjectJakarta").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("nfB_2x", r2.readEntity(String.class));
        r2.close();
    }

    @Test
    public void testConstructorInjected() {

        Response r1 = jetty.getTarget().path("c").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("c_1_x", r1.readEntity(String.class));
        r1.close();

        Response r2 = jetty.getTarget().path("c").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("c_2_x", r2.readEntity(String.class));
        r2.close();
    }

    @Test
    public void testProviderForResource() {

        Response r1 = jetty.getTarget().path("u").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("u_1_x", r1.readEntity(String.class));
        r1.close();

        Response r2 = jetty.getTarget().path("u").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("u_2_x", r2.readEntity(String.class));
        r2.close();
    }

    @Path("/f")
    @Produces(MediaType.TEXT_PLAIN)
    public static class FieldInjectedResource {

        @Inject
        private InjectedService service;

        @Context
        private Configuration config;

        @GET
        public String get() {
            return "f_" + service.getNext() + "_" + config.getProperty(TEST_PROPERTY);
        }
    }

    @Path("/nfJakartaInject")
    @Produces(MediaType.TEXT_PLAIN)
    public static class NamedFieldInjectedResourceWithJakartaAnnotations {

        @jakarta.inject.Inject
        @jakarta.inject.Named("A")
        private InjectedServiceInterface serviceA;

        @Context
        private Configuration config;

        @GET
        public String get() {
            return "nf_" + serviceA.getNext() + config.getProperty(TEST_PROPERTY);
        }
    }

    @Path("/nfCustomInjectJakarta")
    @Produces(MediaType.TEXT_PLAIN)
    public static class NamedFieldInjectedResourceCustomJakartaAnnotations {

        @jakarta.inject.Inject
        @CustomQualifier
        private InjectedServiceInterface serviceB;

        @Context
        private Configuration config;

        @GET
        public String get() {
            return "nfB_" + serviceB.getNext() + config.getProperty(TEST_PROPERTY);
        }
    }


    @Path("/c")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ConstructorInjectedResource {

        private InjectedService service;

        @Context
        private Configuration config;

        @Inject
        public ConstructorInjectedResource(InjectedService service) {
            this.service = service;
        }

        @GET
        public String get() {
            return "c_" + service.getNext() + "_" + config.getProperty(TEST_PROPERTY);
        }
    }

    @Path("/u")
    @Produces(MediaType.TEXT_PLAIN)
    public static class UnInjectedResource {

        private InjectedService service;

        @Context
        private Configuration config;

        public UnInjectedResource(InjectedService service) {
            this.service = service;
        }

        @GET
        public String get() {
            return "u_" + service.getNext() + "_" + config.getProperty(TEST_PROPERTY);
        }
    }

    public static class InjectedService {

        private AtomicInteger atomicInt = new AtomicInteger();

        public void reset() {
            atomicInt.set(0);
        }

        public int getNext() {
            return atomicInt.incrementAndGet();
        }
    }


    public static interface InjectedServiceInterface {
        AtomicInteger atomicInt = new AtomicInteger();
        default void reset() {atomicInt.set(0);}
        public int getNext();
    }

    public static class InjectedServiceImplA implements InjectedServiceInterface {
        public int getNext() {
            return atomicInt.incrementAndGet();
        }
    }
    public static class InjectedServiceImplB implements InjectedServiceInterface {
        public int getNext() {return atomicInt.incrementAndGet();}
    }

    @java.lang.annotation.Documented
    @java.lang.annotation.Retention(RUNTIME)
    @jakarta.inject.Qualifier
    public @interface CustomQualifier {
    }

}
