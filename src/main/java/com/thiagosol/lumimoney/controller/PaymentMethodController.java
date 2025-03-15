package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.dto.paymentmethod.NewPaymentMethodDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.service.PaymentMethodService;
import com.thiagosol.lumimoney.service.auth.SecurityService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/payment-methods")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentMethodController {

    @Inject
    PaymentMethodService paymentMethodService;

    @Inject
    SecurityService securityService;

    @POST
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> createPaymentMethod(NewPaymentMethodDTO dto) {
        UserEntity user = securityService.getAuthenticatedUser();
        paymentMethodService.createPaymentMethod(dto, user);
        return RestResponse.status(Response.Status.CREATED);
    }

    @GET
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<List<GetPaymentMethodDTO>> getUserPaymentMethods() {
        UserEntity user = securityService.getAuthenticatedUser();
        return RestResponse.ok(paymentMethodService.getPaymentMethodsByUser(user));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> deletePaymentMethod(@PathParam("id") Long id) {
        UserEntity user = securityService.getAuthenticatedUser();
        boolean deleted = paymentMethodService.deletePaymentMethod(id, user);
        if (deleted) {
            return RestResponse.noContent();
        } else {
            return RestResponse.status(Response.Status.NOT_FOUND);
        }
    }
}

