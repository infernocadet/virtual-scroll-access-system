package system.controllers;

import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import system.models.User;
import system.services.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping("/register")
    public String getRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String postRegister(@ModelAttribute User user, Model model) {
        if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            model.addAttribute("error", "Username or Password is empty");
            return "register";
        }

        if (!user.getEmail().isEmpty()) {
            Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            if (!emailPattern.matcher(user.getEmail()).matches()) {
                model.addAttribute("error", "Invalid email");
                return "register";
            }
        }

        if (user.getPhone().length() != 10 && !user.getPhone().isEmpty()) {
            model.addAttribute("error", "Phone number must be 10 digits");
            return "register";
        }

        if (userService.userExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        userService.save(user);
        return "redirect:/login";
    }
}
