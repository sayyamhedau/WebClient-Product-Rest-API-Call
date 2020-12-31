package com.app.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.ProductDTO;
import com.app.exception.ProductNotFoundException;
import com.app.service.IProductService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/v1/api/products")
public class ProductRestController {

	@Autowired
	private IProductService productService;

	@GetMapping(value = "/welcome")
	public ResponseEntity<String> getWelcomeMessage() {
		return ResponseEntity.ok().body(productService.getWelcomeMsg());
	}

	@PostMapping(value = "/save")
	public ResponseEntity<String> createNewProduct(@RequestBody ProductDTO product) {
		if (Objects.isNull(product)) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Pass product body");
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(product));
	}

	@GetMapping(value = "/all")
	public ResponseEntity<Flux<ProductDTO>> getAllProducts() {
		return ResponseEntity.ok().body(productService.getAllProducts());
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
		return ResponseEntity.ok().body(productService.getProductById(id));
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<String> updateProduct(@PathVariable Long id, @RequestBody ProductDTO product) {
		product.setId(id);
		return ResponseEntity.ok().body(productService.updateProduct(product));
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
		boolean productExistsByIdStatus = productService.getAllProducts().toStream().anyMatch(product -> product.getId().equals(id));
		if (!productExistsByIdStatus) {
			throw new ProductNotFoundException("Product Not Found With id - " + id);
		}
		return ResponseEntity.accepted().body(productService.deleteProduct(id));
	}
}
