package com.odazie.faunadataapiandjava.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odazie.faunadataapiandjava.data.model.CreateOrReplaceTodoData;
import com.odazie.faunadataapiandjava.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
public class TodoRestController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping("/todos")
    public CompletableFuture<ResponseEntity> createTodo(HttpEntity<String> httpEntity) throws IOException {

        String requestBody = httpEntity.getBody();

        if(isCreateReplacePostData(requestBody)) {
            CreateOrReplaceTodoData data = deserializeCreateReplacePostData(requestBody);
            CompletableFuture<ResponseEntity> result = todoService.createTodo(data)
                    .thenApply(todoEntity -> new ResponseEntity(todoEntity, HttpStatus.CREATED));

            return result;
        }

        return CompletableFuture.completedFuture(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }

    private Boolean isCreateReplacePostData(String json) throws IOException {
        try {
            objectMapper.readValue(json, CreateOrReplaceTodoData.class);
            return true;
        } catch (JsonParseException | JsonMappingException e) {
            return false;
        }
    }
    private CreateOrReplaceTodoData deserializeCreateReplacePostData(String json) throws IOException {
        return objectMapper.readValue(json, CreateOrReplaceTodoData.class);
    }
}
