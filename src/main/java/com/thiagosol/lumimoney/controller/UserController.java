package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.auth.LoginDTO;
import com.thiagosol.lumimoney.dto.auth.RegisterDTO;
import com.thiagosol.lumimoney.dto.auth.TokenDTO;
import com.thiagosol.lumimoney.dto.user.GetUserDTO;
import com.thiagosol.lumimoney.service.auth.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @POST
    @Path("/register")
    @Transactional
    public RestResponse<TokenDTO> register(RegisterDTO dto) {
        userService.registerUser(dto.email(), dto.password());
        String token = userService.authenticateUser(dto.email(), dto.password());
        return RestResponse.ok(new TokenDTO(token));
    }

    @POST
    @Path("/login")
    public RestResponse<TokenDTO> login(LoginDTO dto) {
        String token = userService.authenticateUser(dto.email(), dto.password());
        return RestResponse.ok(new TokenDTO(token));
    }

    @GET
    @Path("/me")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<GetUserDTO> getCurrentUser(@HeaderParam("Authorization") String token) {
        var user = userService.getUserFromToken(token);
        return RestResponse.ok(new GetUserDTO(user));
    }
}
