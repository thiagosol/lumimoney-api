package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.service.CreditCardInvoiceService;
import com.thiagosol.lumimoney.service.auth.SecurityService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
    SecurityService securityService;

    @GET
    @Path("/payment-method/{paymentMethodId}")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<List<GetCreditCardInvoiceDTO>> getInvoicesByPaymentMethod(
            @PathParam("paymentMethodId") UUID paymentMethodId,
            @QueryParam("isClosed") Boolean isClosed) {
        return RestResponse.ok(creditCardInvoiceService.getInvoicesByPaymentMethod(paymentMethodId, isClosed));
    }

    @PUT
    @Path("/{invoiceId}/pay")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> payInvoice(@PathParam("invoiceId") UUID invoiceId) {
        UserEntity user = securityService.getAuthenticatedUser();
        creditCardInvoiceService.payInvoice(invoiceId, user);
        return RestResponse.ok();
    }

    @PUT
    @Path("/{invoiceId}/unpay")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> unpayInvoice(@PathParam("invoiceId") UUID invoiceId) {
        UserEntity user = securityService.getAuthenticatedUser();
        creditCardInvoiceService.unpayInvoice(invoiceId, user);
        return RestResponse.ok();
    }
}

