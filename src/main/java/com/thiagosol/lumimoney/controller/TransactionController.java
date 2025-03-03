package com.thiagosol.lumimoney.controller;

import com.thiagosol.lumimoney.dto.transaction.GetTransactionDTO;
import com.thiagosol.lumimoney.dto.transaction.NewTransactionDTO;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.service.TransactionService;
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
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionService transactionService;

    @Inject
    UserService userService;

    @POST
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> createTransaction(NewTransactionDTO dto, @HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        transactionService.createTransaction(dto, user);
        return RestResponse.status(Response.Status.CREATED);
    }

    @GET
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<List<GetTransactionDTO>> getUserTransactions(@HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        return RestResponse.ok(transactionService.getTransactionsByUser(user));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"USER", "MASTER"})
    public RestResponse<Void> deleteTransaction(@PathParam("id") Long id, @HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        boolean deleted = transactionService.deleteTransaction(id, user);
        if (deleted) {
            return RestResponse.noContent();
        } else {
            return RestResponse.status(Response.Status.NOT_FOUND);
        }
    }
}

