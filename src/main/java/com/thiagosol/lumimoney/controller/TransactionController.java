package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.transaction.GetMonthTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.NewTransactionDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
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

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

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
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> createTransaction(NewTransactionDTO dto) {
        UserEntity user = securityService.getAuthenticatedUser();
        transactionService.createTransaction(dto, user);
        return RestResponse.status(Response.Status.CREATED);
    }

    @GET
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<List<GetTransactionDTO>> getUserTransactions() {
        UserEntity user = securityService.getAuthenticatedUser();
        return RestResponse.ok(transactionService.getTransactionsByUser(user));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> deleteTransaction(@PathParam("id") Long id) {
        UserEntity user = securityService.getAuthenticatedUser();
        boolean deleted = transactionService.deleteTransaction(id, user);
        if (deleted) {
            return RestResponse.noContent();
        } else {
            return RestResponse.status(Response.Status.NOT_FOUND);
        }
    }

    @PUT
    @Path("/unpay")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> unpayTransactions(List<Long> transactionIds) {
        UserEntity user = securityService.getAuthenticatedUser();
        transactionService.unpayTransactions(transactionIds, user);
        return RestResponse.ok();
    }

    @PUT
    @Path("/pay")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> payTransactions(List<Long> transactionIds) {
        UserEntity user = securityService.getAuthenticatedUser();
        transactionService.payTransactions(transactionIds, user);
        return RestResponse.ok();
    }

    @GET
    @Path("/month/{yearMonth}")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<List<GetMonthTransactionDTO>> getTransactionsByMonth(
            @PathParam("yearMonth") String yearMonth,
            @QueryParam("type") TransactionType type,
            @QueryParam("status") TransactionStatus status) {
        try {
            YearMonth month = YearMonth.parse(yearMonth);
            UserEntity user = securityService.getAuthenticatedUser();
            return RestResponse.ok(transactionService.getTransactionsByMonth(user, month, type, status));
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException();
        }
    }
}

