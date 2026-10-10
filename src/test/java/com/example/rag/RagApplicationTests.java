package com.example.rag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

@SpringBootTest
class RagApplicationTests {

	@Autowired
	private RestClient.Builder restClientBuilder;

	@Test
	void applicationStartsAndRestClientBuilderIsAvailable() {
		org.junit.jupiter.api.Assertions.assertNotNull(restClientBuilder);
	}
}
