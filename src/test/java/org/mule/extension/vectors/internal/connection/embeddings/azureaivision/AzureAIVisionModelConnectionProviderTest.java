package org.mule.extension.vectors.internal.connection.embeddings.azureaivision;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureAIVisionModelConnectionProviderTest {
    @Test
    void connect_returnsValidConnection() throws Exception {
        AzureAIVisionModelConnectionProvider provider = spy(new AzureAIVisionModelConnectionProvider());
        AzureAIVisionModelConnectionParameters params = new AzureAIVisionModelConnectionParameters();
        Field endpointField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("endpoint");
        endpointField.setAccessible(true);
        endpointField.set(params, "https://endpoint");
        Field apiKeyField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(params, "key");
        Field apiVersionField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("apiVersion");
        apiVersionField.setAccessible(true);
        apiVersionField.set(params, "2023-04-01-preview");
        Field timeoutField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("totalTimeout");
        timeoutField.setAccessible(true);
        timeoutField.set(params, 1234L);
        Field groupField = AzureAIVisionModelConnectionProvider.class.getDeclaredField("azureAIVisionModelConnectionParameters");
        groupField.setAccessible(true);
        groupField.set(provider, params);
        doReturn(mock(HttpClient.class)).when(provider).getHttpClient();
        BaseModelConnection conn = provider.connect();
        assertTrue(conn instanceof AzureAIVisionModelConnection);
        AzureAIVisionModelConnection azureConn = (AzureAIVisionModelConnection) conn;
        assertEquals("https://endpoint", azureConn.getEndpoint());
        assertEquals("key", azureConn.getApiKey());
        assertEquals("2023-04-01-preview", azureConn.getApiVersion());
        assertEquals(1234L, azureConn.getTimeout());
    }
} 
