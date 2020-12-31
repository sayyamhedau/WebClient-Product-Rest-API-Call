package com.app.service;

import com.app.entity.ProductDTO;

import reactor.core.publisher.Flux;

public interface IProductService {
	String getWelcomeMsg();

	String saveProduct(ProductDTO productDTO);

	Flux<ProductDTO> getAllProducts();

	String updateProduct(ProductDTO productDTO);

	String deleteProduct(Long id);

	ProductDTO getProductById(Long id);
}
