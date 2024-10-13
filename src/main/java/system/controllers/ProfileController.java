package system.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import system.models.User;
import system.services.UserService;

@Controller
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String getProfilePage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User loggedInUser, Model model){
        User user = userService.findByUsername(loggedInUser.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }
}
