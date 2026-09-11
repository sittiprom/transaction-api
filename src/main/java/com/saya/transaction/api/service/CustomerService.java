package com.saya.transaction.api.service;

import com.saya.transaction.api.dto.CustomerDto;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.exception.CustomerNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.saya.transaction.api.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    public CustomerService(CustomerRepository customerRepository, ModelMapper modelMapper) {

        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {

        Customer customer = modelMapper.map(customerDto, Customer.class);
        Customer savedCustomer = customerRepository.save(customer);
        return modelMapper.map(savedCustomer, CustomerDto.class);
    }

    public Customer findCustomerById(Long id) {

        Customer customer =  customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return customer;
    }
}
