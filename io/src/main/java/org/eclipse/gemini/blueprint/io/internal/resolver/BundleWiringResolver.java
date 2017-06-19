package org.eclipse.gemini.blueprint.io.internal.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.io.internal.OsgiHeaderUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.VersionRange;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * <p/>
 * This implementation uses the OSGi Bundle wiring packages to determine
 * dependencies between bundles.
 * 
 * <p/>
 * This implementation does consider required bundles.
 * 
 * @author Bert De Wolf
 * 
 */
public class BundleWiringResolver implements DependencyResolver {

	private static final Log log = LogFactory.getLog(BundleWiringResolver.class);

	private final BundleContext bundleContext;

	public BundleWiringResolver(BundleContext bundleContext) {
		Assert.notNull(bundleContext);
		this.bundleContext = bundleContext;
	}

	@Override
	public ImportedBundle[] getImportedBundles(Bundle bundle) {
		boolean trace = log.isTraceEnabled();

		// create map with bundles as keys and a list of packages as value
		Map<Bundle, List<String>> importedBundles = new LinkedHashMap<Bundle, List<String>>(8);

		// 1. consider required bundles first

		// see if there are required bundle(s) defined
		String[] entries = OsgiHeaderUtils.getRequireBundle(bundle);

		// 1. if so, locate the bundles
		for (int i = 0; i < entries.length; i++) {
			String[] parsed = OsgiHeaderUtils.parseRequiredBundleString(entries[i]);
			// trim the strings just to be on the safe side (some implementations allows whitespaces, some don't)
			List<Bundle> foundBundles = findBundles(parsed[0].trim(), parsed[1].trim());
			if (!ObjectUtils.isEmpty(foundBundles)) {
				Bundle requiredBundle = foundBundles.get(0);
				BundleWiring requiredBundleWiring = requiredBundle.adapt(BundleWiring.class);
				for (BundleCapability bundleCapability : requiredBundleWiring.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE)) {
					addPackage(importedBundles, bundleCapability, requiredBundle);
				}
			} else {
				if (trace) {
					log.trace("Cannot find required bundle " + parsed[0].trim() + "|" + parsed[1].trim());
				}
			}
		}

		// 2. determine imported bundles 
		List<BundleWire> requiredWires = bundle.adapt(BundleWiring.class).getRequiredWires(PackageNamespace.PACKAGE_NAMESPACE);
		for (BundleWire bundleWire : requiredWires) {
			Bundle bundleWireProvider = bundleWire.getProvider().getBundle();
			addPackage(importedBundles, bundleWire.getCapability(), bundleWireProvider);
		}

		List<ImportedBundle> importedBundlesList = new ArrayList<ImportedBundle>(importedBundles.size());

		for (Map.Entry<Bundle, List<String>> entry : importedBundles.entrySet()) {
			List<String> packages = entry.getValue();
			importedBundlesList.add(new ImportedBundle(entry.getKey(), packages.toArray(new String[packages.size()])));
		}

		return importedBundlesList.toArray(new ImportedBundle[importedBundlesList.size()]);
	}

	private void addPackage(Map<Bundle, List<String>> importedBundles, BundleCapability bundleCapability, Bundle bundleWireProvider) {
		List<String> packages = importedBundles.get(bundleWireProvider);
		if (packages == null) {
			packages = new ArrayList<String>(4);
			importedBundles.put(bundleWireProvider, packages);
		}
		packages.add((String) bundleCapability.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE));
	}

	private List<Bundle> findBundles(String symName, String versionRange) {
		Bundle[] bundles = bundleContext.getBundles();
		List<Bundle> foundBundles = new ArrayList<>();
		for (Bundle bundle : bundles == null ? new Bundle[0] : bundles) {
			if (isBundleCandidate(bundle, symName, versionRange)) {
				foundBundles.add(bundle);
			}
		}
		Collections.sort(foundBundles, versionsDescending());
		return foundBundles;
	}

	private Comparator<Bundle> versionsDescending() {
		return new Comparator<Bundle>() {
			@Override
			public int compare(Bundle bundle, Bundle otherBundle) {
				return otherBundle.getVersion().compareTo(bundle.getVersion());
			}
		};
	}

	private boolean isBundleCandidate(Bundle bundle, String symName, String versionRange) {
		String symbolicName = bundle.getSymbolicName();
		return symbolicName != null && symbolicName.equals(symName) && new VersionRange(versionRange).includes(bundle.getVersion());
	}

}