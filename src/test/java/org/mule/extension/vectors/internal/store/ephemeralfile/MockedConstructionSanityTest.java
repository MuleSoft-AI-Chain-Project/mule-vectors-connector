package org.mule.extension.vectors.internal.store.ephemeralfile;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class MockedConstructionSanityTest {
    public static class Foo {
        public String bar() { return "real"; }
    }

    @Test
    void testMockedConstruction() {
        try (MockedConstruction<Foo> construction = mockConstruction(Foo.class,
                (mock, context) -> when(mock.bar()).thenReturn("mocked"))) {
            Foo foo = new Foo();
            assertThat(foo.bar()).isEqualTo("mocked");
        }
    }
} 