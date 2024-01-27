package com.jumani.rutaseg.domain;

import com.jumani.rutaseg.dto.result.Error;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.jumani.rutaseg.TestDataGen.randomId;
import static com.jumani.rutaseg.TestDataGen.randomShortString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientTest {
    @Test
    public void initialization_AllAttributes() {
        String name = randomShortString();
        String phone = randomShortString();
        long cuit = randomId();

        final User user = mock(User.class);

        Client client = new Client(user, name, phone, cuit);

        assertEquals(user, client.getUser());
        assertEquals(name, client.getName());
        assertEquals(phone, client.getPhone());
        assertEquals(cuit, client.getCuit());
        assertEquals(1, client.getConsignees().size());

        final Consignee selfConsignee = client.getConsignees().get(0);

        assertEquals(name, selfConsignee.getName());
        assertEquals(cuit, selfConsignee.getCuit());
    }

    @Test
    public void initialization_WithoutCuit() {
        String name = randomShortString();
        String phone = randomShortString();

        final User user = mock(User.class);

        Client client = new Client(user, name, phone, null);

        assertEquals(user, client.getUser());
        assertEquals(name, client.getName());
        assertEquals(phone, client.getPhone());
        assertNull(client.getCuit());
        assertTrue(client.getConsignees().isEmpty());
    }

    @Test
    public void initialization_WithoutUser_Ok() {
        String name = randomShortString();
        String phone = randomShortString();
        long cuit = randomId();

        Client client = new Client(null, name, phone, cuit);

        assertNull(client.getUser());
        assertEquals(name, client.getName());
        assertEquals(phone, client.getPhone());
        assertEquals(cuit, client.getCuit());
        assertEquals(1, client.getConsignees().size());

        final Consignee selfConsignee = client.getConsignees().get(0);

        assertEquals(name, selfConsignee.getName());
        assertEquals(cuit, selfConsignee.getCuit());
    }

    @Test
    void addConsignee_Ok() {
        String name = "Juan";
        String phone = randomShortString();
        long cuit = 1L;

        final User user = mock(User.class);

        Client client = new Client(user, name, phone, cuit);

        final String consigneeName = "Pepe";
        final long consigneecuit = 2L;

        final Optional<Error> error = client.addConsignee(new Consignee(consigneeName, consigneecuit));

        assertTrue(error.isEmpty());
        assertEquals(2, client.getConsignees().size());

        final Consignee secondConsignee = client.getConsignees().get(1);

        assertEquals(consigneeName, secondConsignee.getName());
        assertEquals(consigneecuit, secondConsignee.getCuit());
    }

    @Test
    void addConsignee_DuplicatedName_ReturnError() {
        String name = "Juan";
        String phone = randomShortString();
        long cuit = 1L;

        final User user = mock(User.class);

        Client client = new Client(user, name, phone, cuit);

        final String consigneeName = "Pepe";
        final long consigneeCuit = 2L;

        final Error expectedError = new Error("duplicated_consignee", "consignee with the same name or cuit already exists");

        client.addConsignee(new Consignee(consigneeName, consigneeCuit));
        final Optional<Error> error = client.addConsignee(new Consignee(consigneeName, 3L));

        assertEquals(Optional.of(expectedError), error);
    }

    @Test
    void addConsignee_DuplicatedCuit_ReturnError() {
        String name = "Juan";
        String phone = randomShortString();
        long cuit = 1L;

        final User user = mock(User.class);

        when(user.getNickname()).thenReturn(name);

        Client client = new Client(user, name, phone, cuit);

        final String consigneeName = "Pepe";
        final long consigneeCuit = 2L;

        final Error expectedError = new Error("duplicated_consignee", "consignee with the same name or cuit already exists");

        client.addConsignee(new Consignee(consigneeName, consigneeCuit));
        final Optional<Error> error = client.addConsignee(new Consignee("Mario", consigneeCuit));

        assertEquals(Optional.of(expectedError), error);
    }
}
