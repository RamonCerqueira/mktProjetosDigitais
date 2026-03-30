package com.mktplace.config;

import com.mktplace.enums.DocumentType;
import com.mktplace.enums.Role;
import com.mktplace.model.User;
import com.mktplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

@Configuration
public class AdminBootstrapConfig {
    @Bean
    public CommandLineRunner adminBootstrapRunner(UserRepository userRepository,
                                                  PasswordEncoder passwordEncoder,
                                                  @Value("${app.admin.bootstrap-enabled:true}") boolean bootstrapEnabled,
                                                  @Value("${app.admin.name:Admin Master}") String adminName,
                                                  @Value("${app.admin.email:admin@marketplace.local}") String adminEmail,
                                                  @Value("${app.admin.password:Admin123!}") String adminPassword,
                                                  @Value("${app.admin.document-number:11144477735}") String adminDocumentNumber) {
        return args -> {
            if (!bootstrapEnabled) return;
            userRepository.findByEmail(adminEmail).orElseGet(() -> userRepository.save(User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .roles(Set.of(Role.ADMIN))
                    .documentType(DocumentType.CPF)
                    .documentNumber(adminDocumentNumber)
                    .createdAt(Instant.now())
                    .active(true)
                    .blocked(false)
                    .city("São Paulo")
                    .state("SP")
                    .build()));
        };
    }
}
