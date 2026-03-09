package com.projects.cinephiles.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
public class RestPageImpl<T> extends PageImpl<T> {

    // 1. This constructor is used by Jackson when fetching from Redis
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPageImpl(@JsonProperty("content") List<T> content,
                        @JsonProperty("number") int number,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") Long totalElements) {

        // Reconstruct the PageImpl using PageRequest (defaults to size 1 to avoid / 0 errors)
        super(content, PageRequest.of(number, size == 0 ? 1 : size), totalElements == null ? 0 : totalElements);
    }

    // 2. This constructor is used by your Service to wrap the database result
    public RestPageImpl(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // 3. Fallback default constructor
    public RestPageImpl() {
        super(new ArrayList<>());
    }
}