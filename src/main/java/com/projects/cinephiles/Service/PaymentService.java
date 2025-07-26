package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.BookingSuccessResponse;
import com.projects.cinephiles.DTO.LockedSeatsRequests;
import com.projects.cinephiles.DTO.PaymentRequest;
import com.projects.cinephiles.DTO.PaymentResponse;
import com.projects.cinephiles.Repo.*;
import com.projects.cinephiles.models.*;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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
    private OrderRepo orderRepo;

    @Autowired
    private BookingRepository bookingRepo;

    private static final String UPPERCASE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();
    public static String generateUniqueBookingId() {
        int length = 3 + random.nextInt(3); // Random part length 3 to 5
        StringBuilder randomPart = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            randomPart.append(UPPERCASE_ALPHABET.charAt(random.nextInt(UPPERCASE_ALPHABET.length())));
        }
        // Add last 5 digits of current time in milliseconds to ensure uniqueness
        String timePart = String.valueOf(Instant.now().toEpochMilli()).substring(8);
        return randomPart + timePart;
    }


    public PaymentResponse createOrder(PaymentRequest request) {
        // 3. Create orderId
        System.out.println(request);
        String orderId = request.getUsername().substring(0, 4) + System.currentTimeMillis();
        User user = userRepo.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User with username " + request.getUsername() + " not found.");
        }
         Optional<Show> opshow = showRepo.findById(request.getShowId());
        if(opshow.isEmpty()) return null;
        Show show = opshow.get();

        System.out.println(user + " is the user ....");
        // 4. Save order in DB
        PaymentOrder order = new PaymentOrder();
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        order.setMovieId(show.getMId());
        order.setShowId(request.getShowId());
        order.setTierName(request.getTierName());
        order.setCgst(request.getCgst());
        order.setSgst(request.getSgst());
        System.out.println("seats are : "+String.join(",", request.getSeatsIds()));
        if(request.getSeatsIds() == null) return null;
        order.setSeatIds(String.join(",", request.getSeatsIds()));
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
        payload.put("order_amount", request.getAmount()+request.getSgst()+request.getCgst());

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

    public BookingSuccessResponse verifyPayment(String orderId) {
        System.out.println("Order Id is : "+orderId);
        PaymentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Optional<Movie> opmovie = movieRepo.findById(order.getMovieId());
        if(opmovie.isEmpty()) return null;
        Movie movie = opmovie.get();

        Optional<Show> opshow = showRepo.findById(order.getShowId());
        if(opshow.isEmpty()) return null;
        Show show = opshow.get();

        Optional<Screen> opscreen = screenRepo.findById(show.getSId());
        if(opscreen.isEmpty()) return null;
        Screen screen = opscreen.get();

        Optional<Theatre> optheatre = theatreRepo.findById(screen.getTheatre().getId());
        if(optheatre.isEmpty()) return null;
        Theatre theatre = optheatre.get();


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
                lockedSeatsRequests.setSeatsId(Arrays.asList(order.getSeatIds().split(",")));
                lockedSeatsRequests.setUser(user.getUsername());
                lockedSeatsRequests.setPrice(order.getAmount());
                lockedSeatsRequests.setCgst(order.getCgst());
                lockedSeatsRequests.setSgst(order.getSgst());
                lockedSeatsRequests.setTierName(order.getTierName());
                String bId = generateUniqueBookingId();
                System.out.println("bId is : "+ bId);
                lockedSeatsRequests.setBookingID(bId);
                lockedSeatsRequests.setShowId(order.getShowId());
                bookingService.bookSeats(lockedSeatsRequests);

               Optional<Booking> opbooking = bookingRepo.findByBookingID(bId);
               if(opbooking.isEmpty()) return null;
               Booking booking = opbooking.get();
                System.out.println(" Fetched Booking Id from bookings : "+ booking.getBookingID());

                return new BookingSuccessResponse(true,
                        "Payment made successfully.", movie.getTitle(),
                        movie.getPoster(),booking.getBookingID(), String.valueOf(movie.getCertification()),
                        theatre.getName(),theatre.getAddress(), theatre.getCity(), screen.getSname(),
                        order.getTierName(), order.getSeatIds(), show.getFormat(),show.getStart(),
                        show.getShowDate(), order.getAmount(), order.getCgst(), order.getSgst());
            } else {
                return new BookingSuccessResponse(false, "Payment unsuccessful", null, null, null, null, null, null, null, null, null, null,null,null, null, null, null, null);
            }
        } else {
            throw new RuntimeException("Failed to verify payment with Cashfree");
        }
    }


}
