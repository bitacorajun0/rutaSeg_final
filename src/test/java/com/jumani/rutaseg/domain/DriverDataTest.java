package com.jumani.rutaseg.domain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DriverDataTest {
    @Test
    public void testDriverData() {
        // Arrange
        String name = "John Doe";
        String phone = "1234567890";
        String chasis = "chasis";
        String semi = "semi";
        String company = "ABC Company";

        // Act
        DriverData driverData = new DriverData(name, phone, chasis, semi, company);

        // Assert
        assertEquals(name, driverData.getName());
        assertEquals(phone, driverData.getPhone());
        assertEquals(chasis, driverData.getChasis());
        assertEquals(semi, driverData.getSemi());
        assertEquals(company, driverData.getCompany());
    }
}