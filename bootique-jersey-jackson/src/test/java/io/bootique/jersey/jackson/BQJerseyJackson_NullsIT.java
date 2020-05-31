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

package io.bootique.jersey.jackson;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.TestRuntumeBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BQJerseyJackson_NullsIT {

    @RegisterExtension
    final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    protected WebTarget startServer(BQModule... modules) {
        TestRuntumeBuilder builder = testFactory
                .app("-s")
                .module(JettyTester.moduleReplacingConnectors());

        asList(modules).forEach(builder::module);

        BQRuntime server = builder.createRuntime();
        assertTrue(server.run().isSuccess());
        return JettyTester.getTarget(server);
    }

    @Test
    public void testPrintNulls() {

        WebTarget target = startServer(b -> JerseyModule.extend(b).addResource(JsonResource.class));

        Response r = target.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"p1\":null,\"p2\":45}", r.readEntity(String.class));
    }

    @Test
    public void testIgnoreNulls() {

        WebTarget target = startServer(
                b -> JerseyModule.extend(b).addResource(JsonResource.class),
                b -> JerseyJacksonModule.extend(b).skipNullProperties());

        Response r = target.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"p2\":45}", r.readEntity(String.class));
    }

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class JsonResource {

        @GET
        public Model get() {
            return new Model(null, 45);
        }
    }

    public static class Model {
        private String p1;
        private Integer p2;

        public Model(String p1, Integer p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public String getP1() {
            return p1;
        }

        public Integer getP2() {
            return p2;
        }
    }
}
