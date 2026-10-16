package ro.msg.learning.shop.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ro.msg.learning.shop.model.User;
import ro.msg.learning.shop.model.enums.UserRole;
import ro.msg.learning.shop.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_mapsRoleToRoleAuthority() {
        User user = User.builder().username("admin").password("encoded").role(UserRole.ADMIN).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_whenMissing_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
