package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.service.ShippingService;
import com.sahaja.swalayan.ecommerce.infrastructure.config.CacheConfig;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.BiteshipShippingClient;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.AreaResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancellationReasonResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@Import({CacheConfig.class, ShippingServiceImpl.class, ShippingServiceCachingTest.TestConfig.class})
@ActiveProfiles(profiles = "test")
class ShippingServiceCachingTest {

    @Autowired
    private ShippingService shippingService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private BiteshipShippingClient biteshipShippingClient;

    @BeforeEach
    void clearCaches() {
        // Ensure clean cache state before each test
        var areas = cacheManager.getCache(CacheConfig.CACHE_AREAS);
        if (areas != null) {
            areas.clear();
        }
        var couriers = cacheManager.getCache(CacheConfig.CACHE_COURIERS);
        if (couriers != null) {
            couriers.clear();
        }
        var reasons = cacheManager.getCache(CacheConfig.CACHE_CANCELLATION_REASONS);
        if (reasons != null) {
            reasons.clear();
        }
    }

    @Test
    void searchAreas_cachesByNormalizedInput() {
        // Arrange
        AreaResponseDTO resp = new AreaResponseDTO();
        when(biteshipShippingClient.searchAreas(Mockito.anyString())).thenReturn(resp);

        // Act
        AreaResponseDTO r1 = shippingService.searchAreas(" Jakarta "); // first call hits client
        AreaResponseDTO r2 = shippingService.searchAreas("jakarta");   // second call should hit cache (trim/lowercase key)

        // Assert
        assertThat(r1).isNotNull();
        assertThat(r2).isSameAs(r1); // same instance from cache
        verify(biteshipShippingClient, times(1)).searchAreas(Mockito.anyString());
    }

    @Test
    void getAvailableCouriers_cachesGlobal() {
        // Arrange
        CourierResponseDTO resp = new CourierResponseDTO();
        when(biteshipShippingClient.getAvailableCouriers()).thenReturn(resp);

        // Act
        CourierResponseDTO r1 = shippingService.getAvailableCouriers();
        CourierResponseDTO r2 = shippingService.getAvailableCouriers();

        // Assert
        assertThat(r1).isNotNull();
        assertThat(r2).isSameAs(r1);
        verify(biteshipShippingClient, times(1)).getAvailableCouriers();
    }

    @Test
    void getCancellationReasons_cachesByNormalizedLang() {
        // Arrange
        CancellationReasonResponseDTO resp = new CancellationReasonResponseDTO();
        when(biteshipShippingClient.getCancellationReasons(Mockito.anyString())).thenReturn(resp);

        // Act
        CancellationReasonResponseDTO r1 = shippingService.getCancellationReasons("EN");
        CancellationReasonResponseDTO r2 = shippingService.getCancellationReasons(" en ");

        // Assert
        assertThat(r1).isNotNull();
        assertThat(r2).isSameAs(r1);
        verify(biteshipShippingClient, times(1)).getCancellationReasons(Mockito.anyString());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        BiteshipShippingClient biteshipShippingClient() {
            return Mockito.mock(BiteshipShippingClient.class);
        }
    }
}
