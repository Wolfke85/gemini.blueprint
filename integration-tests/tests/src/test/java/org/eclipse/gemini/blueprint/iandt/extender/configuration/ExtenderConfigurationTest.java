/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.iandt.extender.configuration;

import java.util.List;
import java.util.Properties;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Extender configuration fragment.
 * 
 * @author Costin Leau
 * 
 */
public class ExtenderConfigurationTest extends BaseIntegrationTest {

	private ApplicationContext context;

	@Override
	protected void onSetUp() throws Exception {
		context = (ApplicationContext) applicationContext.getBean("appCtx");
	}

	@Override
	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,extender-fragment-bundle," + getSpringDMVersion() };
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "org/eclipse/gemini/blueprint/iandt/extender/configuration/config.xml" };
	}

	public void testExtenderConfigAppCtxPublished() throws Exception {
		ServiceReference[] refs =
				bundleContext.getAllServiceReferences("org.springframework.context.ApplicationContext", null);
		for (int i = 0; i < refs.length; i++) {
			System.out.println(OsgiStringUtils.nullSafeToString(refs[i]));
		}
		assertNotNull(context);
	}

	public void testShutdownTaskExecutor() throws Exception {
		assertTrue(context.containsBean("shutdownTaskExecutor"));
		Object bean = context.getBean("shutdownTaskExecutor");
		assertTrue("unexpected type", bean instanceof ThreadPoolTaskExecutor);
	}

	public void testTaskExecutor() throws Exception {
		assertTrue(context.containsBean("taskExecutor"));
		Object bean = context.getBean("shutdownTaskExecutor");
		assertTrue("unexpected type", bean instanceof TaskExecutor);
	}

	public void testCustomProperties() throws Exception {
		assertTrue(context.containsBean("extenderProperties"));
		Object bean = context.getBean("extenderProperties");
		assertTrue("unexpected type", bean instanceof Properties);
	}

	// felix doesn't support fragments, so disable this test
	@Override
	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return getPlatformName().indexOf("elix") > -1;
	}

	@Override
	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		return list;
	}
}