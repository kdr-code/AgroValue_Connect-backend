package com.agrovalue.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agrovalue.backend.dto.ProductRequest;
import com.agrovalue.backend.dto.ProductResponse;
import com.agrovalue.backend.exception.ResourceNotFoundException;
import com.agrovalue.backend.entity.*;
import com.agrovalue.backend.repository.ProductRepository;
import com.agrovalue.backend.repository.ReviewRepository;
import com.agrovalue.backend.repository.UserRepository;
import com.agrovalue.backend.service.ProductService;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        User farmer = userRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + request.getFarmerId()));

        Product product = new Product();
        applyRequest(product, request, farmer);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        User farmer = userRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + request.getFarmerId()));
        applyRequest(product, request, farmer);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByFarmer(Long farmerId) {
        return productRepository.findByFarmerId(farmerId).stream().map(this::mapToResponse).toList();
    }

    private void applyRequest(Product product, ProductRequest request, User farmer) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setFarmer(farmer);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());
        response.setFarmerId(product.getFarmer().getId());
        response.setFarmerName(product.getFarmer().getName());
        Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
        response.setAverageRating(avgRating == null ? 0.0 : Math.round(avgRating * 10.0) / 10.0);
        return response;
    }
}
