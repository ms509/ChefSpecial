package com.raghu.chefspecial.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
	
	private String name;
	private String displayName;
	private String createdBy;
	private String userId;
	private List<RecipeInstructions> instructions = new ArrayList<>();
	private List<RecipeIngredients> ingredients = new ArrayList<>();

}
