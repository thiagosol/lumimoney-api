package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.auth.GoogleUserDTO;
import com.thiagosol.lumimoney.dto.auth.TokenDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.service.auth.GoogleAuthService;
import com.thiagosol.lumimoney.service.auth.JwtService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    public Response authenticateWithGoogle(@QueryParam("token") String token) {
        GoogleUserDTO googleUser = googleAuthService.verifyToken(token);

        if (googleUser == null || googleUser.email() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();
        }
        UserEntity user = UserEntity.findByEmail(googleUser.email())
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity(googleUser.email());
                    newUser.persist();
                    return newUser;
                });

        String jwt = jwtService.generateToken(user.email, user.role);
        return Response.ok(new TokenDTO(jwt)).build();
    }
}
