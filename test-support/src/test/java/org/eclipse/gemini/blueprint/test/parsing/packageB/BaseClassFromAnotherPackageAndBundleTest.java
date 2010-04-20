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

package org.eclipse.gemini.blueprint.test.parsing.packageB;

import javax.naming.CompositeName;
import javax.naming.Name;

import org.eclipse.gemini.blueprint.test.parsing.packageA.BaseClassFromAnotherPackageTest;

/**
 * @author Costin Leau
 * 
 */
// add an interface just to check the behaviour
public abstract class BaseClassFromAnotherPackageAndBundleTest extends BaseClassFromAnotherPackageTest {

	private Name context = new CompositeName();

}
