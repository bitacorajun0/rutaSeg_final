package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Consignee;
import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.dto.request.ConsigneeRequest;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.repository.client.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsigneeControllerTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ConsigneeController consigneeController;

    @Test
    void createConsignee_Success() {
        Long clientId = 1L;
        SessionInfo sessionInfo = new SessionInfo(1L, true);
        ConsigneeRequest consigneeRequest = new ConsigneeRequest();
        consigneeRequest.setName("Name");
        consigneeRequest.setCuit(123456789);

        Client client = mock(Client.class);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ResponseEntity<Consignee> response = consigneeController.createConsignee(clientId, sessionInfo, consigneeRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Consignee responseBodyConsignee = response.getBody();
        assertEquals(consigneeRequest.getName(), responseBodyConsignee.getName());
        assertEquals(consigneeRequest.getCuit(), responseBodyConsignee.getCuit());
    }

    @Test
    void createConsignee_Failure_InvalidUser() {
        Long clientId = 16L;
        SessionInfo sessionInfo = new SessionInfo(16L, true);
        ConsigneeRequest consigneeRequest = new ConsigneeRequest();
        consigneeRequest.setName("Name");
        consigneeRequest.setCuit(123456789);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> consigneeController.createConsignee(clientId, sessionInfo, consigneeRequest));

        assertEquals("client_not_found", exception.getCode());
        assertEquals("client not found", exception.getMessage());
    }

    @Test
    void createConsignee_Failure_DuplicateCUIT() {
        Long clientId = 12L;
        SessionInfo sessionInfo = new SessionInfo(12L, true);
        ConsigneeRequest consigneeRequest = new ConsigneeRequest();
        consigneeRequest.setName("Name");
        consigneeRequest.setCuit(123456789);

        Client client = mock(Client.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        when(client.addConsignee(any(Consignee.class)))
                .thenThrow(new ValidationException("duplicate_cuit", "a client with the same CUIT already exists"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> consigneeController.createConsignee(clientId, sessionInfo, consigneeRequest));

        assertEquals("duplicate_cuit", exception.getCode());
        assertEquals("a client with the same CUIT already exists", exception.getMessage());
    }


    @Test
    void getAllConsignees_EmptyList_Success() {
        Long clientId = 1L;
        SessionInfo sessionInfo = new SessionInfo(clientId, true);
        Client client = mock(Client.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ResponseEntity<List<Consignee>> response = consigneeController.getAllConsignees(clientId, sessionInfo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAllConsignees_Failure_InvalidUser() {
        Long clientId = 16L;
        SessionInfo sessionInfo = new SessionInfo(16L, false);

        Client client = mock(Client.class);
        Consignee consignee = new Consignee("Consignee", 123456789);
        client.addConsignee(consignee);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> consigneeController.getAllConsignees(clientId, sessionInfo));

        assertEquals("client_not_found", exception.getCode());
        assertEquals("client not found", exception.getMessage());
    }

    @Test
    void getAllConsignees_Failure_InvalidUser_NotAdmin() {
        Long clientId = 16L;
        SessionInfo sessionInfo = new SessionInfo(16L, false);

        Client client = mock(Client.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> consigneeController.getAllConsignees(clientId, sessionInfo));

        assertEquals("client_not_found", exception.getCode());
        assertEquals("client not found", exception.getMessage());
    }
}