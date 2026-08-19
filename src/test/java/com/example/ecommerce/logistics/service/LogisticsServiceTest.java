package com.example.ecommerce.logistics.service;

import com.example.ecommerce.logistics.model.CourierPartner;
import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Logistics Multi-Carrier Service & Provider Unit Tests")
class LogisticsServiceTest {

    @Mock
    private OrderDAO orderDAO;

    private LogisticsService logisticsService;

    @BeforeEach
    void setUp() {
        logisticsService = new LogisticsService(orderDAO);
    }

    @Test
    @DisplayName("Should accurately auto-detect Courier Partner from AWB prefixes")
    void testDetectPartnerByAwbPrefix() {
        assertEquals(CourierPartner.BLUEDART, CourierPartner.detectByAwb("BD-89240182"));
        assertEquals(CourierPartner.DELHIVERY, CourierPartner.detectByAwb("DL-98234190"));
        assertEquals(CourierPartner.DTDC, CourierPartner.detectByAwb("DTDC-7749201"));
        assertEquals(CourierPartner.SHIPROCKET, CourierPartner.detectByAwb("SR-882910"));
        assertEquals(CourierPartner.SHADOWFAX, CourierPartner.detectByAwb("SFX-99210"));
        assertEquals(CourierPartner.ECOM_EXPRESS, CourierPartner.detectByAwb("EE-112233"));
        assertEquals(CourierPartner.FEDEX, CourierPartner.detectByAwb("FX-445566"));
        assertEquals(CourierPartner.SPEED_POST, CourierPartner.detectByAwb("IP-778899"));
    }

    @Test
    @DisplayName("Should route BlueDart live tracking requests with complete checkpoint scan logs")
    void testBlueDartProviderTelemetry() {
        Order order = new Order();
        order.setOrderId(101);
        order.setOrderStatus(OrderStatus.IN_TRANSIT);
        order.setCourierPartner("BlueDart Express");
        order.setTrackingNumber("BD-99482018");
        order.setShippingCity("Mumbai");
        order.setShippingState("Maharashtra");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));

        TrackingResult result = logisticsService.trackLiveShipment(order);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(CourierPartner.BLUEDART, result.getCourierPartner());
        assertEquals("BD-99482018", result.getTrackingNumber());
        assertTrue(result.getCarrierTrackingUrl().contains("bluedart.com"));
        assertFalse(result.getScanHistory().isEmpty(), "BlueDart must return detailed checkpoint scans");
    }

    @Test
    @DisplayName("Should route Delhivery live tracking requests with scan details")
    void testDelhiveryProviderTelemetry() {
        Order order = new Order();
        order.setOrderId(102);
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setCourierPartner("Delhivery Logistics");
        order.setTrackingNumber("DL-88291029");
        order.setShippingCity("Delhi");
        order.setShippingState("Delhi");

        TrackingResult result = logisticsService.trackLiveShipment(order);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(CourierPartner.DELHIVERY, result.getCourierPartner());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, result.getNormalizedStatus());
        assertTrue(result.getCarrierTrackingUrl().contains("delhivery.com"));
    }

    @Test
    @DisplayName("Should route DTDC and Shiprocket live tracking correctly")
    void testDtdcAndShiprocketTelemetry() {
        Order dtdcOrder = new Order();
        dtdcOrder.setOrderId(103);
        dtdcOrder.setOrderStatus(OrderStatus.IN_TRANSIT);
        dtdcOrder.setCourierPartner("DTDC Express");
        dtdcOrder.setTrackingNumber("DTDC-556677");

        TrackingResult dtdcResult = logisticsService.trackLiveShipment(dtdcOrder);
        assertEquals(CourierPartner.DTDC, dtdcResult.getCourierPartner());

        Order srOrder = new Order();
        srOrder.setOrderId(104);
        srOrder.setOrderStatus(OrderStatus.DISPATCHED);
        srOrder.setCourierPartner("Shiprocket");
        srOrder.setTrackingNumber("SR-112233");

        TrackingResult srResult = logisticsService.trackLiveShipment(srOrder);
        assertEquals(CourierPartner.SHIPROCKET, srResult.getCourierPartner());
    }

    @Test
    @DisplayName("Should parse BlueDart real-time webhook payload")
    void testBlueDartWebhookParsing() {
        String payload = "{\"awb\":\"BD-12345678\",\"status\":\"OUT FOR DELIVERY\",\"location\":\"Bangalore Hub\",\"remarks\":\"Out for delivery by rider\"}";

        TrackingResult result = logisticsService.processCarrierWebhook("bluedart", payload, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("BD-12345678", result.getTrackingNumber());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, result.getNormalizedStatus());
        assertEquals("Bangalore Hub", result.getCurrentLocation());
    }

    @Test
    @DisplayName("Should parse Delhivery real-time webhook payload")
    void testDelhiveryWebhookParsing() {
        String payload = "{\"waybill\":\"DL-99887766\",\"status\":\"Delivered\",\"location\":\"Customer Doorstep\",\"remarks\":\"Delivered with signature\"}";

        TrackingResult result = logisticsService.processCarrierWebhook("delhivery", payload, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("DL-99887766", result.getTrackingNumber());
        assertEquals(OrderStatus.DELIVERED, result.getNormalizedStatus());
    }

    @Test
    @DisplayName("Should parse Shiprocket real-time webhook payload")
    void testShiprocketWebhookParsing() {
        String payload = "{\"awb\":\"SR-55443322\",\"current_status\":\"IN TRANSIT\",\"current_location\":\"Delhi Sort Hub\",\"courier_name\":\"BlueDart via Shiprocket\"}";

        TrackingResult result = logisticsService.processCarrierWebhook("shiprocket", payload, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SR-55443322", result.getTrackingNumber());
        assertEquals(OrderStatus.IN_TRANSIT, result.getNormalizedStatus());
    }
}
