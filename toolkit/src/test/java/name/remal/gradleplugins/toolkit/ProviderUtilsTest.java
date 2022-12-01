package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.ProviderUtils.newFixedProvider;
import static name.remal.gradleplugins.toolkit.ProviderUtils.newProvider;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.Test;

class ProviderUtilsTest {

    @Test
    void newProviderWithPresentValue() {
        val provider = newProvider(() -> "value");
        assertDoesNotThrow(provider::get);
        assertEquals("value", provider.get());
    }

    @Test
    void newProviderWithMissingValue() {
        val provider = newProvider(() -> null);
        assertThrows(IllegalStateException.class, provider::get);
    }

    @Test
    void newFixedProviderWithPresentValue() {
        val provider = newFixedProvider("value");
        assertDoesNotThrow(provider::get);
        assertEquals("value", provider.get());
    }

    @Test
    void newFixedProviderWithMissingValue() {
        val provider = newFixedProvider(null);
        assertThrows(IllegalStateException.class, provider::get);
    }

}
