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
package org.onap.aai.migration;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.javatuples.Pair;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.db.EdgeRule;
import org.onap.aai.serialization.db.EdgeRules;

/**
 * A migration template for migrating all edge properties between "from" and "to" node from the DbedgeRules.json
 * 
 */
@MigrationPriority(0)
@MigrationDangerRating(1)
public abstract class EdgeMigrator extends Migrator {

	private boolean success = true;
	private EdgeRules rules;

	public EdgeMigrator(TransactionalGraphEngine engine) {
		super(engine);
		rules = EdgeRules.getInstance();
	}

	public EdgeMigrator(TransactionalGraphEngine engine, List<Pair<String, String>> nodePairList) {
		super(engine);
		rules = EdgeRules.getInstance();
	}


	/**
	 * Do not override this method as an inheritor of this class
	 */
	@Override
	public void run() {

		executeModifyOperation();

	}

	/**
	 * This is where inheritors should add their logic
	 */
	protected void executeModifyOperation() {
		
		changeEdgeProperties();
		
	}

	protected void changeEdgeLabels() {
	//TODO: when json file has edge label as well as edge property changes	
	}
	
	
	
	protected void changeEdgeProperties() {
		try {
			List<Pair<String, String>> nodePairList = this.getAffectedNodePairTypes();
			for (Pair<String, String> nodePair : nodePairList) {
				
				String NODE_A = nodePair.getValue0();
				String NODE_B = nodePair.getValue1();
				Map<String, EdgeRule> result = rules.getEdgeRules(NODE_A, NODE_B);

				GraphTraversal<Vertex, Vertex> g = this.engine.asAdmin().getTraversalSource().V();
				/*
				 * Find Out-Edges from Node A to Node B and change them
				 * Also Find Out-Edges from Node B to Node A and change them 
				 */
				g.union(__.has(AAIProperties.NODE_TYPE, NODE_A).outE().where(__.inV().has(AAIProperties.NODE_TYPE, NODE_B)),
						__.has(AAIProperties.NODE_TYPE, NODE_B).outE().where(__.inV().has(AAIProperties.NODE_TYPE, NODE_A)))
						.sideEffect(t -> {
							Edge e = t.get();
							try {
								Vertex out = e.outVertex();
								Vertex in = e.inVertex();
								if (out == null || in == null) {
									logger.error(
											e.id() + " invalid because one vertex was null: out=" + out + " in=" + in);
								} else {
									if (result.containsKey(e.label())) {
										EdgeRule rule = result.get(e.label());
										e.properties().forEachRemaining(prop -> prop.remove());
										rules.addProperties(e, rule);
									} else {
										logger.info("found vertices connected by unkwown label: out=" + out + " label="
												+ e.label() + " in=" + in);
									}
								}
							} catch (Exception e1) {
								throw new RuntimeException(e1);
							}
						}).iterate();
			}

		} catch (Exception e) {
			logger.error("error encountered", e);
			success = false;
		}
	}
  
	@Override
	public Status getStatus() {
		if (success) {
			return Status.SUCCESS;
		} else {
			return Status.FAILURE;
		}
	}

	/**
	 * List of node pairs("from" and "to"), you would like EdgeMigrator to migrate from json files
	 * @return
	 */
	public abstract List<Pair<String, String>> getAffectedNodePairTypes() ;
	
}
