package com.catalyst.ProCounsellor.model;

public enum Courses {
	 	HSC("HSC"),
	    ENGINEERING("Engineering"),
	    MEDICAL("Medical"),
	    LAW("Law"),
	    BBA("BBA"),
	    MBA("MBA"),
	    BCOM("B.Com"),
	    MCOM("M.Com"),
	    BA("B.A."),
	    MA("M.A."),
	    BSC("B.Sc"),
	    MSC("M.Sc"),
	    BCA("BCA"),
	    MCA("MCA"),
	    DIPLOMA_ENGINEERING("Diploma in Engineering"),
	    POLYTECHNIC("Polytechnic"),
	    ITI("ITI"),
	    DESIGN("Design"),
	    ARCHITECTURE("Architecture"),
	    PHARMACY("Pharmacy"),
	    NURSING("Nursing"),
	    JOURNALISM("Journalism and Mass Communication"),
	    HOTEL_MANAGEMENT("Hotel Management"),
	    AVIATION("Aviation"),
	    DEFENSE("Defense Services"),
	    CA("Chartered Accountancy"),
	    CS("Company Secretary"),
	    CMA("Cost and Management Accounting"),
	    FASHION("Fashion Designing"),
	    ANIMATION("Animation and Multimedia"),
	    EDUCATION("B.Ed / M.Ed"),
	    AGRICULTURE("Agriculture"),
	    VETERINARY("Veterinary Sciences"),
	    DENTAL("Dental (BDS/MDS)"),
	    SOCIAL_WORK("Social Work (BSW/MSW)"),
	    PSYCHOLOGY("Psychology"),
	    BANKING("Banking and Finance"),
	    CIVIL_SERVICES("Civil Services / UPSC"),
	    OTHERS("Others");

	    private final String label;

	    Courses(String label) {
	        this.label = label;
	    }

	    public String getLabel() {
	        return label;
	    }
}
