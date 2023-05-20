package com.rosariob.crud.couchbase.repository;

import java.util.List;

public interface GenericRepository<T>{
    List<T> findAll();
    T create(T object);
    T findById(String id);
    T update(T object);
    T upsert(T object);
    void deleteById(String id);
    void deleteAll();
}