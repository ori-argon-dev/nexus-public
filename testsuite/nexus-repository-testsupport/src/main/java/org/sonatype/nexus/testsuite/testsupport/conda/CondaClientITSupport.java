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
package org.sonatype.nexus.testsuite.testsupport.conda;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.sonatype.nexus.docker.testsupport.conda.CondaCommandLineITSupport;
import com.sonatype.nexus.docker.testsupport.framework.DockerContainerConfig;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.helpers.ComponentAssetTestHelper;
import org.sonatype.nexus.testsuite.testsupport.FormatClientITSupport;

import org.joda.time.DateTime;
import org.junit.Before;

import static com.sonatype.nexus.docker.testsupport.conda.CondaClientITConfigFactory.createCondaConfig;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * @since 3.19
 * Support class for Conda Format Client ITs.
 */
public abstract class CondaClientITSupport
    extends FormatClientITSupport
{
  public static final String TERMINAL_DELIMITER = " ";
  public static final String CONDA_DELIMITER = "=";

  protected CondaCommandLineITSupport condaCli;

  private String environmentName;

  @Inject
  protected ComponentAssetTestHelper testHelper;

  /**
   * This initialize method will add the conda resources test data directory and create the {@link #condaCli}.
   */
  @Before
  public void onInitializeClientIT() throws Exception {
    addTestDataDirectory("target/it-resources/conda");
    BaseUrlHolder.set(this.nexusUrl.toString());
    condaCli = new CondaCommandLineITSupport(createTestConfig());
  }

  /**
   * Default {@link DockerContainerConfig} for testing Conda
   *
   * @return DockerContainerConfig
   */
  protected DockerContainerConfig createTestConfig() throws Exception {
    return createCondaConfig();
  }

  protected Repository createCondaProxyRepository(final String repoName, final String repoProxyUrl) {
    return repos.createCondaProxy(repoName, repoProxyUrl);
  }

  public Optional<String> getInstalledPackage(final String packageName)
  {
    List<String> installed = condaCli.listInstalled().stream()
        .map(this::getTerminalPackage)
        .collect(toList());
    return installed.stream()
        .filter(row -> row.contains(condaPackageToTerminalPackage(packageName)))
        .findFirst();
  }

  public Optional<String> installPackage(final String packageName) {
    condaCli.condaInstall(packageName);
    List<String> listInstalled = condaCli.listInstalled();
    List<String> installed = listInstalled.stream()
        .map(this::getTerminalPackage)
        .collect(toList());

    String terminalPackage = condaPackageToTerminalPackage(packageName);

    assertThat(installed, hasItem(containsString(terminalPackage)));

    return installed.stream()
        .filter(row -> row.contains(terminalPackage))
        .findFirst();
  }

  public void checkRepositoryCachedPackageFormat(final Repository condaRepository,
                                                 final Boolean isEnableTar,
                                                 final String formatExtension,
                                                 final String packageName)
  {
    //Update client to use latest version
    condaCli.condaUpdate();
    //Set configuration to use or not legacy code
    condaCli.condaExec("config --set use_only_tar_bz2 " + isEnableTar.toString());

    installPackage(packageName);

    String[] packageNameAndVersion = packageName.split(CONDA_DELIMITER);
    assertThat(packageNameAndVersion.length, is(2));
    String name = packageNameAndVersion[0];
    String version = packageNameAndVersion[1];

    assertThat(testHelper.componentExists(condaRepository, name, version), is(TRUE));
    assertThat(testHelper.assetExists(condaRepository, name, formatExtension), is(TRUE));
  }

  public void activateNewEnvironment()
  {
    environmentName = DateTime.now().toString();
    condaCli.condaExec("create -y -n " + environmentName);
    condaCli.condaExec("activate " + environmentName);
  }

  public void deactivateEnvironment()
  {
    condaCli.condaExec("deactivate");
    condaCli.condaExec("remove -y -n " + environmentName + " --all");
    environmentName = null;
  }

  protected String getTerminalPackage(final String row) {
    return row.trim().replaceAll("\\s+", TERMINAL_DELIMITER);
  }

  private String condaPackageToTerminalPackage(final String packageName) {
    return packageName.replaceAll(CONDA_DELIMITER, TERMINAL_DELIMITER);
  }
}
