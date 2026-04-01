package dev.ssafy.bought_snack.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoughtSnackStatusUpdateRequest {

    @NotBlank(message = "상태값은 필수입니다.")
    private String status;

}
