package com.saya.transaction.api.controller;

import com.saya.transaction.api.dto.CustomerDto;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.service.CustomerService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final ModelMapper modelMapper;

    public CustomerController(CustomerService customerService, ModelMapper modelMapper) {
        this.customerService = customerService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/customer/create")
    public CustomerDto createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        return customerService.createCustomer(customerDto);
    }

    @GetMapping("/customer/{id}")
    public CustomerDto getCustomerDetail(@PathVariable Long id) {
        Customer customer = customerService.findCustomerById(id);
        return modelMapper.map(customer, CustomerDto.class);
    }
}
