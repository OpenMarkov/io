package org.openmarkov.io.probmodel.reader;

/**
 * Enum to define the parser version labels
 */
public enum Version {
	V02("0.2"),
	V10("1.0");  
	// Extension point in future parser versions.
	
	private final String label;
	
	Version(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
}