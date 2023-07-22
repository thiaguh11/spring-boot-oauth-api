package br.com.thsoft.auth.controllers;

import br.com.thsoft.auth.domain.user.*;
import br.com.thsoft.auth.repositories.UserRepository;
import br.com.thsoft.auth.infra.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository repository;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data){
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);

            var token = tokenService.generateToken((User) auth.getPrincipal());

            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid Optional<RegisterDTO> data){
        try {
            if(data.isPresent()) {
                Optional<UserDetails> user = this.repository.findByUsername(data.get().username());
                if(!user.isEmpty()) return ResponseEntity.badRequest().body("Usuário já existe!");

                String encryptedPassword = new BCryptPasswordEncoder().encode(data.get().password());

                User newUser = new User(data.get().username(), encryptedPassword, data.get().roles());

                this.repository.save(newUser);

                return ResponseEntity.ok().body("Usuário cadastrado com sucesso!");
            } else {
                return ResponseEntity.badRequest().body("Dados inválidos!");
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Dados inválidos! Por favor, verifique se os dados foram inseridos corretamente!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
