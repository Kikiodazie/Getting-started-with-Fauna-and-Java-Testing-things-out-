package com.odazie.faunadataapiandjava.data;


import com.faunadb.client.query.Pagination;
import com.odazie.faunadataapiandjava.data.common.Page;
import com.odazie.faunadataapiandjava.data.common.PaginationOptions;
import com.odazie.faunadataapiandjava.persistence.FaunaDBRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;

import static com.faunadb.client.query.Language.*;

@Repository
public class TodoRepository extends FaunaDBRepository<TodoEntity> {

    public TodoRepository(){
        super(TodoEntity.class, "todo", "all_todos");
    }

    public CompletableFuture<Page<TodoEntity>> findByTitle(String title, PaginationOptions po) {
        Pagination paginationQuery = Paginate(Match(Index(Value("todos_by_title")), Value(title)));
        po.getSize().ifPresent(size -> paginationQuery.size(size));
        po.getAfter().ifPresent(after -> paginationQuery.after(Ref(Class(collectionName), Value(after))));
        po.getBefore().ifPresent(before -> paginationQuery.before(Ref(Class(collectionName), Value(before))));

        CompletableFuture<Page<TodoEntity>> result =
                faunaClient.query(
                        Map(
                                paginationQuery,
                                Lambda(Value("nextRef"), Select(Value("data"), Get(Var("nextRef"))))
                        )
                )
                        .thenApply(this::toPage);

        return result;
    }


}
