package com.sdm.auth.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfo implements Serializable {
    @NotBlank(message = "Device UniqueID is required.")
    @Size(max = 255)
    private String deviceId;

    @NotBlank(message = "Device OS (ios, android, windows, browser) is required.")
    @Size(max = 50)
    private String deviceOS;

    @Size(max = 500)
    private String firebaseToken;
}
