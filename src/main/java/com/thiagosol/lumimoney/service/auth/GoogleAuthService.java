package com.thiagosol.lumimoney.service.auth;

import com.thiagosol.lumimoney.dto.auth.GoogleUserDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class GoogleAuthService {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    public GoogleUserDTO verifyToken(String token) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(GOOGLE_TOKEN_INFO_URL + token);
            return target.request(MediaType.APPLICATION_JSON)
                    .get(GoogleUserDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
