/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.migration.v12;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReadOnlyStrategy;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aai.AAISetup;
import org.onap.aai.dbmap.DBConnectionType;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.Version;
import org.onap.aai.serialization.db.EdgeRules;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;

import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphTransaction;

public class MigrateServiceInstanceToConfigurationTestPreMigrationMock extends AAISetup {

	private final static Version version = Version.v12;
	private final static ModelType introspectorFactoryType = ModelType.MOXY;
	private final static QueryStyle queryStyle = QueryStyle.TRAVERSAL;
	private final static DBConnectionType type = DBConnectionType.REALTIME;

	private static Loader loader;
	private static TransactionalGraphEngine dbEngine;
	private static JanusGraph graph;
	private static MigrateServiceInstanceToConfiguration migration;
	private static JanusGraphTransaction tx;
	private static GraphTraversalSource g;
	private static EdgeRules rules;

	@BeforeClass
	public static void setUp() throws Exception {
		graph = JanusGraphFactory.build().set("storage.backend","inmemory").open();
		tx = graph.newTransaction();
		g = tx.traversal();
		loader = LoaderFactory.createLoaderForVersion(introspectorFactoryType, version);
		dbEngine = new JanusGraphDBEngine(
				queryStyle,
				type,
				loader);
		rules = EdgeRules.getInstance();

		Vertex customer = g.addV()
				.property("aai-node-type", "customer")
				.property("global-customer-id", "customer-9972-BandwidthMigration")
				.property("subscriber-type", "CUST")
				.next();
		
		Vertex servSubSDNEI = g.addV()
				.property("aai-node-type", "service-subscription")
				.property("service-type", "SDN-ETHERNET-INTERNET")
				.next();
		
		Vertex servInstance22 = g.addV()
				.property("aai-node-type", "service-instance")
				.property("service-instance-id", "servInstance-9972-22-BandwidthMigration")
				.property("operational-status", "activated")
				.property("bandwidth-total", "bandwidth-total-22-BandwidthMigration")
				.next();
		
		Vertex servInstance11 = g.addV()
				.property("aai-node-type", "service-instance")
				.property("service-instance-id", "servInstance-9972-11-BandwidthMigration")
				.property("operational-status", "activated")
				.property("bandwidth-total", "bandwidth-total-11-BandwidthMigration")
				.next();
		
		Vertex servSubDHV = g.addV()
				.property("aai-node-type", "service-subscription")
				.property("service-type", "DHV")
				.next();
		
		Vertex servInstance4 = g.addV()
				.property("aai-node-type", "service-instance")
				.property("service-instance-id", "servInstance-9972-4-BandwidthMigration")
				.property("operational-status", "activated")
				.property("bandwidth-total", "bandwidth-total-4-BandwidthMigration")
				.next();
		
		Vertex servInstance1 = g.addV()
				.property("aai-node-type", "service-instance")
				.property("service-instance-id", "ServInstance-9972-1-BandwidthMigration")
				.property("operational-status", "activated")
				.property("bandwidth-total", "2380")
				.next();
		
		Vertex servInstance3 = g.addV()
				.property("aai-node-type", "service-instance")
				.property("service-instance-id", "servInstance-9972-3-BandwidthMigration")
				.property("operational-status", "activated")
				.property("bandwidth-total", "bandwidth-total-3-BandwidthMigration")
				.next();

		Vertex servInstance2 = g.addV()
				.property("aai-node-type", "service-instance")
				.property("service-instance-id", "servInstance-9972-2-BandwidthMigration")
				.property("operational-status", "activated")
				.property("bandwidth-total", "bandwidth-total-2-BandwidthMigration")
				.next();
		
		Vertex config1 = g.addV()
				.property("aai-node-type", "configuration")
				.property("configuration-id", "9972-config-LB1113")
				.property("configuration-type", "DHV")
				.property("tunnel-bandwidth", "12")
				.next();
		
		Vertex config2 = g.addV()
				.property("aai-node-type", "configuration")
				.property("configuration-id", "9972-1config-LB1113")
				.property("configuration-type", "configuration-type1-9972")
				.next();
		
		Vertex allottedResource = g.addV()
				.property("aai-node-type", "allotted-resource")
				.property("id", "allResource-9972-BandwidthMigration")
				.next();

		rules.addTreeEdge(g, customer, servSubSDNEI);
		rules.addTreeEdge(g, customer, servSubDHV);
		rules.addTreeEdge(g, servSubSDNEI, servInstance22);
		rules.addTreeEdge(g, servSubSDNEI, servInstance11);
		rules.addTreeEdge(g, servSubDHV, servInstance4);
		rules.addTreeEdge(g, servSubDHV, servInstance1);
		rules.addTreeEdge(g, servSubDHV, servInstance3);
		rules.addTreeEdge(g, servSubDHV, servInstance2);
		rules.addEdge(g, servInstance1, allottedResource);
		rules.addEdge(g, servInstance1, config1);
		rules.addEdge(g, servInstance2, config2);

		TransactionalGraphEngine spy = spy(dbEngine);
		TransactionalGraphEngine.Admin adminSpy = spy(dbEngine.asAdmin());

		GraphTraversalSource traversal = g;
		GraphTraversalSource readOnly = tx.traversal(GraphTraversalSource.build().with(ReadOnlyStrategy.instance()));
		when (spy.tx()).thenReturn(tx);
		when(spy.asAdmin()).thenReturn(adminSpy);
		when(adminSpy.getTraversalSource()).thenReturn(traversal);
		when(adminSpy.getReadOnlyTraversalSource()).thenReturn(readOnly);
		
		migration = new MigrateServiceInstanceToConfiguration(spy);
		migration.run();
	}
	
