package com.university.admission.security;

import com.university.admission.entity.User;
import com.university.admission.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Duoc Spring Security tu dong goi moi khi co yeu cau dang nhap.
 * Nhiem vu: tim User trong CSDL theo username, boc thanh CustomUserDetails.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Khong tim thay tai khoan voi username: " + username));
        return new CustomUserDetails(user);
    }
}
