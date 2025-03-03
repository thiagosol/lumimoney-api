package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.service.CreditCardInvoiceService;
import com.thiagosol.lumimoney.service.auth.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.UUID;

@Path("/credit-card-invoices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CreditCardInvoiceController {

    @Inject
    CreditCardInvoiceService creditCardInvoiceService;

    @Inject
    UserService userService;

    @GET
    @Path("/{creditCardId}")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<List<GetCreditCardInvoiceDTO>> getUserCreditCardInvoicesByCreditCard(
            @HeaderParam("Authorization") String token,
            @PathParam("creditCardId") UUID creditCardId) {
        UserEntity user = userService.getUserFromToken(token);
        return RestResponse.ok(creditCardInvoiceService.getPaymentMethodsByUserAndCreditCard(user, creditCardId));
    }
}

