package com.jumani.rutaseg.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConsigneeTest {
    @Test
    public void testConsignee() {
        String name = "Juancho";
        long cuit = 1234567890L;

        Consignee consignee = new Consignee(name, cuit);

        assertNotNull(consignee);
        assertEquals(name, consignee.getName());
        assertEquals(cuit, consignee.getCuit());
    }

}
