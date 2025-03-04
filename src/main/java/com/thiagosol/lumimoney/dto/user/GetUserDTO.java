package com.thiagosol.lumimoney.dto.user;

import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.entity.enums.Role;

public record GetUserDTO(Long id,
                        String email,
                        Role role) {

    public GetUserDTO(UserEntity userEntity) {
        this(userEntity.id, userEntity.email, userEntity.role);
    }
}
