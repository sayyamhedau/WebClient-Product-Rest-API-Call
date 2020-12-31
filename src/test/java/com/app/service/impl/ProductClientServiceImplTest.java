package com.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.entity.ProductDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class ProductClientServiceImplTest {
	
	private static ProductClientServiceImpl productClientServiceImpl;
	private static MockWebServer mockWebServer;
	private static List<ProductDTO> productList;

	@BeforeAll
	static void setup() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		productClientServiceImpl = new ProductClientServiceImpl(WebClient.builder(), mockWebServer.url("/").toString());

		productList = Stream
				.of(new ProductDTO(1L, "P001", "HP Laptop", "Laptop", 65000.0, 2.3, 2.0, 10),
					new ProductDTO(2L, "P002", "HP Laptop", "Laptop", 65000.0, 2.3, 2.0, 10))
				.collect(Collectors.toList());
	}

	@Test
	@Order(value = 1)
	void shouldReturnWelcomeMessage() {
		mockWebServer.enqueue(
				new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody("Welcome to Rest Controller"));
		assertNotNull(productClientServiceImpl.getWelcomeMsg());
	}

	@Test
	@Order(value = 3)
	void shouldReturnProductById() throws JsonProcessingException {

		mockWebServer.enqueue(new MockResponse()
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.setBody(convertObjectToJsonFormat(productList.get(0))));

		assertEquals(1L, productClientServiceImpl.getProductById(1L).getId());
	}

	@Test
	@Order(value = 4)
	void shouldReturnListOfProducts() throws JsonProcessingException {
		mockWebServer.enqueue(new MockResponse()
				.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setResponseCode(HttpStatus.OK.value()).setBody(convertObjectToJsonFormat(productList.get(0))));

		assertThat(productClientServiceImpl.getAllProducts().toStream().collect(Collectors.toList()).size())
				.isEqualTo(1);
	}

	@Test
	@Order(value = 2)
	void shouldCreateNewProduct() throws JsonProcessingException {
		mockWebServer.enqueue(new MockResponse()
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setBody(convertObjectToJsonFormat(productList.get(0)))
				.setResponseCode(HttpStatus.CREATED.value()));

		assertNotNull(productClientServiceImpl.saveProduct(productList.get(0)));
	}

	@Test
	@Order(value = 5)
	void shouldUpdateProduct() throws JsonProcessingException {
		mockWebServer.enqueue(new MockResponse()
				.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setBody(convertObjectToJsonFormat(productList.get(0))));

		assertNotNull(productClientServiceImpl.updateProduct(productList.get(0)));
	}

	@Test
	@Order(value = 6)
	void shouldDeleteProduct() throws JsonProcessingException, InterruptedException {
		mockWebServer.enqueue(new MockResponse()
				.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setBody(convertObjectToJsonFormat(productList.get(0))));

		assertAll(() -> productClientServiceImpl.deleteProduct(productList.get(0).getId()),
				() -> assertEquals("DELETE", mockWebServer.takeRequest().getMethod()),
				() -> assertEquals("/delete/" + productList.get(0).getId(), mockWebServer.takeRequest().getPath()));

		System.out.println("Body - " + mockWebServer.takeRequest().getBody());
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	private static String convertObjectToJsonFormat(Object obj) throws JsonProcessingException {
		String response = "";
		ObjectMapper objectMapper = new ObjectMapper();

		if (obj instanceof ProductDTO) {
			ProductDTO product = (ProductDTO) obj;
			response = objectMapper.writeValueAsString(product);
		}
		return response;
	}
}
