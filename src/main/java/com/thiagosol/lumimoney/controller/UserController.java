package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.auth.LoginDTO;
import com.thiagosol.lumimoney.dto.auth.RegisterDTO;
import com.thiagosol.lumimoney.dto.auth.TokenDTO;
import com.thiagosol.lumimoney.service.auth.UserService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterDTO dto) {
        userService.registerUser(dto.email(), dto.password());
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginDTO dto) {
        String token = userService.authenticateUser(dto.email(), dto.password());
        return Response.ok(new TokenDTO(token)).build();
    }
}
