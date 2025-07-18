package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.BookingSuccessResponse;
import com.projects.cinephiles.DTO.LockedSeatsRequests;
import com.projects.cinephiles.DTO.PaymentRequest;
import com.projects.cinephiles.DTO.PaymentResponse;
import com.projects.cinephiles.Repo.PaymentRepo;
import com.projects.cinephiles.Repo.ShowRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.*;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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


    public PaymentResponse createOrder(PaymentRequest request) {
        // 3. Create orderId
        String orderId = request.getUsername().substring(0, 4) + System.currentTimeMillis();
        User user = userRepo.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User with username " + request.getUsername() + " not found.");
        }

        System.out.println(user + " is the user ....");
        // 4. Save order in DB
        PaymentOrder order = new PaymentOrder();
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        order.setShowId(request.getShowId());
        order.setSeatIds(request.getSeatIds());
        order.setAmount(request.getAmount());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        System.out.println("order saved in db");


        // 5. Call Cashfree API
        String url = cashfreeApiUrl + "/pg/orders";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-version", "2025-01-01");
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
//        headers.set("Content-Type", "application/json");
//        headers.set("Accept", "application/
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        System.out.println(url+" called.....");

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", orderId);
        payload.put("order_currency", "INR");
        payload.put("order_amount", request.getAmount());

        System.out.println(payload+" Payload set.....");

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("customer_id", generateCustomerId(user.getId(),request.getUsername()));
        customerDetails.put("customer_phone", "9999999999");
        customerDetails.put("customer_email",request.getUsername());
        payload.put("customer_details", customerDetails);


        System.out.println(customerDetails+" CustomerDetails set.....");

        //Extra block added
//        Optional<Show> opShow = showRepo.findById(request.getShowId());
//        if(opShow.isEmpty()) return null;
//
//        Show show = opShow.get();
//        Movie movie = show.getMovie();
//
//        Map<String, Object> cartDetails = new HashMap<>();
//        List<Map<String, Object>> cartItems = new ArrayList<>();
//        Map<String, Object> item = new HashMap<>();
//        item.put("item_name", movie.getTitle());
//        Double amt = request.getAmount()/request.getSeatIds().size();
//        item.put("item_quantity", request.getSeatIds().size());
//        item.put("item_price", amt);
//        cartItems.add(item);
//        cartDetails.put("cart_items", cartItems);
//        payload.put("cart_details", cartDetails);
//
//        System.out.println(cartDetails+" Movie Details set.....");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(entity);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String paymentSessionId = (String) response.getBody().get("payment_session_id");
            String returnUrl = frontendReturnUrl + "/payment-success?orderId=" + orderId;
            return new PaymentResponse(paymentSessionId, orderId, returnUrl);
        } else {
            throw new RuntimeException("Failed to create payment session");
        }
    }

    private String generateCustomerId(long userId, String username) {
        // 1. Take only the part before '@' from email
        String namePart = username.split("@")[0];
        // 2. Remove any invalid characters (keep letters, digits, underscores)
        namePart = namePart.replaceAll("[^a-zA-Z0-9_-]", "_");
        // 3. Append userId to ensure uniqueness
        return namePart + "_" + userId;
    }

    public Map<String, Object> verifyPayment(String orderId) {
        PaymentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Call Cashfree Get Order API
        String url = cashfreeApiUrl + "/pg/orders/" + orderId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-version", "2023-08-01");
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
        System.out.println(url+" called.....");
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String status = (String) response.getBody().get("order_status");

            if ("PAID".equalsIgnoreCase(status)) {
                // Update DB
                order.setStatus("PAID");
                orderRepository.save(order);
                User user = userRepo.getUserById(order.getUserId());
                LockedSeatsRequests lockedSeatsRequests = new LockedSeatsRequests();
                lockedSeatsRequests.setSeatsId(order.getSeatIds());
                lockedSeatsRequests.setUser(user.getUsername());
                lockedSeatsRequests.setShowId(order.getShowId());
                bookingService.bookSeats(lockedSeatsRequests);

                System.out.println("success called ....");


                return Map.of("success", true, "message", "Payment successful. Seats booked.");
            } else {
                return Map.of("success", false, "message", "Payment status: " + status);
            }
        } else {
            throw new RuntimeException("Failed to verify payment with Cashfree");
        }
    }
}
