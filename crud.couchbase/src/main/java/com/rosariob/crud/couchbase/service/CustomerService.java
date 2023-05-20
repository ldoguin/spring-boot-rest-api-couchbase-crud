package com.rosariob.crud.couchbase.service;

import com.rosariob.crud.couchbase.entity.Customer;
import com.rosariob.crud.couchbase.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService implements GenericService<Customer> {
    @Autowired
    private CustomerRepository repository;

    @Override
    public Customer findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Override
    public Customer create(Customer customer) {
        return repository.create(customer);
    }

    @Override
    public Customer update(Customer customer) {
        return repository.update(customer);
    }

    @Override
    public Customer upsert(Customer customer) {
        return repository.upsert(customer);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
