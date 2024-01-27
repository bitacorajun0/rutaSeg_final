package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.domain.Consignee;
import com.jumani.rutaseg.domain.User;
import com.jumani.rutaseg.dto.request.ClientRequest;
import com.jumani.rutaseg.dto.response.ClientResponse;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.repository.client.ClientRepository;
import com.jumani.rutaseg.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.jumani.rutaseg.TestDataGen.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    ClientRepository clientRepo;
    @Mock
    UserRepository userRepo;

    @InjectMocks
    ClientController controller;

    @Test
    void create_WithUser_Ok() {
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final long userId = randomId();
        final List<Consignee> consignees = List.of(new Consignee(randomShortString(), randomId()));
        final long clientId = randomId();

        final SessionInfo session = new SessionInfo(1L, true);
        final ClientRequest request = new ClientRequest(name, phone, cuit, userId);

        final User user = mock(User.class);
        final Client savedClient = mock(Client.class);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(clientRepo.findOneByUser_Id(userId)).thenReturn(Optional.empty());
        when(clientRepo.save(any(Client.class))).thenReturn(savedClient);

        when(savedClient.getId()).thenReturn(clientId);
        when(savedClient.getUserId()).thenReturn(userId);
        when(savedClient.getName()).thenReturn(name);
        when(savedClient.getPhone()).thenReturn(phone);
        when(savedClient.getCuit()).thenReturn(cuit);
        when(savedClient.getConsignees()).thenReturn(consignees);

        final ClientResponse expectedClientResponse = new ClientResponse(clientId, userId, name, phone, cuit, consignees);
        final ResponseEntity<ClientResponse> response = controller.create(request, session);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedClientResponse, response.getBody());

        verifyNoMoreInteractions(clientRepo, userRepo);
    }

    @Test
    void create_WithoutUser_Ok() {
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final List<Consignee> consignees = new ArrayList<>();
        final long clientId = randomId();

        final SessionInfo session = new SessionInfo(1L, true);
        final ClientRequest request = new ClientRequest(name, phone, cuit, null);

        final Client savedClient = mock(Client.class);

        when(clientRepo.save(any(Client.class))).thenReturn(savedClient);

        when(savedClient.getId()).thenReturn(clientId);
        when(savedClient.getUserId()).thenReturn(null);
        when(savedClient.getName()).thenReturn(name);
        when(savedClient.getPhone()).thenReturn(phone);
        when(savedClient.getCuit()).thenReturn(cuit);
        when(savedClient.getConsignees()).thenReturn(consignees);

        final ClientResponse expectedClientResponse = new ClientResponse(clientId, null, name, phone, cuit, consignees);
        final ResponseEntity<ClientResponse> response = controller.create(request, session);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedClientResponse, response.getBody());

        verifyNoMoreInteractions(clientRepo, userRepo);
    }

    @Test
    void create_UserNotFound_ValidationException() {
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final long userId = randomId();

        final SessionInfo session = new SessionInfo(1L, true);
        final ClientRequest request = new ClientRequest(name, phone, cuit, userId);

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        final ValidationException valEx = assertThrows(ValidationException.class, () -> controller.create(request, session));

        assertEquals("user_not_found", valEx.getCode());
        assertEquals(String.format("user with id [%s] not found", userId), valEx.getMessage());

        verifyNoInteractions(clientRepo);
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    void create_UserAlreadyTaken_ValidationException() {
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final long userId = randomId();

        final SessionInfo session = new SessionInfo(1L, true);
        final ClientRequest request = new ClientRequest(name, phone, cuit, userId);

        final User user = mock(User.class);
        final Client existentClient = mock(Client.class);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(clientRepo.findOneByUser_Id(userId)).thenReturn(Optional.of(existentClient));

        final ValidationException valEx = assertThrows(ValidationException.class, () -> controller.create(request, session));

        assertEquals("user_already_taken", valEx.getCode());
        assertEquals("a client with the same user already exists", valEx.getMessage());

        verifyNoMoreInteractions(clientRepo, userRepo);
    }

    @Test
    void create_NotAdminSession_Forbidden() {
        final SessionInfo session = new SessionInfo(1L, false);

        final ClientRequest request = new ClientRequest();

        assertThrows(ForbiddenException.class, () -> controller.create(request, session));

        verifyNoInteractions(clientRepo, userRepo);
    }

    @Test
    void search_AllAttributes_AdminSession_WithConsignees_FirstPage_Ok() {
        final SessionInfo session = new SessionInfo(1L, true);

        final long userId = randomId();
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final boolean withUser = randomBoolean();
        final long totalElements = randomPositiveInt(100);
        final int pageSize = randomPositiveInt(10);

        final long id = randomId();
        final List<Consignee> consignees = List.of(mock(Consignee.class));

        final Client client = mock(Client.class);
        when(client.getId()).thenReturn(id);
        when(client.getUserId()).thenReturn(userId);
        when(client.getName()).thenReturn(name);
        when(client.getPhone()).thenReturn(phone);
        when(client.getCuit()).thenReturn(cuit);
        when(client.getConsignees()).thenReturn(consignees);

        when(clientRepo.count(userId, name, phone, cuit, withUser)).thenReturn(totalElements);
        when(clientRepo.search(userId, name, phone, cuit, withUser, 0, pageSize)).thenReturn(List.of(client));

        final ClientResponse expectedResponse = new ClientResponse(id, userId, name, phone, cuit, consignees);

        final ResponseEntity<PaginatedResult<ClientResponse>> result =
                controller.search(userId, name, phone, cuit, withUser, pageSize, 1, true, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());

        final PaginatedResult<ClientResponse> paginatedResult = result.getBody();
        assertEquals(List.of(expectedResponse), paginatedResult.elements());
    }

    @Test
    void search_AllAttributes_AdminSession_WithoutConsignees_SecondPage_Ok() {
        final SessionInfo session = new SessionInfo(1L, true);

        final long userId = randomId();
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final boolean withUser = randomBoolean();
        final long totalElements = 20;
        final int pageSize = 5;

        final long id = randomId();

        final Client client = mock(Client.class);
        when(client.getId()).thenReturn(id);
        when(client.getUserId()).thenReturn(userId);
        when(client.getName()).thenReturn(name);
        when(client.getPhone()).thenReturn(phone);
        when(client.getCuit()).thenReturn(cuit);

        when(clientRepo.count(userId, name, phone, cuit, withUser)).thenReturn(totalElements);
        when(clientRepo.search(userId, name, phone, cuit, withUser, pageSize, pageSize)).thenReturn(List.of(client));

        final ClientResponse expectedResponse = new ClientResponse(id, userId, name, phone, cuit, Collections.emptyList());

        final ResponseEntity<PaginatedResult<ClientResponse>> result =
                controller.search(userId, name, phone, cuit, withUser, pageSize, 2, false, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());

        final PaginatedResult<ClientResponse> paginatedResult = result.getBody();
        assertEquals(List.of(expectedResponse), paginatedResult.elements());
        assertEquals(totalElements, paginatedResult.totalElements());
        assertEquals(4, paginatedResult.totalPages());
        assertEquals(2, paginatedResult.page());
        assertEquals(pageSize, paginatedResult.pageSize());
    }

    @Test
    void search_NotAllAttributes_NotAdminSession_WithoutConsignees_Ok() {
        final long sessionUserId = randomId();
        final SessionInfo session = new SessionInfo(sessionUserId, false);

        final long ignoredUserId = randomId();
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();
        final int ignoredPageSize = 100;

        final long id = randomId();

        final Client client = mock(Client.class);
        when(client.getId()).thenReturn(id);
        when(client.getUserId()).thenReturn(sessionUserId);
        when(client.getName()).thenReturn(name);
        when(client.getPhone()).thenReturn(phone);
        when(client.getCuit()).thenReturn(cuit);

        when(clientRepo.search(sessionUserId, name, phone, cuit, null, 0, 1)).thenReturn(List.of(client));

        final ClientResponse expectedResponse = new ClientResponse(id, sessionUserId, name, phone, cuit, Collections.emptyList());

        final ResponseEntity<PaginatedResult<ClientResponse>> result =
                controller.search(sessionUserId, name, phone, cuit, null, ignoredPageSize, 1, false, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());

        final PaginatedResult<ClientResponse> paginatedResult = result.getBody();
        assertEquals(List.of(expectedResponse), paginatedResult.elements());
        assertEquals(1, paginatedResult.totalElements());
        assertEquals(1, paginatedResult.totalPages());
        assertEquals(1, paginatedResult.page());
        assertEquals(1, paginatedResult.pageSize());
    }

    @Test
    void getById_AdminSession_WithConsignees_Ok() {
        final SessionInfo session = new SessionInfo(randomId(), true);

        final long userId = randomId();
        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();

        final long id = randomId();
        final List<Consignee> consignees = List.of(mock(Consignee.class));

        final Client client = mock(Client.class);
        when(client.getId()).thenReturn(id);
        when(client.getUserId()).thenReturn(userId);
        when(client.getName()).thenReturn(name);
        when(client.getPhone()).thenReturn(phone);
        when(client.getCuit()).thenReturn(cuit);
        when(client.getConsignees()).thenReturn(consignees);

        when(clientRepo.findById(id)).thenReturn(Optional.of(client));

        final ClientResponse expectedResponse = new ClientResponse(id, userId, name, phone, cuit, consignees);

        final ResponseEntity<ClientResponse> result = controller.getById(id, true, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void getById_AdminSession_NotFound() {
        final SessionInfo session = new SessionInfo(randomId(), true);

        final long id = randomId();

        when(clientRepo.findById(id)).thenReturn(Optional.empty());

        final NotFoundException notFoundEx = assertThrows(NotFoundException.class,
                () -> controller.getById(id, false, session));

        assertEquals(String.format("client with id [%s] not found", id), notFoundEx.getMessage());
    }

    @Test
    void getById_NotAdminSession_WithoutConsignees_Ok() {
        final long userId = randomId();
        final SessionInfo session = new SessionInfo(userId, false);

        final String name = randomShortString();
        final String phone = randomShortString();
        final long cuit = randomId();

        final long id = randomId();

        final Client client = mock(Client.class);
        when(client.getId()).thenReturn(id);
        when(client.getUserId()).thenReturn(userId);
        when(client.getName()).thenReturn(name);
        when(client.getPhone()).thenReturn(phone);
        when(client.getCuit()).thenReturn(cuit);

        when(clientRepo.findById(id)).thenReturn(Optional.of(client));

        final ClientResponse expectedResponse = new ClientResponse(id, userId, name, phone, cuit, Collections.emptyList());

        final ResponseEntity<ClientResponse> result = controller.getById(id, false, session);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void getById_NotAdminSession_FromAnotherUser_NotFound() {
        final long userId = randomId();
        final SessionInfo session = new SessionInfo(userId, false);

        final long id = randomId();

        final Client client = mock(Client.class);
        when(client.getUserId()).thenReturn(userId + 1);

        when(clientRepo.findById(id)).thenReturn(Optional.of(client));

        final NotFoundException notFoundEx = assertThrows(NotFoundException.class,
                () -> controller.getById(id, false, session));

        assertEquals(String.format("client with id [%s] not found", id), notFoundEx.getMessage());
    }
}
