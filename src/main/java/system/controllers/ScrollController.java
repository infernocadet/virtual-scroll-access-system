package system.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import system.models.Scroll;
import system.models.User;
import system.services.ScrollService;
import system.services.UserService;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ScrollController {

    private final UserService userService;
    private final ScrollService scrollService;

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("scrolls", scrollService.findAll());
        return "index";
    }

    @GetMapping("/scroll/create")
    public String getCreateScroll(Model model) {
        model.addAttribute("scroll", new Scroll());
        return "scroll_create";
    }

    @PostMapping("/scroll/create")
    @PreAuthorize("isAuthenticated()")
    public String postCreateScroll(@ModelAttribute Scroll scroll, Model model, Principal principal) throws IOException {
        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        scroll.setUser(user);

        if (scroll.getName().isEmpty()) {
            model.addAttribute("error", "Name is empty");
            return "scroll_create";
        }

        if (scroll.getContentFile().isEmpty()) {
            model.addAttribute("error", "File is empty");
            return "scroll_create";
        }

        scrollService.save(scroll, scroll.getContentFile());
        return "redirect:/";
    }
}
