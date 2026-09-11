package com.saya.transaction.api.service;

import com.saya.transaction.api.dto.AccountDto;
import com.saya.transaction.api.dto.CustomerDto;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.entity.Transaction;
import com.saya.transaction.api.exception.CustomerNotFoundException;
import com.saya.transaction.api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void createCustomer() {
        Customer customer = new Customer();
        customer.setEmail("test@gmail.com");
        customer.setName("Test Customer");

        CustomerDto dto = new CustomerDto();
        dto.setEmail("test@gmail.com");
        dto.setName("Test Customer");


        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        when(modelMapper.map(dto, Customer.class)).thenReturn(customer);

        customerService.createCustomer(dto);
        verify(customerRepository).save(customerCaptor.capture());

        Customer captorCustomer = customerCaptor.getValue();

        assertEquals(dto.getName(),captorCustomer.getName());
        assertEquals(dto.getEmail(),captorCustomer.getEmail());

    }

    @Test
    void findCustomerById() {
        Customer customer = new Customer();
        customer.setEmail("test@gmail.com");
        customer.setName("Test Customer");
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.findCustomerById(1L);
        verify(customerRepository).findById(1L);
        assertEquals(customer.getId(),result.getId());
        assertEquals(customer.getEmail(),result.getEmail());
        assertEquals(customer.getName(),result.getName());

    }

    @Test
    void findCustomerByIdNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.findCustomerById(1L));
    }
}