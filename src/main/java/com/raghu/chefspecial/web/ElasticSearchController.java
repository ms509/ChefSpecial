package com.raghu.chefspecial.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.raghu.chefspecial.config.ElasticSearchTemplate;
import com.raghu.chefspecial.util.ChefSpecialUtil;

@RequestMapping("/es")
@RestController
public class ElasticSearchController {

	@Autowired
	private ElasticSearchTemplate customElasticsearchTemplate;

	@RequestMapping(value = "/createIndex", method = RequestMethod.POST)
	public Boolean greeting1() throws IOException {
		String mappings = this.loadFile(ChefSpecialUtil.MAPPING_FILE);

		return customElasticsearchTemplate.createIndex(mappings, ChefSpecialUtil.INDEX_NAME);
	}

	/**
	 * Read resource file from classpath.
	 */
	private String loadFile(final String path) throws IOException {
		File file = new File(path);

		return new BufferedReader(new FileReader(file)).lines().collect(Collectors.joining("\n"));
	}
}
