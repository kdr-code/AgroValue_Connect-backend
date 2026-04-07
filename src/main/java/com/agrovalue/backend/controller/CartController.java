package com.agrovalue.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.agrovalue.backend.dto.CartItemRequest;
import com.agrovalue.backend.dto.CartResponse;
import com.agrovalue.backend.dto.UpdateCartItemRequest;
import com.agrovalue.backend.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long cartItemId, @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        cartService.removeItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getUserCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }
}
