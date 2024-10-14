package system.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import system.models.User;
import system.repositories.UserRepository;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping("/admin/users")
    public String viewAllUsers(Model model) {

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/view_users";
    }
}
