package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.MonthTransactionsDTO;
import com.thiagosol.lumimoney.dto.transaction.NewTransactionDTO;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.entity.enums.TransactionType;
import com.thiagosol.lumimoney.service.TransactionService;
import com.thiagosol.lumimoney.service.auth.SecurityService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import com.thiagosol.lumimoney.exception.InvalidDateFormatException;
import com.thiagosol.lumimoney.exception.InvalidParameterException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Path("/transactions")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionService transactionService;

    @Inject
    SecurityService securityService;

    @POST
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<Void> createTransaction(NewTransactionDTO dto) {
        UUID userId = securityService.getAuthenticatedUser();
        transactionService.createTransaction(dto, userId);
        return RestResponse.status(Response.Status.CREATED);
    }

    @GET
    @RolesAllowed(SecurityService.ROLE_ADMIN)
    public RestResponse<List<GetTransactionDTO>> getUserTransactions() {
        UUID userId = securityService.getAuthenticatedUser();
        return RestResponse.ok(transactionService.getTransactionsByUser(userId));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<Void> deleteTransaction(@PathParam("id") Long id) {
        UUID userId = securityService.getAuthenticatedUser();
        boolean deleted = transactionService.deleteTransaction(id, userId);
        if (deleted) {
            return RestResponse.noContent();
        } else {
            return RestResponse.status(Response.Status.NOT_FOUND);
        }
    }

    @PUT
    @Path("/unpay")
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<Void> unpayTransactions(List<Long> transactionIds) {
        UUID userId = securityService.getAuthenticatedUser();
        transactionService.unpayTransactions(transactionIds, userId);
        return RestResponse.ok();
    }

    @PUT
    @Path("/pay")
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<Void> payTransactions(List<Long> transactionIds) {
        UUID userId = securityService.getAuthenticatedUser();
        transactionService.payTransactions(transactionIds, userId);
        return RestResponse.ok();
    }

    @GET
    @Path("/month/{yearMonth}")
    @RolesAllowed(SecurityService.ROLE_USER)
    public RestResponse<MonthTransactionsDTO> getTransactionsByMonth(
            @PathParam("yearMonth") String yearMonth,
            @QueryParam("type") TransactionType type,
            @QueryParam("status") TransactionStatus status,
            @QueryParam("paymentMethodType") PaymentMethodType paymentMethodType,
            @QueryParam("id") UUID id) {
        try {
            if ((paymentMethodType != null && id == null) || (id != null && paymentMethodType == null)) {
                throw new InvalidParameterException("Se informado payment method type ou id, ambos devem ser informados");
            }

            YearMonth month = YearMonth.parse(yearMonth);
            UUID userId = securityService.getAuthenticatedUser();
            return RestResponse.ok(transactionService.getTransactionsByMonth(userId, month, type, status, paymentMethodType, id));
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException();
        }
    }
}

