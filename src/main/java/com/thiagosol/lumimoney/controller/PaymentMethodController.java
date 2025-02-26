package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.dto.paymentmethod.NewPaymentMethodDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.service.PaymentMethodService;
import com.thiagosol.lumimoney.service.auth.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/payment-methods")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentMethodController {

    @Inject
    PaymentMethodService paymentMethodService;

    @Inject
    UserService userService;

    @POST
    @RolesAllowed({"USER", "MASTER"})
    public Response createPaymentMethod(NewPaymentMethodDTO dto, @HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        paymentMethodService.createPaymentMethod(dto, user);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @RolesAllowed({"USER", "MASTER"})
    public List<GetPaymentMethodDTO> getUserPaymentMethods(@HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        return paymentMethodService.getPaymentMethodsByUser(user);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"USER", "MASTER"})
    public Response deletePaymentMethod(@PathParam("id") Long id, @HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        boolean deleted = paymentMethodService.deletePaymentMethod(id, user);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}

