package com.odazie.faunadataapiandjava.persistence;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IdentityFactory {

    CompletableFuture<String> nextId();

    CompletableFuture<List<String>> nextIds(int size);
}
