package com.seth.java.springreactiveclient.controller;

import com.seth.java.springreactiveclient.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ItemClientController {

    WebClient webClient = WebClient.create("http://localhost:8080");

    @GetMapping("/client/retrieve")
    public Flux<Item> getAllItemsUsingRetrieve(){
        return webClient.get().uri("/v1/items")
                .retrieve()
                .bodyToFlux(Item.class)
                .log("Items in Client Project in retrieve");
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange(){
        return webClient.get().uri("/v1/items")
                .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(Item.class))
                .log("Items in Client Project in exchange");
    }

    @GetMapping("/client/retrieve/singleItem")
    public Mono<Item> getOneItemUsingRetrieve(){
        String id = "1111";
        return webClient.get().uri("/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Items in Client Project in retrieve single item");
    }

    @GetMapping("/client/exchange/singleItem")
    public Mono<Item> getOneItemUsingExchange(){
        String id = "1111";
        return webClient.get().uri("/v1/items/{id}", id)
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log("Items in Client Project in exchange single item");
    }

    @PostMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item){
        return webClient.post().uri("/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Created Item is ");
    }

    @PutMapping("/client/updateItem/{id}")
    public Mono<Item> updateItem(@PathVariable String id, @RequestBody Item item){
        return webClient.put().uri("/v1/items/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Updated Item is ");
    }

    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> deleteItem(@PathVariable String id){
        return webClient.delete().uri("/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .log("Deleted Item is ");
    }

    @GetMapping("/client/retrieve/error")
    public Flux<Item> errorRetrieve(){
        return webClient.get().uri("/v1/items/exception")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                    return errorMono.flatMap(errorMessage -> {
                        log.error("The error Message is : " + errorMessage);
                        return Mono.error(new RuntimeException(errorMessage));
                    });
                })
                .bodyToFlux(Item.class)
                .log("Items in Client Project in retrieve single item");
    }

    @GetMapping("/client/exchange/error")
    public Flux<Item> errorExchange(){
        return webClient.get()
                .uri("/v1/items/exception")
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().is5xxServerError()){
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error Message in Error Exchange : "+ errorMessage);
                                    return Mono.error(new RuntimeException(errorMessage));
                                });
                    } else {
                        return clientResponse.bodyToFlux(Item.class);
                    }
                });
    }
}
