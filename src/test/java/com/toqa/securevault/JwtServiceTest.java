package com.toqa.securevault;

import com.toqa.securevault.config.JwtProperties;
import com.toqa.securevault.security.JwtService;
import com.toqa.securevault.user.entity.Role;
import com.toqa.securevault.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtProperties properties = new JwtProperties(
            "ZGV2LW9ubHktc2VjcmV0LWtleS1kby1ub3QtdXNlLWluLXByb2R1Y3Rpb24tMTIzNA==",
            900, 1209600
    );
    private final JwtService jwtService = new JwtService(properties);

    @Test
    void generatesTokenThatValidatesForTheSameUser() {
        User user = User.builder().username("toqa").email("toqa@example.com")
                .passwordHash("irrelevant").role(Role.ROLE_USER).build();

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("toqa");
        assertThat(jwtService.isValid(token, user)).isTrue();
    }

    @Test
    void tokenDoesNotValidateForADifferentUser() {
        User owner = User.builder().username("toqa").passwordHash("x").role(Role.ROLE_USER).build();
        User someoneElse = User.builder().username("attacker").passwordHash("y").role(Role.ROLE_USER).build();

        String token = jwtService.generateAccessToken(owner);

        assertThat(jwtService.isValid(token, someoneElse)).isFalse();
    }
}
