package com.zhaobiao.admin.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionDatabaseSecurityValidatorTests {

    @Test
    void rejectsBlankUsernameInProd() {
        assertThrows(IllegalStateException.class,
                () -> ProductionDatabaseSecurityValidator.validateConfiguration("   ", "StrongPassword123!"));
    }

    @Test
    void rejectsRootUsernameInProd() {
        assertThrows(IllegalStateException.class,
                () -> ProductionDatabaseSecurityValidator.validateConfiguration("root", "StrongPassword123!"));
    }

    @Test
    void rejectsDefaultRootPasswordInProd() {
        assertThrows(IllegalStateException.class,
                () -> ProductionDatabaseSecurityValidator.validateConfiguration("zb_app", "root"));
    }

    @Test
    void allowsDedicatedDatabaseAccountInProd() {
        assertDoesNotThrow(() ->
                ProductionDatabaseSecurityValidator.validateConfiguration("zb_app", "StrongPassword123!"));
    }
}
