package org.sid.billingservice.feign;

import org.sid.billingservice.models.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;

@CrossOrigin(origins = {"http://localhost:4200","http://localhost:8888"})
@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerRestClient {
    @GetMapping(path = "/customers/{id}")
    Customer getCustomerById(@PathVariable(name="id") Long id);

    @GetMapping(path = "/customers")
    ArrayList<Customer> getAllCustomers();
}