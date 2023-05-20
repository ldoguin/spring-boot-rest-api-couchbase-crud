package com.rosariob.crud.couchbase.rest;

import com.rosariob.crud.couchbase.entity.Customer;
import com.rosariob.crud.couchbase.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    public Customer findById(@PathVariable String customerId){
        return customerService.findById(customerId);
    }

    @GetMapping
    public List<Customer> findAll(){
        return customerService.findAll();
    }

    @PostMapping
    public Customer create(@RequestBody Customer customer){
        return  customerService.create(customer);
    }

    @PutMapping
    public Customer update(@RequestBody Customer customer){
        return customerService.update(customer);
    }

    @DeleteMapping("/{customerId}")
    public void deleteById(@PathVariable String customerId){
        customerService.deleteById(customerId);
    }

    @DeleteMapping()
    public void deleteAll(){customerService.deleteAll();};
}
