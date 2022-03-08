package com.sdm.auth.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String firebaseMessagingToken;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenInfo)) return false;

        TokenInfo tokenInfo = (TokenInfo) o;

        if (getDeviceId() != null ? !getDeviceId().equals(tokenInfo.getDeviceId()) : tokenInfo.getDeviceId() != null)
            return false;
        if (getDeviceOS() != null ? !getDeviceOS().equals(tokenInfo.getDeviceOS()) : tokenInfo.getDeviceOS() != null)
            return false;
        return getFirebaseMessagingToken() != null ? getFirebaseMessagingToken().equals(tokenInfo.getFirebaseMessagingToken()) : tokenInfo.getFirebaseMessagingToken() == null;
    }

    @Override
    public int hashCode() {
        int result = getDeviceId() != null ? getDeviceId().hashCode() : 0;
        result = 31 * result + (getDeviceOS() != null ? getDeviceOS().hashCode() : 0);
        result = 31 * result + (getFirebaseMessagingToken() != null ? getFirebaseMessagingToken().hashCode() : 0);
        return result;
    }
}