	@AfterClass
	public static void cleanUp() {
		tx.tx().rollback();
		graph.close();
	}
	
	@Test
	public void testRun() throws Exception {
		// check if graph nodes exist
		assertEquals("customer node exists", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.hasNext());

		assertEquals("service subscription node, service-type=SDN-ETHERNET-INTERNET", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "SDN-ETHERNET-INTERNET")
				.hasNext());

		assertEquals("service instance node, bandwidth-total=bandwidth-total-22-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "SDN-ETHERNET-INTERNET")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-22-BandwidthMigration")
				.has("bandwidth-total", "bandwidth-total-22-BandwidthMigration")
				.hasNext());
		
		assertEquals("service instance node, bandwidth-total=bandwidth-total-11-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "SDN-ETHERNET-INTERNET")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-11-BandwidthMigration")
				.has("bandwidth-total", "bandwidth-total-11-BandwidthMigration")
				.hasNext());
		
		assertEquals("service subscription node, service-type=DHV", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.hasNext());

		assertEquals("service instance node, bandwidth-total=servInstance-9972-4-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-4-BandwidthMigration")
				.has("bandwidth-total", "bandwidth-total-4-BandwidthMigration")
				.hasNext());
		
		assertEquals("service instance node, bandwidth-total=ServInstance-9972-1-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "ServInstance-9972-1-BandwidthMigration")
				.has("bandwidth-total", "2380")
				.hasNext());
		
		assertEquals("service instance node, bandwidth-total=servInstance-9972-3-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-3-BandwidthMigration")
				.has("bandwidth-total", "bandwidth-total-3-BandwidthMigration")
				.hasNext());
		
		assertEquals("service instance node, bandwidth-total=servInstance-9972-2-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-2-BandwidthMigration")
				.has("bandwidth-total", "bandwidth-total-2-BandwidthMigration")
				.hasNext());
		
		assertEquals("configuration node with type=configuration-type1-9972, tunnel-bandwidth does not exist", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-2-BandwidthMigration")
				.out("org.onap.relationships.inventory.Uses").has("aai-node-type", "configuration")
				.has("configuration-type", "configuration-type1-9972")
				.hasNext());
		
		// check if configuration node gets created for 2, 3, 4
		assertEquals("configuration node created with type=DHV, tunnel-bandwidth=servInstance-9972-4-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-4-BandwidthMigration")
				.out("org.onap.relationships.inventory.Uses").has("aai-node-type", "configuration")
				.has("configuration-type", "DHV").has("tunnel-bandwidth", "bandwidth-total-4-BandwidthMigration")
				.hasNext());
		
		assertEquals("configuration node created with type=DHV, tunnel-bandwidth=servInstance-9972-3-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-3-BandwidthMigration")
				.out("org.onap.relationships.inventory.Uses").has("aai-node-type", "configuration")
				.has("configuration-type", "DHV").has("tunnel-bandwidth", "bandwidth-total-3-BandwidthMigration")
				.hasNext());
		
		assertEquals("configuration node created with type=DHV, tunnel-bandwidth=servInstance-9972-2-BandwidthMigration", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "servInstance-9972-2-BandwidthMigration")
				.out("org.onap.relationships.inventory.Uses").has("aai-node-type", "configuration")
				.has("configuration-type", "DHV").has("tunnel-bandwidth", "bandwidth-total-2-BandwidthMigration")
				.hasNext());
		
		// configuration modified for ServInstance-9972-1-BandwidthMigration
		assertEquals("configuration node modified for ServInstance-9972-1-BandwidthMigration, tunnel-bandwidth=2380", true, 
				g.V().has("global-customer-id", "customer-9972-BandwidthMigration")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-type", "DHV")
				.in("org.onap.relationships.inventory.BelongsTo").has("service-instance-id", "ServInstance-9972-1-BandwidthMigration")
				.out("org.onap.relationships.inventory.Uses").has("aai-node-type", "configuration")
				.has("configuration-type", "DHV").has("tunnel-bandwidth", "2380")
				.hasNext());
	}
	
	@Test
	public void testGetAffectedNodeTypes() {
		Optional<String[]> types = migration.getAffectedNodeTypes();
		Optional<String[]> expected = Optional.of(new String[]{"service-instance"});
		
		assertNotNull(types);
		assertArrayEquals(expected.get(), types.get());
	}

	@Test
	public void testGetMigrationName() {
		String migrationName = migration.getMigrationName();

		assertNotNull(migrationName);
		assertEquals("service-instance-to-configuration", migrationName);
	}
}
