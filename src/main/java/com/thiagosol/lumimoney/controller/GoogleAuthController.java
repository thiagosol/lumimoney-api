package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.auth.GoogleLoginDTO;
import com.thiagosol.lumimoney.dto.auth.TokenDTO;
import com.thiagosol.lumimoney.service.auth.GoogleAuthService;
import com.thiagosol.lumimoney.service.auth.JwtService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GoogleAuthController {

    @Inject
    GoogleAuthService googleAuthService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/google")
    @Transactional
    public RestResponse<TokenDTO> login(GoogleLoginDTO login) {
        String token = googleAuthService.login(login);
        return RestResponse.ok(new TokenDTO(token));
    }
}
