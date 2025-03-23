package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.service.auth.SecurityService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
    SecurityService securityService;

    /*@GET
    @Path("/me")
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<GetUserDTO> getCurrentUser() {
        var user = securityService.getAuthenticatedUser();
        return RestResponse.ok(new GetUserDTO(user));
    }*/
}
