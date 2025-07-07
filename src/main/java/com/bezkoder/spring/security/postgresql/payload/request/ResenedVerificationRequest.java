package com.bezkoder.spring.security.postgresql.payload.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResenedVerificationRequest {

    private String email;
}
