package com.odazie.faunadataapiandjava.persistence;

import com.odazie.faunadataapiandjava.data.common.Entity;
import com.odazie.faunadataapiandjava.data.common.Page;
import com.odazie.faunadataapiandjava.data.common.PaginationOptions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Repository<T extends Entity> {

    CompletableFuture<T> save(T entity);

    CompletableFuture<Optional<T>> find(String id);

    CompletableFuture<Page<T>> findAll(PaginationOptions po);

    CompletableFuture<Optional<T>> remove(String id);


}
