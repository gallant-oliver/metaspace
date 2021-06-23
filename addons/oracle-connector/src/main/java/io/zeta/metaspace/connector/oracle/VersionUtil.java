package io.zeta.metaspace.connector.oracle;

/**
 * @author T480
 */
class VersionUtil {
	
	public static String getVersion() {
		try {
			return VersionUtil.class.getPackage().getImplementationVersion();
		} catch (Exception ex) {
			return "0.0.1";
		}
	}
}
