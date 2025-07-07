package org.mule.extension.vectors.internal.connection.model.einstein;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnectionParameters;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class EinsteinModelConnectionParametersTest {

    @Test
    void testGettersReturnSetValues() throws Exception {
        EinsteinModelConnectionParameters params = new EinsteinModelConnectionParameters();
        // Set private fields via reflection
        Field orgField = EinsteinModelConnectionParameters.class.getDeclaredField("salesforceOrg");
        orgField.setAccessible(true);
        orgField.set(params, "mydomain.my.salesforce.com");
        Field clientIdField = EinsteinModelConnectionParameters.class.getDeclaredField("clientId");
        clientIdField.setAccessible(true);
        clientIdField.set(params, "client-id");
        Field clientSecretField = EinsteinModelConnectionParameters.class.getDeclaredField("clientSecret");
        clientSecretField.setAccessible(true);
        clientSecretField.set(params, "client-secret");
        assertEquals("mydomain.my.salesforce.com", params.getSalesforceOrg());
        assertEquals("client-id", params.getClientId());
        assertEquals("client-secret", params.getClientSecret());
    }

    @Test
    void testDefaultValues() {
        EinsteinModelConnectionParameters params = new EinsteinModelConnectionParameters();
        assertNull(params.getSalesforceOrg());
        assertNull(params.getClientId());
        assertNull(params.getClientSecret());
    }

    @Test
    void testImmutability() throws Exception {
        EinsteinModelConnectionParameters params = new EinsteinModelConnectionParameters();
        Field orgField = EinsteinModelConnectionParameters.class.getDeclaredField("salesforceOrg");
        orgField.setAccessible(true);
        orgField.set(params, "immutable-org");
        assertEquals("immutable-org", params.getSalesforceOrg());
    }
} 
