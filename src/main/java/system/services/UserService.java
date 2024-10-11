package system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import system.models.User;
import system.repositories.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username).orElse(null);
    }

    public boolean userExists(String username) {
        return userRepository.findByUsernameIgnoreCase(username).isPresent();
    }

    public User save(User user) {
        if (user.getId() == 0) {
            user.setCreatedAt(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
}
