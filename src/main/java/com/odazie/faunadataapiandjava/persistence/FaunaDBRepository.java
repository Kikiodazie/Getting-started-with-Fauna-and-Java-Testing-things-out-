package com.odazie.faunadataapiandjava.persistence;

import com.faunadb.client.FaunaClient;
import com.faunadb.client.errors.NotFoundException;
import com.faunadb.client.query.Expr;
import com.faunadb.client.query.Pagination;
import com.faunadb.client.types.Value;
import com.odazie.faunadataapiandjava.data.common.Entity;
import com.odazie.faunadataapiandjava.data.common.Page;
import com.odazie.faunadataapiandjava.data.common.PaginationOptions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.faunadb.client.query.Language.*;

public abstract class FaunaDBRepository<T extends Entity> implements Repository<T>, IdentityFactory{

    @Autowired
    protected FaunaClient faunaClient;

    protected final Class<T> entityType;
    protected final String className;
    protected final String classIndexName;


    protected FaunaDBRepository(Class<T> entityType, String className, String classIndexName) {
        this.entityType = entityType;
        this.className = className;
        this.classIndexName = classIndexName;
    }


    @Override
    public CompletableFuture<String> nextId() {

        CompletableFuture<String> result =
                faunaClient.query(
                       NewId()
                )
                        .thenApply(value -> value.to(String.class).get());

        return result;
    }


    @Override
    public CompletableFuture<List<String>> nextIds(int size) {
        List<Integer> indexes = IntStream
                .range(0, size)
                .mapToObj(i -> i)
                .collect(Collectors.toList());


        CompletableFuture<List<String>> result =
                faunaClient.query(
                        Map(
                                Value(indexes),
                                Lambda(Value("i"), NewId())
                        )
                )
                        .thenApply(value -> value.asCollectionOf(String.class).get().stream().collect(Collectors.toList()));

        return result;
    }

    @Override
    public CompletableFuture<T> save(T entity) {
        CompletableFuture<T> result =
                faunaClient.query(
                        saveQuery(Value(entity.getId()), Value(entity))
                )
                        .thenApply(this::toEntity);

        return result;
    }

    @Override
    public CompletableFuture<Optional<T>> remove(String id) {
        CompletableFuture<T> result =
                faunaClient.query(
                        Select(
                                Value("data"),
                                Delete(Ref(Class(className), Value(id)))
                        )
                )
                        .thenApply(this::toEntity);

        CompletableFuture<Optional<T>> optionalResult = toOptionalResult(result);

        return optionalResult;
    }


    @Override
    public CompletableFuture<Optional<T>> find(String id) {
        CompletableFuture<T> result =
                faunaClient.query(
                        Select(
                                Value("data"),
                                Get(Ref(Class(className), Value(id)))
                        )
                )
                        .thenApply(this::toEntity);

        CompletableFuture<Optional<T>> optionalResult = toOptionalResult(result);

        return optionalResult;
    }


    @Override
    public CompletableFuture<Page<T>> findAll(PaginationOptions po) {
        Pagination paginationQuery = Paginate(Match(Index(Value(classIndexName))));
        po.getSize().ifPresent(size -> paginationQuery.size(size));
        po.getAfter().ifPresent(after -> paginationQuery.after(Ref(Class(className), Value(after))));
        po.getBefore().ifPresent(before -> paginationQuery.before(Ref(Class(className), Value(before))));

        CompletableFuture<Page<T>> result =
                faunaClient.query(
                        Map(
                                paginationQuery,
                                Lambda(Value("nextRef"), Select(Value("data"), Get(Var("nextRef"))))
                        )
                ).thenApply(this::toPage);

        return result;
    }



    protected Expr saveQuery(Expr id, Expr data) {
        Expr query =
                Select(
                        Value("data"),
                        If(
                                Exists(Ref(Class(className), id)),
                                Replace(Ref(Class(className), id), Obj("data", data)),
                                Create(Ref(Class(className), id), Obj("data", data))
                        )
                );

        return query;
    }

    protected T toEntity(Value value) {
        return value.to(entityType).get();
    }


    protected CompletableFuture<Optional<T>> toOptionalResult(CompletableFuture<T> result) {
        CompletableFuture<Optional<T>> optionalResult =
                result.handle((v, t) -> {
                    CompletableFuture<Optional<T>> r = new CompletableFuture<>();
                    if(v != null) r.complete(Optional.of(v));
                    else if(t != null && t.getCause() instanceof NotFoundException) r.complete(Optional.empty());
                    else r.completeExceptionally(t);
                    return r;
                }).thenCompose(Function.identity());

        return optionalResult;
    }

    protected Page<T> toPage(Value value) {
        /*
         * Note that below code for extracting the data within the "after"
         * and the "before" cursors directly depends on the definition of
         * the Index from which the Page is being derived. For this particular
         * case, the Index return values should only contain the Ref field
         * from the Instances being covered by the Index. If the Index return
         * values should contain more fields, update below code accordingly.
         */
        Optional<String> after = value.at("after").asCollectionOf(Value.RefV.class).map(c -> c.iterator().next().getId()).getOptional();
        Optional<String> before = value.at("before").asCollectionOf(Value.RefV.class).map(c -> c.iterator().next().getId()).getOptional();

        List<T> data = value.at("data").collect(entityType).stream().collect(Collectors.toList());

        Page<T> page = new Page(data, before, after);

        return page;
    }
}
