package com.rosariob.crud.couchbase.repository;


import com.rosariob.crud.couchbase.config.ApplicationProperties;
import com.rosariob.crud.couchbase.entity.Customer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository{
    @Autowired
    CouchbaseTemplate couchbaseTemplate;

    @Autowired
    ApplicationProperties applicationProperties;
    private String scopeName;
    private String collection;

    @PostConstruct
    private void postConstruct() {
        scopeName = applicationProperties.getScopeName();
        collection = applicationProperties.getCollection();
    }

    @Override
    public List<Customer> findAll() {
        return couchbaseTemplate.findByQuery(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .all();
    }

    @Override
    public Customer create(Customer customer) {
        return couchbaseTemplate.insertById(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .one(customer);
    }

    @Override
    public Customer findById(String id) {
        return couchbaseTemplate.findById(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .one(id);
    }

    @Override
    public Customer update(Customer customer) {
        return couchbaseTemplate.replaceById(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .one(customer);
    }

    @Override
    public Customer upsert(Customer customer) {
        return couchbaseTemplate.upsertById(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .one(customer);
    }

    @Override
    public void deleteById(String id) {
        couchbaseTemplate.removeById(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .one(id);
    }

    @Override
    public void deleteAll() {
        couchbaseTemplate.removeByQuery(Customer.class)
                .inScope(scopeName)
                .inCollection(collection)
                .all();
    }
}
