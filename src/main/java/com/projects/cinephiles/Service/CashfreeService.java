package com.projects.cinephiles.Service;

import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CashfreeService {

    @Value("${cashfree.client-id}")
    private String clientId;

    @Value("${cashfree.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    @Autowired
    private UserService userService;

    public CashfreeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createOrderSession( double order_amount,User user) {
        String orderId = UUID.randomUUID().toString().replace("-", "");
        System.out.println("before url : " + orderId + order_amount + " | "+ user);
        String url = "https://sandbox.cashfree.com/pg/orders"; // Cashfree Sandbox URL

        // Fetch the user from userService using the customerEmail (which should be the username)

        // Prepare request data as a Map
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("order_id", orderId);
        requestData.put("order_amount", order_amount);
        requestData.put("order_currency", "INR");
        String phone = user.getPhone()==null ? "9999999999": user.getPhone();
        // Prepare customer details
        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("customer_id", String.valueOf(user.getId())); // Set customer_id to userId
        customerDetails.put("customer_phone", phone); // Set customer_phone from user object
        customerDetails.put("customer_email", user.getUsername()); // Set customer_email from user object
        requestData.put("customer_details", customerDetails);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("x-api-version", "2023-08-01");
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);

        // Wrap the request data and headers in an HttpEntity object
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
        System.out.println("After HttpEntity : " + entity);

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println("Response is : " + response);

        // Return the response body (you can further process this to extract session ID or handle errors)
        return response.getBody();
    }
}
