package com.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.entity.ProductDTO;
import com.app.service.IProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductClientServiceImpl implements IProductService {
	
	private final WebClient webClient;
	private static final Logger log = LoggerFactory.getLogger(ProductClientServiceImpl.class);

	@Autowired
	public ProductClientServiceImpl(WebClient.Builder builder, @Value("${client.base.url}") String api_base_url) {
		webClient = builder.baseUrl(api_base_url).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
	}

	@Override
	public String getWelcomeMsg() {
		return webClient.get().uri("/welcome").retrieve().bodyToMono(String.class).retry().block();
	}

	@Override
	public String saveProduct(ProductDTO product) {

		return webClient.post().uri("/save")
		 		.body(BodyInserters.fromValue(product))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, response -> {
					log.info("Error Code {}", response.statusCode());
					return Mono.error(new RuntimeException("4xx exception occured"));
				}).onStatus(HttpStatus::is5xxServerError, response -> {
					log.info("Error Code {}", response.statusCode());
					return Mono.error(new RuntimeException("5xx exception occured"));
				})
				.bodyToMono(String.class)
				.doOnSuccess(success -> log.info(success))
				.block();
	}

	@Override
	public Flux<ProductDTO> getAllProducts() {
		return webClient.get()
				.uri("/all")
				.retrieve()
				.bodyToFlux(ProductDTO.class);
	}

	@Override
	public String updateProduct(ProductDTO product) {
		return webClient.put()
				.uri("/update/" + product.getId())
				.body(BodyInserters.fromValue(product))
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

	@Override
	public String deleteProduct(Long id) {
		return webClient.delete()
			.uri("/delete/"+ id)
			.retrieve()
			.bodyToMono(String.class)
			.block();
	}

	@Override
	public ProductDTO getProductById(Long id) {
		return webClient.get()
			.uri("/" + id)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.onStatus(HttpStatus::is4xxClientError, response->{
				log.info("Error Code {}", response.statusCode());
				return Mono.error(new RuntimeException("4xx exception occured"));
			})
			.onStatus(HttpStatus::is5xxServerError, response->{
				log.info("Error Code {}", response.statusCode());
				return Mono.error(new RuntimeException("5xx exception occured"));
			})
			.bodyToMono(ProductDTO.class)
			.block();
	}
}
