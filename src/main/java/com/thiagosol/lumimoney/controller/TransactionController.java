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
    public Response createTransaction(NewTransactionDTO dto, @HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        transactionService.createTransaction(dto, user);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @RolesAllowed({"USER", "MASTER"})
    public List<GetTransactionDTO> getUserTransactions(@HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        return transactionService.getTransactionsByUser(user);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"USER", "MASTER"})
    public Response deleteTransaction(@PathParam("id") Long id, @HeaderParam("Authorization") String token) {
        UserEntity user = userService.getUserFromToken(token);
        boolean deleted = transactionService.deleteTransaction(id, user);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}

