package com.sdm.auth.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthRequest extends TokenInfo implements Serializable {
    @NotBlank(message = "Token can't be blank.")
    @Size(min = 6, max = 255)
    private String accessToken;

    @NotBlank(message = "ClientId can't be blank.")
    @Size(min = 6, max = 255)
    private String clientId;
}
