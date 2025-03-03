package com.thiagosol.lumimoney.service.auth;

import com.thiagosol.lumimoney.dto.auth.GoogleLoginDTO;
import com.thiagosol.lumimoney.dto.auth.GoogleUserDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.exception.InvalidCredentialsException;
import com.thiagosol.lumimoney.exception.InvalidGoogleTokenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class GoogleAuthService {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    @Inject
    JwtService jwtService;

    public String login(GoogleLoginDTO login) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(GOOGLE_TOKEN_INFO_URL).queryParam("access_token",  login.token());
            var googleUser = target.request(MediaType.APPLICATION_JSON).get(GoogleUserDTO.class);

            if (googleUser == null || googleUser.email() == null) {
                throw new InvalidGoogleTokenException();
            }

            UserEntity user = UserEntity.findByEmail(googleUser.email())
            .orElseGet(() -> {
                UserEntity newUser = new UserEntity(googleUser.email());
                newUser.persist();
                return newUser;
            });

            return jwtService.generateToken(user.email, user.role);
        } catch (Exception e) {
            throw new InvalidCredentialsException(e);
        }
    }
}
