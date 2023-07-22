package br.com.thsoft.auth.domain.user;

import java.util.Set;

public record RegisterDTO(String username, String password, Set<UserRole> roles) {
}
