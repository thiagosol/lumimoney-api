package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.dto.paymentmethod.NewPaymentMethodDTO;
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
import java.util.UUID;

@Path("/payment-methods")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentMethodController {

    @Inject
    PaymentMethodService paymentMethodService;

    @Inject
    SecurityService securityService;

    @POST
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<Void> createPaymentMethod(NewPaymentMethodDTO dto) {
        UUID userId = securityService.getAuthenticatedUser();
        paymentMethodService.createPaymentMethod(dto, userId);
        return RestResponse.status(Response.Status.CREATED);
    }

    @GET
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<List<GetPaymentMethodDTO>> getUserPaymentMethods() {
        UUID userId = securityService.getAuthenticatedUser();
        return RestResponse.ok(paymentMethodService.getPaymentMethodsByUser(userId));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<Void> deletePaymentMethod(@PathParam("id") Long id) {
        UUID userId = securityService.getAuthenticatedUser();
        boolean deleted = paymentMethodService.deletePaymentMethod(id, userId);
        if (deleted) {
            return RestResponse.noContent();
        } else {
            return RestResponse.status(Response.Status.NOT_FOUND);
        }
    }
}

