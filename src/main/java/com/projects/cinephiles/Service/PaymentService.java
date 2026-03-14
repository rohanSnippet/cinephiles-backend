package com.projects.cinephiles.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.cinephiles.DTO.BookingSuccessResponse;
import com.projects.cinephiles.DTO.LockedSeatsRequests;
import com.projects.cinephiles.DTO.PaymentRequest;
import com.projects.cinephiles.DTO.PaymentResponse;
import com.projects.cinephiles.Repo.*;
import com.projects.cinephiles.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepo orderRepository;

    @Autowired
    private UserRepo userRepo;

    @Value("${cashfree.client.id}")
    private String clientId;

    @Value("${cashfree.client.secret}")
    private String clientSecret;

    @Value("${cashfree.api.url}")
    private String cashfreeApiUrl;

    @Value("${frontend.return.url}")
    private String frontendReturnUrl;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ShowRepo showRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private ScreenRepository screenRepo;

    @Autowired
    private TheatreRepo theatreRepo;

    @Autowired
    private BookingRepository bookingRepo;

    private static final String UPPERCASE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    public static String generateUniqueBookingId() {
        int length = 3 + random.nextInt(3);
        StringBuilder randomPart = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            randomPart.append(UPPERCASE_ALPHABET.charAt(random.nextInt(UPPERCASE_ALPHABET.length())));
        }
        String timePart = String.valueOf(Instant.now().toEpochMilli()).substring(8);
        return randomPart + timePart;
    }

    public PaymentResponse createOrder(PaymentRequest request) {
        String orderId = request.getUsername().substring(0, 4) + System.currentTimeMillis();
        User user = userRepo.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User with username " + request.getUsername() + " not found.");
        }
        Optional<Show> opshow = showRepo.findById(request.getShowId());
        if(opshow.isEmpty()) return null;
        Show show = opshow.get();

        PaymentOrder order = new PaymentOrder();
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        order.setMovieId(show.getMId());
        order.setShowId(request.getShowId());
        order.setTierName(request.getTierName());
        order.setCgst(request.getCgst());
        order.setSgst(request.getSgst());
        if(request.getSeatsIds() == null) return null;
        order.setSeatIds(String.join(",", request.getSeatsIds()));
        order.setAmount(request.getAmount());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        String url = cashfreeApiUrl + "/pg/orders";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-version", "2025-01-01");
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        double roundedAmount = new BigDecimal(request.getAmount()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", orderId);
        payload.put("order_currency", "INR");
        payload.put("order_amount", roundedAmount);

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("customer_id", generateCustomerId(user.getId(),request.getUsername()));
        customerDetails.put("customer_phone", "9999999999");
        customerDetails.put("customer_email",request.getUsername());
        payload.put("customer_details", customerDetails);

        // Required: Attach return_url for modal processing
        Map<String, String> orderMeta = new HashMap<>();
        //orderMeta.put("return_url", frontendReturnUrl + "booking-confirmation?order_id={order_id}");
        orderMeta.put("return_url", frontendReturnUrl + "/payment-success?order_id={order_id}");
        payload.put("order_meta", orderMeta);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String paymentSessionId = (String) response.getBody().get("payment_session_id");
            return new PaymentResponse(paymentSessionId, orderId, orderMeta.get("return_url"));
        } else {
            throw new RuntimeException("Failed to create payment session");
        }
    }

    private String generateCustomerId(long userId, String username) {
        String namePart = username.split("@")[0];
        namePart = namePart.replaceAll("[^a-zA-Z0-9_-]", "_");
        return namePart + "_" + userId;
    }

    // --- NEW: REUSABLE CORE BOOKING LOGIC ---
    @Transactional
    public String processSuccessfulOrder(PaymentOrder order) {
        order.setStatus("PAID");
        orderRepository.save(order);

        User user = userRepo.getUserById(order.getUserId());
        LockedSeatsRequests lockedSeatsRequests = new LockedSeatsRequests();
        lockedSeatsRequests.setSeatsId(Arrays.asList(order.getSeatIds().split(",")));
        lockedSeatsRequests.setUser(user.getUsername());
        lockedSeatsRequests.setPrice(order.getAmount());
        lockedSeatsRequests.setCgst(order.getCgst());
        lockedSeatsRequests.setSgst(order.getSgst());
        lockedSeatsRequests.setTierName(order.getTierName());

        String bId = generateUniqueBookingId();
        lockedSeatsRequests.setBookingID(bId);
        lockedSeatsRequests.setShowId(order.getShowId());

        // Actually lock the seats permanently in the database
        bookingService.bookSeats(lockedSeatsRequests);
        log.info("Booking successfully generated for order: " + order.getOrderId());

        return bId;
    }

    @Transactional
    public BookingSuccessResponse verifyPayment(String orderId) {
        PaymentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Movie movie = movieRepo.findById(order.getMovieId()).orElse(null);
        Show show = showRepo.findById(order.getShowId()).orElse(null);
        if(movie == null || show == null) return null;
        Screen screen = screenRepo.findById(show.getSId()).orElse(null);
        Theatre theatre = theatreRepo.findById(screen.getTheatre().getId()).orElse(null);

        // IDEMPOTENCY CHECK: If already paid (e.g., webhook beat the frontend), return success
        if ("PAID".equalsIgnoreCase(order.getStatus())) {

            // Grab the first seat from the order's seat list (e.g. "A1" from "A1,A2")
            String firstSeat = order.getSeatIds().split(",")[0].trim();

            // Use the new custom query to find the booking
            Optional<Booking> existingBookingOpt = bookingRepo.findByShowIdAndSeatIdPart(order.getShowId(), firstSeat);

            String bId = existingBookingOpt.isPresent() ? existingBookingOpt.get().getBookingID() : "PROCESSED";

            return new BookingSuccessResponse(true, "Payment verified.", movie.getTitle(), movie.getPoster(), bId,
                    String.valueOf(movie.getCertification()), theatre.getName(), theatre.getAddress(), theatre.getCity(),
                    screen.getSname(), order.getTierName(), order.getSeatIds(), show.getFormat(), show.getStart(),
                    show.getShowDate(), order.getAmount(), order.getCgst(), order.getSgst());
        }

        String url = cashfreeApiUrl + "/pg/orders/" + orderId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-version", "2025-01-01");
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String status = (String) response.getBody().get("order_status");

            if ("PAID".equalsIgnoreCase(status)) {
                String bId = processSuccessfulOrder(order);
                return new BookingSuccessResponse(true, "Payment made successfully.", movie.getTitle(), movie.getPoster(), bId,
                        String.valueOf(movie.getCertification()), theatre.getName(), theatre.getAddress(), theatre.getCity(),
                        screen.getSname(), order.getTierName(), order.getSeatIds(), show.getFormat(), show.getStart(),
                        show.getShowDate(), order.getAmount(), order.getCgst(), order.getSgst());
            } else if ("FAILED".equalsIgnoreCase(status)) {
                order.setStatus("FAILED");
                orderRepository.save(order);
                return new BookingSuccessResponse(false, "Payment unsuccessful", null, null, null, null, null, null, null, null, null, null,null,null, null, null, null, null);
            }
        }
        throw new RuntimeException("Failed to verify payment with Cashfree");
    }

    // --- NEW WEBHOOK PROCESSING LOGIC ---
    public void handleWebhook(String rawPayload, String signature, String timestamp) {
        try {
            if (signature == null || timestamp == null) {
                log.error("Webhook rejected: Missing security headers");
                return;
            }

            // 1. Verify Cashfree Signature (CRITICAL SECURITY)
            String dataToSign = timestamp + rawPayload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            String calculatedSignature = Base64.getEncoder().encodeToString(mac.doFinal(dataToSign.getBytes()));

            if (!calculatedSignature.equals(signature)) {
                log.error("Webhook rejected: Signature verification failed!");
                return;
            }

            // 2. Parse Webhook Payload
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(rawPayload);

            JsonNode dataNode = rootNode.path("data");
            JsonNode orderNode = dataNode.path("order");
            JsonNode paymentNode = dataNode.path("payment");

            String orderId = orderNode.path("order_id").asText();
            String paymentStatus = paymentNode.path("payment_status").asText();

            Optional<PaymentOrder> opOrder = orderRepository.findById(orderId);
            if (opOrder.isEmpty()) {
                log.error("Webhook: Order not found in DB: " + orderId);
                return;
            }

            PaymentOrder order = opOrder.get();

            // 3. Idempotency Check: Don't process twice
            if ("PAID".equalsIgnoreCase(order.getStatus())) {
                log.info("Webhook: Order " + orderId + " is already PAID. Skipping.");
                return;
            }

            // 4. Execute Core Booking Logic
            if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
                log.info("Webhook received SUCCESS for order " + orderId + ". Booking seats in background...");
                processSuccessfulOrder(order);
            } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
                order.setStatus("FAILED");
                orderRepository.save(order);
                log.info("Webhook marked order " + orderId + " as FAILED.");
            }

        } catch (Exception e) {
            log.error("Error processing Cashfree webhook", e);
        }
    }
}