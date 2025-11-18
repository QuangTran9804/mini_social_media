package com.example.social.dto.response.dev;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResSeedSummaryDTO {
    private int usersCreated;
    private int requestsCreated;
    private int requestsAccepted;
    private int friendshipsCreatedRows; // two rows per friendship
}


