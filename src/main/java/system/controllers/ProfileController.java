package system.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import system.models.User;
import system.services.UserService;

@Controller
public class ProfileController {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String getProfilePage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User loggedInUser, Model model){
        User user = userService.findByUsername(loggedInUser.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String phone,
                                @RequestParam(required = false) String password,
                                Model model){
        User currentUser = userService.getCurrentlyLoggedInUser();

        // update fields with new values
        currentUser.setEmail(email);
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setPhone(phone);

        // if new password is provided, encrypt and update
        if (password != null && !password.isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(password));
        }

        userService.save(currentUser);
        model.addAttribute("user", currentUser);
        return "profile";
    }
}
