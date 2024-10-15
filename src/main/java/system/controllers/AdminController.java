package system.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import system.models.Scroll;
import system.models.User;
import system.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import system.services.ScrollService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ScrollService scrollService;

    public AdminController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ScrollService scrollService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.scrollService = scrollService;
    }

    // View all users
    @GetMapping("/admin/users")
    public String viewAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User()); // Add an empty User object for the add form
        return "admin/view_users";
    }

    @GetMapping("/admin/statistics")
    public String viewAllScrolls(Model model) {
        List<Scroll> scrolls = scrollService.findAll();
        model.addAttribute("scrolls", scrolls);
        return "admin/view_scrolls";
    }


    @PostMapping("/admin/users/add")
    public String addUser(@ModelAttribute("newUser") User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty()) {
            return "redirect:/admin/users";
        }

        try {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception e) {
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
        }
        return "redirect:/admin/users";
    }
}

