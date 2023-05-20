package com.rosariob.crud.couchbase.service;

import com.rosariob.crud.couchbase.entity.Customer;
import com.rosariob.crud.couchbase.repository.CustomerRepositoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CustomerServiceTestConfig.class)
public class CustomerServiceTest {
    @MockBean
    private CustomerRepositoryImpl repository;

    @Autowired
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer jack = new Customer("customer2","Jack", "Sparrow");

        when(repository.findById(alex.getId())).thenReturn(alex);
        when(repository.findAll()).thenReturn(List.of(alex,jack));
        when(repository.create(alex)).thenReturn(alex);
        when(repository.update(alex)).thenReturn(alex);
        when(repository.upsert(alex)).thenReturn(alex);
    }

    @Test
    public void findByIdOk(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        String id = "customer1";
        Customer found = customerService.findById(id);
        Assertions.assertEquals(alex, found);
    }

    @Test
    public void findAllOk(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer jack = new Customer("customer2","Jack", "Sparrow");
        List<Customer> customers = customerService.findAll();
        Assertions.assertEquals(List.of(alex, jack), customers);
    }

    @Test
    public void createOk(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer customer = customerService.create(alex);
        Assertions.assertEquals(alex, customer);
    }

    @Test
    public void updateOk(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer customer = customerService.update(alex);
        Assertions.assertEquals(alex, customer);
    }

    @Test
    public void upsertOk(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer customer = customerService.upsert(alex);
        Assertions.assertEquals(alex, customer);
    }

    @Test
    public void deleteByIdOk(){
        String customerId = "customer1";
        customerService.deleteById(customerId);
        verify(repository, times(1)).deleteById("customer1");
    }

    @Test
    public void deleteAllOk(){
        customerService.deleteAll();
        verify(repository, times(1)).deleteAll();
    }
}
