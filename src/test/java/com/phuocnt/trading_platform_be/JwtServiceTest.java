package com.phuocnt.trading_platform_be;

import com.phuocnt.trading_platform_be.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Generate token → decode lại được đúng email")
    void generateAndDecodeToken() {
        String email = "test@gmail.com";
        String roles = "ROLE_USER";

        String token = jwtService.generateToken(email, roles);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.getEmailFromToken(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Token giả → throw exception")
    void fakeToken_throwsException() {
        assertThatThrownBy(() -> jwtService.getEmailFromToken("token.gia.mao"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("2 user khác nhau → token khác nhau")
    void differentUsers_differentTokens() {
        String token1 = jwtService.generateToken("user1@gmail.com", "ROLE_USER");
        String token2 = jwtService.generateToken("user2@gmail.com", "ROLE_USER");

        assertThat(token1).isNotEqualTo(token2);
    }
}
