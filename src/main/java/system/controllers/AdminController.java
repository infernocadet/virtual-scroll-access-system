package system.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import system.models.User;
import system.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // View all users
    @GetMapping("/admin/users")
    public String viewAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User()); // Add an empty User object for the add form
        return "admin/view_users";
    }

    // Add a new user
    @PostMapping("/admin/users/add")
    public String addUser(@ModelAttribute("newUser") User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    // Delete an existing user by ID
    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }
}
