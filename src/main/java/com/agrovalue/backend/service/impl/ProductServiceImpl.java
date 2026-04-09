package com.agrovalue.backend.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.agrovalue.backend.dto.ProductRequest;
import com.agrovalue.backend.dto.ProductResponse;
import com.agrovalue.backend.entity.Product;
import com.agrovalue.backend.entity.User;
import com.agrovalue.backend.exception.ResourceNotFoundException;
import com.agrovalue.backend.repository.ProductRepository;
import com.agrovalue.backend.repository.ReviewRepository;
import com.agrovalue.backend.repository.UserRepository;
import com.agrovalue.backend.service.FileService;
import com.agrovalue.backend.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FileService fileService;

    
    public ProductServiceImpl(ProductRepository productRepository,
                              UserRepository userRepository,
                              ReviewRepository reviewRepository,
                              FileService fileService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.fileService = fileService;
    }

    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
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
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        Product product = new Product();
        applyRequest(product, request, farmer);

        return mapToResponse(productRepository.save(product));
    }

    
    @Override
    @Transactional
    public ProductResponse addProductWithImage(ProductRequest request, MultipartFile file) {

        logger.info("Starting product creation with image");

       
        String imageUrl = fileService.uploadFile(file);

        logger.info("Image uploaded: {}", imageUrl);

       
        User farmer = userRepository.findById(request.getFarmerId())
                .orElseThrow(() -> {
                    logger.error("Farmer not found with ID: {}", request.getFarmerId());
                    return new ResourceNotFoundException("Farmer not found");
                });

       
        Product product = new Product();
        applyRequest(product, request, farmer);

        
        product.setImageUrl(imageUrl);

        logger.info("Saving product: {}", request.getName());

        Product savedProduct = productRepository.save(product);

        logger.info("Product saved successfully with ID: {}", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User farmer = userRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        applyRequest(product, request, farmer);

        return mapToResponse(productRepository.save(product));
    }

    
    @Override
    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByFarmer(Long farmerId) {
        return productRepository.findByFarmerId(farmerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
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