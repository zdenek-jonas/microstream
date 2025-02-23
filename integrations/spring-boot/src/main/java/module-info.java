/*-
 * #%L
 * microstream-integrations-spring-boot
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */
module microstream.integrations.spring.boot
{
	exports one.microstream.integrations.spring.boot.types.oracle.coherence;
	exports one.microstream.integrations.spring.boot.types.redis;
	exports one.microstream.integrations.spring.boot.types.hazelcast;
	exports one.microstream.integrations.spring.boot.types.mongodb;
	exports one.microstream.integrations.spring.boot.types.oracle;
	exports one.microstream.integrations.spring.boot.types.sql;
	exports one.microstream.integrations.spring.boot.types.aws;
	exports one.microstream.integrations.spring.boot.types;
	exports one.microstream.integrations.spring.boot.types.azure;
	exports one.microstream.integrations.spring.boot.types.oracle.nosql;
	exports one.microstream.integrations.spring.boot.types.oraclecloud;
	exports one.microstream.integrations.spring.boot.types.config;
	exports one.microstream.integrations.spring.boot.types.storage;

	requires transitive microstream.storage.embedded.configuration;
	requires transitive spring.beans;
	requires transitive spring.boot;
	requires transitive spring.boot.autoconfigure;
	requires transitive spring.context;
	requires transitive spring.core;
}