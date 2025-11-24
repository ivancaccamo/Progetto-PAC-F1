package com.ivancaccamo.progetto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    public void testAdd() {
        assertEquals(5, App.add(2, 3));
    }
}
