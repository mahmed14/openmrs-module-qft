package org.openmrs.module.qft.web.util;

public enum QFTKeys {
	
	SubjectID("Subject ID"), NIL("Nil"), TB1("TB1"), TB2("TB2"), MITOGEN("Mitogen"), TB1Nil("TB1-Nil"), TB2Nil("TB2-Nil"),
	
	MitogenNil("Mitogen-Nil"),
	
	RunNumber("Run Number"), RunDate("Run Date"), ValidTest("Valid Test"), Result("Result");
	
	private final String key;
	
	private QFTKeys(String key) {
		this.key = key;
	}
	
	public boolean equalsName(String otherName) {
		// (otherName == null) check is not needed because name.equals(null) returns false 
		return key.equals(otherName);
	}
	
	public String toString() {
		return this.key;
	}
	
}
