package com.rosariob.crud.couchbase.repository;

import com.rosariob.crud.couchbase.entity.Customer;
import java.util.List;

public interface CustomerRepository{
    List<Customer> findAll();
    Customer create(Customer customer);
    Customer findById(String id);
    Customer update(Customer customer);
    Customer upsert(Customer customer);
    void deleteById(String id);
    void deleteAll();
}