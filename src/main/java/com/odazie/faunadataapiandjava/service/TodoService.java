package com.odazie.faunadataapiandjava.service;

import com.odazie.faunadataapiandjava.data.TodoEntity;
import com.odazie.faunadataapiandjava.data.TodoRepository;
import com.odazie.faunadataapiandjava.data.model.CreateOrReplaceTodoData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;


    public CompletableFuture<TodoEntity> createTodo(CreateOrReplaceTodoData data) {
        CompletableFuture<TodoEntity> result =
                todoRepository.nextId()
                        .thenApply(id -> new TodoEntity(id, data.getTitle(), data.getDescription()))
                        .thenCompose(todoEntity -> todoRepository.save(todoEntity));

        return result;
    }


}
