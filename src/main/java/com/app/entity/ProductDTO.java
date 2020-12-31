package com.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	private Long id;

	private String productCode;

	private String productName;

	private String productType;

	private Double productCost;

	private Double productDiscount;

	private Double productGst;

	private Integer productQuantity;
}
