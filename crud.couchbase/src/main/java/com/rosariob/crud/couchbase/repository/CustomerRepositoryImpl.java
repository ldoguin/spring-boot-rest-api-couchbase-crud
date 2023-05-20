package com.rosariob.crud.couchbase.repository;


import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rosariob.crud.couchbase.config.ApplicationProperties;
import com.rosariob.crud.couchbase.entity.Customer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository<Customer>{
    private CouchbaseTemplate couchbaseTemplate;
    private ApplicationProperties applicationProperties;
    private String bucketName;
    private String scopeName;
    private String collection;
    private String keySpace;


    @Autowired
    public CustomerRepositoryImpl(CouchbaseTemplate couchbaseTemplate, ApplicationProperties applicationProperties) {
        this.couchbaseTemplate = couchbaseTemplate;
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    private void postConstruct() {
        scopeName = applicationProperties.getScope();
        collection = applicationProperties.getCollection();
        bucketName = applicationProperties.getBucket();
        keySpace = String.join(".", bucketName, scopeName, collection);
    }

    @Override
    public List<Customer> findAll() {
        List<JsonObject> jsonObjects = couchbaseTemplate.getCouchbaseClientFactory().getScope()
                .query(String.format("SELECT * FROM %1$s ", keySpace)).rowsAsObject();
        return jsonObjects.stream().map(this::mapJsonToCustomer).collect(Collectors.toList());
     }

    @Override
    public Customer create(Customer customer) {
        couchbaseTemplate.getCollection(collection).insert(customer.getId(),customer);
        return customer;
    }

    @Override
    public Customer findById(String id) {
        return couchbaseTemplate.getCollection(collection).get(id).contentAs(Customer.class);
    }

    @Override
    public Customer update(Customer customer) {
        couchbaseTemplate.getCollection(collection).replace(customer.getId(),customer);
        return customer;
    }

    @Override
    public Customer upsert(Customer customer) {
        couchbaseTemplate.getCollection(collection).upsert(customer.getId(),customer);
        return customer;
    }

    @Override
    public void deleteById(String id) {
        couchbaseTemplate.getCollection(collection).remove(id);
    }

    @Override
    public void deleteAll() {
        couchbaseTemplate.getCouchbaseClientFactory().getScope()
                .query(String.format("DELETE FROM %1$s ", keySpace));
    }

    private Customer mapJsonToCustomer(JsonObject jsonObject){
        Customer customer = null;
        try {
            customer = new ObjectMapper().readValue(jsonObject.get(collection).toString(), Customer.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return customer;
    }
}
