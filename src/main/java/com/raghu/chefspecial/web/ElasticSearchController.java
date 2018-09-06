package com.raghu.chefspecial.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raghu.chefspecial.config.CustomElasticsearchTemplate;
import com.raghu.chefspecial.document.model.ESDocument;

@RequestMapping("/es")
@RestController
public class ElasticSearchController {

	@Autowired
	private CustomElasticsearchTemplate customElasticSearchTemplate;

	@RequestMapping("/create")
	public String greeting() throws IOException {
		String settings = this.loadFile("/Users/mbellamkonda/Projects/ChefSpecial/src/main/resources/settings.json");
		String mappings = this.loadFile("/Users/mbellamkonda/Projects/ChefSpecial/src/main/resources/mappings.json");
		this.customElasticSearchTemplate.createIndex(ESDocument.INDEX_NAME, settings);
		this.customElasticSearchTemplate.putMapping(ESDocument.INDEX_NAME, ESDocument.TYPE, mappings);
		this.customElasticSearchTemplate.refresh(ESDocument.INDEX_NAME);
		return "greeting";
	}

	/**
	 * Read resource file from classpath.
	 */
	private String loadFile(final String path) throws IOException {
		File file = new File(path);

		return new BufferedReader(new FileReader(file)).lines().collect(Collectors.joining("\n"));
	}
}
