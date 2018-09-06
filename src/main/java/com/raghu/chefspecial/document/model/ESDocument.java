package com.raghu.chefspecial.document.model;

import java.util.List;


public class ESDocument {
	public static final String INDEX_NAME = "recipe-autocomplete-v1";
	public static final String TYPE = "chefspecial";

	private List<String> aliases;
	private String type;
}
