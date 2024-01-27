package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.domain.OrderStatus;
import com.jumani.rutaseg.dto.request.CustomsDataRequest;
import com.jumani.rutaseg.dto.request.DriverDataRequest;
import com.jumani.rutaseg.dto.request.OrderRequest;
import com.jumani.rutaseg.dto.response.OrderResponse;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.repository.client.ClientRepository;
import com.jumani.rutaseg.service.order.OrderReportService;
import com.jumani.rutaseg.service.order.OrderSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static com.jumani.rutaseg.TestDataGen.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    ClientRepository clientRepo;

    @Mock
    OrderRepository orderRepo;

    @Mock
    OrderSearchService searchService;

    @Mock
    OrderReportService orderReportService;

    @Mock
    private SessionInfo sessionInfo;

    @InjectMocks
    OrderController controller;

    @Test
    public void getById_WithValidIdAndMatchingClient_ShouldReturnOrderResponse() {
        // Arrange
        String code = randomShortString();
        long orderId = 1L;
        long clientId = 1L;
        long createdByUserId = 2L;
        boolean pema = true;
        boolean port = false;
        boolean transport = true;
        final LocalDate arrivalDate = mock(LocalDate.class);
        final LocalTime arrivalTime = mock(LocalTime.class);
        final String origin = randomShortString();
        final String target = randomShortString();
        final boolean freeLoad = randomBoolean();

        OrderStatus status = OrderStatus.DRAFT;
        ZonedDateTime createdAt = ZonedDateTime.now().minusDays(1);
        ZonedDateTime finishedAt = ZonedDateTime.now();


        OrderResponse expectedResponse = new OrderResponse(
                orderId,
                code,
                clientId,
                createdByUserId,
                pema,
                port,
                transport,
                arrivalDate,
                arrivalTime,
                origin,
                target,
                freeLoad,
                status,
                createdAt,
                finishedAt,
                null,
                null,
                Collections.emptyList(),
                0,
                Collections.emptyList(),
                0,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                false
        );

        Order order = mock(Order.class);
        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getId()).thenReturn(orderId);
        when(order.getCode()).thenReturn(code);
        when(order.getClientId()).thenReturn(clientId);
        when(order.getCreatedByUserId()).thenReturn(createdByUserId);
        when(order.isPema()).thenReturn(pema);
        when(order.isPort()).thenReturn(port);
        when(order.isTransport()).thenReturn(transport);
        when(order.getArrivalDate()).thenReturn(arrivalDate);
        when(order.getArrivalTime()).thenReturn(arrivalTime);
        when(order.getOrigin()).thenReturn(origin);
        when(order.getTarget()).thenReturn(target);
        when(order.isFreeLoad()).thenReturn(freeLoad);
        when(order.getStatus()).thenReturn(status);
        when(order.getCreatedAt()).thenReturn(createdAt);
        when(order.getFinishedAt()).thenReturn(finishedAt);
        SessionInfo sessionInfo = new SessionInfo(1L, true);

        // Act
        ResponseEntity<OrderResponse> response = controller.getById(orderId, sessionInfo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponse actualResponse = response.getBody();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getById_NonAdminSessionAndDifferentClientIds_ThrowsNotFoundException() {
        // Arrange
        long orderId = 1L;
        long clientId = 2L;
        long sessionClientId = 3L;
        SessionInfo session = new SessionInfo(sessionClientId, false);

        Order order = mock(Order.class);
        Client client = mock(Client.class);

        when(order.getClient()).thenReturn(client);
        when(client.getUserId()).thenReturn(clientId);
        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            controller.getById(orderId, session);
        });

        // Verify
        verify(orderRepo, times(1)).findById(orderId);
    }


    @Test
    void createOrder_WithValidData_ReturnsOrderResponse() {
        // Arrange
        String code = randomShortString();
        boolean pema = randomBoolean();
        boolean port = randomBoolean();
        boolean transport = randomBoolean();
        long clientId = randomId();
        final LocalDate arrivalDate = mock(LocalDate.class);
        final LocalTime arrivalTime = mock(LocalTime.class);
        final String origin = randomShortString();
        final String target = randomShortString();
        final boolean freeLoad = randomBoolean();

        DriverDataRequest driverDataRequest = new DriverDataRequest();
        CustomsDataRequest customsDataRequest = new CustomsDataRequest();
        OrderRequest orderRequest = new OrderRequest(
                code, clientId, pema, port, transport, arrivalDate, arrivalTime, origin, target, freeLoad,
                driverDataRequest, customsDataRequest,
                Collections.emptyList(), Collections.emptyList(), null
        );

        SessionInfo session = new SessionInfo(randomId(), true);

        Client client = mock(Client.class);

        Order savedOrder = mock(Order.class);
        when(savedOrder.getClientId()).thenReturn(clientId);
        long createdByUserId = randomId(); // Generar un número aleatorio para createdByUserId
        when(savedOrder.getCreatedByUserId()).thenReturn(createdByUserId); // Configurar el mock para devolver el número aleatorio

        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);
        when(savedOrder.getClientId()).thenReturn(clientId);

        when(savedOrder.getId()).thenReturn(1L);
        when(savedOrder.getCode()).thenReturn(code);
        when(savedOrder.isPema()).thenReturn(pema);
        when(savedOrder.isPort()).thenReturn(port);
        when(savedOrder.isTransport()).thenReturn(transport);
        when(savedOrder.getArrivalDate()).thenReturn(arrivalDate);
        when(savedOrder.getArrivalTime()).thenReturn(arrivalTime);
        when(savedOrder.getOrigin()).thenReturn(origin);
        when(savedOrder.getTarget()).thenReturn(target);
        when(savedOrder.isFreeLoad()).thenReturn(freeLoad);
        when(savedOrder.getStatus()).thenReturn(OrderStatus.DRAFT);
        when(savedOrder.getCreatedAt()).thenReturn(ZonedDateTime.now());
        when(savedOrder.getFinishedAt()).thenReturn(null);

        OrderResponse expectedOrderResponse = new OrderResponse(
                1L, code, clientId, createdByUserId, pema, port, transport, arrivalDate, arrivalTime, origin, target, freeLoad,
                OrderStatus.DRAFT, ZonedDateTime.now(), null, null, null, Collections.emptyList(), 0,
                Collections.emptyList(), 0, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                false, false
        );

        // Act
        ResponseEntity<OrderResponse> response = controller.createOrder(orderRequest, session);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        OrderResponse actualOrderResponse = response.getBody();

        assertEquals(expectedOrderResponse, actualOrderResponse);

        verify(clientRepo).findById(clientId);
        verify(orderRepo).save(any(Order.class));
        verifyNoMoreInteractions(clientRepo, orderRepo);
    }

    @Test
    void createOrder_WithNonAdminUserAndDifferentUserId_ThrowsForbiddenException() {
        // Arrange
        long createdByUserId = randomId();
        long clientId = randomId();

        OrderRequest orderRequest = mock(OrderRequest.class);
        when(orderRequest.getClientId()).thenReturn(clientId);

        SessionInfo session = new SessionInfo(createdByUserId, false);

        Client client = mock(Client.class);

        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(client.getUserId()).thenReturn(clientId + 1);

        // Act and Assert
        assertThrows(ForbiddenException.class, () -> {
            controller.createOrder(orderRequest, session);
        });

        verify(clientRepo).findById(clientId);
        verifyNoMoreInteractions(clientRepo, orderRepo);
    }

    @Test
    void createOrder_WithNonExistentClient_ThrowsNotFoundException() {
        // Arrange
        long createdByUserId = randomId();
        long clientId = randomId();

        OrderRequest orderRequest = mock(OrderRequest.class);
        when(orderRequest.getClientId()).thenReturn(clientId);

        SessionInfo session = new SessionInfo(createdByUserId, true);

        when(clientRepo.findById(clientId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NotFoundException.class, () -> {
            controller.createOrder(orderRequest, session);
        });

        verify(clientRepo).findById(clientId);
        verifyNoMoreInteractions(clientRepo, orderRepo);
    }

    @Test
    void updateOrder_WithNonExistingOrder_ReturnsNotFound() {
        // Arrange
        long orderId = 1L;

        OrderRequest orderRequest = mock(OrderRequest.class);
        SessionInfo session = new SessionInfo(501L, true);

        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Exception exception = assertThrows(NotFoundException.class, () ->
                controller.updateOrder(orderId, orderRequest, session));

        // Assert
        assertEquals("order with id [1] not found", exception.getMessage());
        verify(orderRepo).findById(orderId);
        verifyNoMoreInteractions(orderRepo, clientRepo);
    }

    @Test
    void updateOrder_WithNonDraftOrderAndNonAdminUser_ReturnsForbidden() {
        // Arrange
        long orderId = 1L;
        OrderRequest orderRequest = mock(OrderRequest.class);

        SessionInfo session = new SessionInfo(501L, false);

        Client existingClient = mock(Client.class);
        Order existingOrder = mock(Order.class);
        when(orderRepo.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(clientRepo.findById(orderRequest.getClientId())).thenReturn(Optional.of(existingClient));
        when(existingOrder.getStatus()).thenReturn(OrderStatus.PROCESSING);
        when(existingClient.getUserId()).thenReturn(session.userId());

        // Act
        assertThrows(ValidationException.class, () -> controller.updateOrder(orderId, orderRequest, session));

        // Assert
        verify(orderRepo).findById(orderId);
        verify(clientRepo).findById(orderRequest.getClientId());
        verifyNoMoreInteractions(orderRepo, clientRepo, existingOrder);
    }

    @Test
    void updateOrder_WithValidDataAndAdminUser_ReturnsUpdatedOrderResponse() {
        // Arrange
        long orderId = 1L;
        String code = randomShortString();
        boolean pema = randomBoolean();
        boolean port = randomBoolean();
        boolean transport = randomBoolean();
        long clientId = randomId();
        final LocalDate arrivalDate = mock(LocalDate.class);
        final LocalTime arrivalTime = mock(LocalTime.class);
        final String origin = randomShortString();
        final String target = randomShortString();
        final boolean freeLoad = randomBoolean();

        DriverDataRequest driverDataRequest = new DriverDataRequest();
        CustomsDataRequest customsDataRequest = new CustomsDataRequest();
        OrderRequest orderRequest = new OrderRequest(
                code, clientId, pema, port, transport, arrivalDate, arrivalTime, origin, target, freeLoad,
                driverDataRequest, customsDataRequest,
                Collections.emptyList(), Collections.emptyList(), null
        );

        SessionInfo session = new SessionInfo(501L, true);

        Client existingClient = mock(Client.class);
        Order existingOrder = mock(Order.class);
        when(orderRepo.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(clientRepo.findById(orderRequest.getClientId())).thenReturn(Optional.of(existingClient));
        when(existingOrder.getClient()).thenReturn(existingClient);
        when(existingOrder.getStatus()).thenReturn(OrderStatus.DRAFT);
        when(orderRepo.save(any(Order.class))).thenReturn(existingOrder);

        // Act
        ResponseEntity<OrderResponse> response = controller.updateOrder(orderId, orderRequest, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(orderRepo).findById(orderId);
        verify(clientRepo).findById(orderRequest.getClientId());
        verify(existingOrder).getClient();
        verify(existingOrder).getStatus();
        verify(orderRepo).save(any(Order.class));
        verifyNoMoreInteractions(orderRepo, clientRepo);
    }

    @Test
    void testChangeOrderStatus_AdminSession_FinishedToRevision() {
        long orderId = 1L;
        OrderStatus newStatus = OrderStatus.REVISION;
        SessionInfo session = new SessionInfo(501L, true);

        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.changeOrderStatus(orderId, newStatus, session));

        verify(orderRepo).findById(orderId);
        verifyNoMoreInteractions(orderRepo);
    }

    @Test
    void testChangeOrderStatus_ClientSession_DraftToProcessing_ThrowsForbiddenException() {
        long orderId = 1L;
        OrderStatus newStatus = OrderStatus.PROCESSING;
        SessionInfo session = new SessionInfo(501L, false);

        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.changeOrderStatus(orderId, newStatus, session));

        verify(orderRepo).findById(orderId);
        verifyNoMoreInteractions(orderRepo);
    }

}






