package com.jumani.rutaseg.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jumani.rutaseg.TestDataGen.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class OrderTest {

    @Test
    public void testOrderInitialization_WithContainers() {
        // Arrange
        String code = randomShortString();
        boolean pema = randomBoolean();
        boolean port = randomBoolean();
        boolean transport = randomBoolean();
        long createdByUserId = randomId();
        final LocalDate arrivalDate = mock(LocalDate.class);
        final LocalTime arrivalTime = mock(LocalTime.class);
        final String origin = randomShortString();
        final String target = randomShortString();

        DriverData driverData = new DriverData("John Doe", "1234567890", "chasis", "semi", "ABC Company");
        CustomsData customsData = new CustomsData("Customs Name", "9876543210");
        Client client = new Client(new User("John", "password", "john@example.com", false),
                "name", "1234567890", 123456789L);
        List<Destination> destinations = List.of(new Destination(randomEnum(DestinationType.class), "code", "fob", "currency", "details"));
        Container container1 = new Container("ABC123", ContainerType.ST_20, false, "BL1", "PEMA1", destinations);
        Container container2 = new Container("XYZ789", ContainerType.OS_20, true, "BL2", "PEMA2", destinations);
        List<Container> containers = Arrays.asList(container1, container2);
        ConsigneeData consigneeData = new ConsigneeData("Consignee Name", 123456789L);

        // Act
        final Order order = new Order(code, client, pema, port, transport, arrivalDate, arrivalTime, origin, target, false,
                driverData, customsData, containers, Collections.emptyList(), consigneeData, createdByUserId);
        // Assert
        assertEquals(code, order.getCode());
        assertEquals(pema, order.isPema());
        assertEquals(port, order.isPort());
        assertEquals(transport, order.isTransport());
        assertEquals(arrivalDate, order.getArrivalDate());
        assertEquals(arrivalTime, order.getArrivalTime());
        assertEquals(origin, order.getOrigin());
        assertEquals(target, order.getTarget());
        assertFalse(order.isFreeLoad());
        assertEquals(driverData, order.getDriverData());
        assertEquals(customsData, order.getCustomsData());
        assertNotNull(order.getCreatedAt());
        assertNull(order.getFinishedAt());
        assertEquals(createdByUserId, order.getCreatedByUserId());
        assertEquals(client, order.getClient());
        assertEquals(consigneeData, order.getConsignee());
        assertEquals(containers, order.getContainers());
        assertEquals(containers.size(), order.getContainerQty());
        assertEquals(order.getCreatedAt().truncatedTo(ChronoUnit.DAYS).toEpochSecond(), order.getCreatedAtIdx());
    }

    @Test
    public void testOrderInitialization_WithFreeLoad() {
        // Arrange
        String code = randomShortString();
        boolean pema = randomBoolean();
        boolean port = randomBoolean();
        boolean transport = randomBoolean();
        final LocalDate arrivalDate = mock(LocalDate.class);
        final LocalTime arrivalTime = mock(LocalTime.class);
        final String origin = randomShortString();
        final String target = randomShortString();

        long createdByUserId = randomId();

        DriverData driverData = new DriverData("John Doe", "1234567890", "chasis", "semi", "ABC Company");
        CustomsData customsData = new CustomsData("Customs Name", "9876543210");
        Client client = new Client(new User("John", "password", "john@example.com", false),
                "name", "1234567890", 123456789L);
        FreeLoad freeLoad1 = new FreeLoad("123456", FreeLoadType.SEMI, "20kg", "Guide1", "PEMA1", null);
        FreeLoad freeLoad2 = new FreeLoad("123456", FreeLoadType.SEMI, "20kg", "Guide1", "PEMA1", null);
        List<FreeLoad> freeLoads = Arrays.asList(freeLoad1, freeLoad2);
        ConsigneeData consigneeData = new ConsigneeData("Consignee Name", 123456789L);

        // Act
        final Order order = new Order(code, client, pema, port, transport, arrivalDate, arrivalTime, origin, target, true,
                driverData, customsData, Collections.emptyList(), freeLoads, consigneeData, createdByUserId);

        // Assert
        assertEquals(code, order.getCode());
        assertEquals(pema, order.isPema());
        assertEquals(port, order.isPort());
        assertEquals(arrivalDate, order.getArrivalDate());
        assertEquals(arrivalTime, order.getArrivalTime());
        assertEquals(origin, order.getOrigin());
        assertEquals(target, order.getTarget());
        assertTrue(order.isFreeLoad());
        assertEquals(transport, order.isTransport());
        assertEquals(driverData, order.getDriverData());
        assertEquals(customsData, order.getCustomsData());
        assertNotNull(order.getCreatedAt());
        assertNull(order.getFinishedAt());
        assertEquals(createdByUserId, order.getCreatedByUserId());
        assertEquals(client, order.getClient());
        assertEquals(consigneeData, order.getConsignee());
        assertEquals(freeLoads, order.getFreeLoads());
        assertEquals(freeLoads.size(), order.getFreeLoadQty());
    }

    @Test
    void testUpdateMethod() {
        // Arrange
        Client originalClient = new Client(new User("John", "password", "john@example.com", false),
                "Original Client", "1234567890", 123456789L);
        boolean originalPema = false;
        boolean originalPort = true;
        boolean originalTransport = true;
        final LocalDate originalArrivalDate = mock(LocalDate.class);
        final LocalTime originalArrivalTime = mock(LocalTime.class);
        final String originalOrigin = randomShortString();
        final String originalTarget = randomShortString();
        final boolean originalFreeLoad = randomBoolean();

        DriverData originalDriverData = new DriverData(
                "John Doe",
                "1234567890",
                null, null, "ABC Company"
        );
        CustomsData originalCustomsData = new CustomsData(
                "Customs Name",
                "9876543210"
        );
        long originalCreatedByUserId = 12345L;
        List<Container> originalContainers = new ArrayList<>();
        ConsigneeData originalConsigneeData = new ConsigneeData(
                "Consignee Name",
                67890L
        );

        Order order = new Order("code-1", originalClient, originalPema, originalPort, originalTransport,
                originalArrivalDate, originalArrivalTime, originalOrigin, originalTarget, originalFreeLoad,
                originalDriverData, originalCustomsData, originalContainers, new ArrayList<>(), originalConsigneeData, originalCreatedByUserId);

        String updatedCode = "code-2";
        Client updatedClient = new Client(new User("Jane", "password", "jane@example.com", false),
                "Updated Client", "9876543210", 987654321L);
        boolean updatedPema = true;
        boolean updatedPort = false;
        boolean updatedTransport = false;

        final LocalDate updatedArrivalDate = mock(LocalDate.class);
        final LocalTime updatedArrivalTime = mock(LocalTime.class);
        final String updatedOrigin = randomShortString();
        final String updatedTarget = randomShortString();
        final boolean updatedFreeLoad = randomBoolean();

        DriverData updatedDriverData = new DriverData(
                "Jane Smith",
                "9876543210",
                "chasis", "semi", "XYZ Transport"
        );
        CustomsData updatedCustomsData = new CustomsData(
                "Updated Customs",
                "1234567890"
        );
        List<Container> updatedContainers = new ArrayList<>();
        List<Destination> destinations = List.of(new Destination(randomEnum(DestinationType.class), "code", "fob", "currency", "details"));
        updatedContainers.add(new Container("ABC123", ContainerType.ST_20, false, "BL1", "PEMA1", destinations));
        updatedContainers.add(new Container("XYZ789", ContainerType.OS_20, true, "BL2", "PEMA2", destinations));

        FreeLoad freeLoad1 = new FreeLoad("123456", FreeLoadType.SEMI, "20kg", "Guide1", "PEMA1", destinations);
        List<FreeLoad> updatedFreeLoads = List.of(freeLoad1);

        ConsigneeData updatedConsigneeData = new ConsigneeData(
                "New Consignee",
                54321L
        );

        // Act
        order.update(updatedCode, updatedClient, updatedPema, updatedPort, updatedTransport,
                updatedArrivalDate, updatedArrivalTime, updatedOrigin, updatedTarget, updatedFreeLoad,
                updatedDriverData, updatedCustomsData,
                updatedContainers, updatedFreeLoads, updatedConsigneeData);

        // Assert
        assertEquals(updatedCode, order.getCode());
        assertEquals(updatedClient, order.getClient());
        assertEquals(updatedPema, order.isPema());
        assertEquals(updatedPort, order.isPort());
        assertEquals(updatedArrivalDate, order.getArrivalDate());
        assertEquals(updatedArrivalTime, order.getArrivalTime());
        assertEquals(updatedOrigin, order.getOrigin());
        assertEquals(updatedTarget, order.getTarget());
        assertEquals(updatedFreeLoad, order.isFreeLoad());
        assertEquals(updatedTransport, order.isTransport());
        assertEquals(updatedDriverData, order.getDriverData());
        assertEquals(updatedCustomsData, order.getCustomsData());
        assertEquals(updatedContainers, order.getContainers());
        assertEquals(updatedContainers.size(), order.getContainerQty());
        assertEquals(updatedFreeLoads, order.getFreeLoads());
        assertEquals(updatedFreeLoads.size(), order.getFreeLoadQty());
        assertEquals(updatedConsigneeData, order.getConsignee());
    }

    @Test
    void testUpdateStatusMethod() {
        Client client = new Client(new User("John", "password", "john@example.com", false),
                "name", "1234567890", 123456789L);

        boolean pema = randomBoolean();
        boolean port = randomBoolean();
        boolean transport = randomBoolean();
        long createdByUserId = randomId();

        DriverData driverData = new DriverData("John Doe", "1234567890", "chasis", "semi", "ABC Company");
        CustomsData customsData = new CustomsData("Customs Name", "9876543210");
        ConsigneeData consigneeData = new ConsigneeData("Consignee Name", 123456789L);

        Container container1 = new Container("ABC123", ContainerType.ST_20, false, "BL1", "PEMA1", null);
        Container container2 = new Container("XYZ789", ContainerType.OS_20, true, "BL2", "PEMA2", null);
        List<Container> containers = new ArrayList<>(Arrays.asList(container1, container2));

        Order order = new Order(randomShortString(), client, pema, port, transport,
                null, null, null, null, false, driverData, customsData,
                containers, Collections.emptyList(), consigneeData, createdByUserId);

        OrderStatus newStatus = OrderStatus.DRAFT;

        order.updateStatus(newStatus);

        assertEquals(newStatus, order.getStatus());
    }


}
