package com.projects.cinephiles.JwtConfig;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String username;
    private String jwtToken;


}

