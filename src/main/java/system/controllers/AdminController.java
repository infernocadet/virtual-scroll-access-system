package system.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import system.models.Scroll;
import system.models.User;
import system.repositories.ScrollRepository;
import system.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import system.services.ScrollService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ScrollService scrollService;
    private ScrollRepository scrollRepository;

    public AdminController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ScrollService scrollService, ScrollRepository scrollRepository ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.scrollService = scrollService;
        this.scrollRepository=  scrollRepository;
    }

    @GetMapping("/admin/users")
    public String viewAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        Map<User, Integer> userScrollCounts = new HashMap<>();
        for (User user : users) {
            int scrollCount = (user.getScrolls() != null) ? user.getScrolls().size() : 0;
            userScrollCounts.put(user, scrollCount);
        }
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User()); // Add an empty User object for the add form
        model.addAttribute("userScrollCounts", userScrollCounts);
        return "admin/view_users";
    }

    @GetMapping("/admin/statistics")
    public String viewAllScrolls(Model model, @RequestParam(value = "sort", required = false) String sort) {
        List<Scroll> scrolls;

        if ("asc".equals(sort)) {
            scrolls = scrollRepository.findAllByOrderByDownloadsAsc();
        } else if ("desc".equals(sort)) {
            scrolls = scrollRepository.findAllByOrderByDownloadsDesc();
        } else {
            scrolls = scrollRepository.findAll(); // Default to no sorting
        }

        model.addAttribute("scrolls", scrolls);
        return "admin/view_scrolls";
    }

    @PostMapping("/admin/scrolls/increase/{id}")
    public String increaseDownloads(@PathVariable int id) {
        Scroll scroll = scrollRepository.findById(id).orElseThrow();
        scroll.setDownloads(scroll.getDownloads() + 1);
        scrollRepository.save(scroll);
        return "redirect:/admin/statistics?sort=asc";
    }

    @PostMapping("/admin/scrolls/decrease/{id}")
    public String decreaseDownloads(@PathVariable int id) {
        Scroll scroll = scrollRepository.findById(id).orElseThrow();
        if (scroll.getDownloads() > 0) {
            scroll.setDownloads(scroll.getDownloads() - 1);
        }
        scrollRepository.save(scroll);
        return "redirect:/admin/statistics?sort=asc";
    }


    @PostMapping("/admin/users/add")
    public String addUser(@ModelAttribute("newUser") User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getUsername().length() > 255 || !user.getUsername().matches("^[a-zA-Z0-9 ]+$")) {
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
    @GetMapping("/admin/users/search")
    public String searchUsers(@RequestParam("username") String username, Model model) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);
        Map<User, Integer> userScrollCounts = new HashMap<>();
        for (User user : users) {
            int scrollCount = (user.getScrolls() != null) ? user.getScrolls().size() : 0;
            userScrollCounts.put(user, scrollCount);
        }
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User());
        model.addAttribute("userScrollCounts", userScrollCounts);
        return "admin/view_users";
    }
    @PostMapping("/admin/users/makeAdmin/{id}")
    public String makeAdmin(@PathVariable int id) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
            user.setAdmin(true);
            userRepository.save(user);
        } catch (Exception e) {

        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/demoteAdmin/{id}")
    public String removeAdmin(@PathVariable int id) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
            user.setAdmin(false);
            userRepository.save(user);
        } catch (Exception e) {

        }
        return "redirect:/admin/users";
    }




}


