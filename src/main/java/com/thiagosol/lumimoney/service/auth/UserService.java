package com.thiagosol.lumimoney.service.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.exception.EmailAlreadyRegisteredException;
import com.thiagosol.lumimoney.exception.InvalidCredentialsException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    JwtService jwtService;

    @Inject
    JWTParser jwtParser;

    @Transactional
    public UserEntity registerUser(String email, String password) {
        if (UserEntity.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        UserEntity user = new UserEntity(email, password);
        user.persist();

        return user;
    }

    public String authenticateUser(String email, String password) {
        Optional<UserEntity> userOpt = UserEntity.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        UserEntity user = userOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash);
        if (!result.verified) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user.email, user.role);
    }

    public UserEntity getUserFromToken(String token) {
        try {
            String email = jwtParser.parse(token.replace("Bearer ", "")).getClaim("sub");
            return UserEntity.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        } catch (ParseException e) {
            throw new InvalidCredentialsException();
        }
    }
}
