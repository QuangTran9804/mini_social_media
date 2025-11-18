package com.example.social.dto.request.friend;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqSendFriendRequest {

    @NotNull(message = "Receiver id is required")
    private Long receiverId;
}

