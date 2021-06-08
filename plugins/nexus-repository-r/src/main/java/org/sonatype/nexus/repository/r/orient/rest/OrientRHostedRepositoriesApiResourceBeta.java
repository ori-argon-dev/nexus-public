/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.r.orient.rest;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import org.sonatype.nexus.common.app.FeatureFlag;
import org.sonatype.nexus.repository.r.rest.RHostedRepositoriesApiResource;
import org.sonatype.nexus.repository.rest.api.RepositoriesApiResourceBeta;

import io.swagger.annotations.Api;

import static org.sonatype.nexus.common.app.FeatureFlags.ORIENT_ENABLED;

/**
 * @since 3.28
 * @deprecated - prefer to use {@link OrientRHostedRepositoriesApiResourceV1 } instead of Beta.
 */
@FeatureFlag(name = ORIENT_ENABLED)
@Named
@Singleton
@Path(RepositoriesApiResourceBeta.RESOURCE_URI + "/r/hosted")
@Api(hidden = true)
@Deprecated
public class OrientRHostedRepositoriesApiResourceBeta
    extends RHostedRepositoriesApiResource
{
}