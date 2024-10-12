package system.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import system.models.Scroll;
import system.models.User;
import system.services.ScrollService;
import system.services.UserService;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

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

        if (scrollService.nameExists(scroll.getName())) {
            model.addAttribute("error", "Name already exists");
            return "scroll_create";
        }

        if (scroll.getContentFile().isEmpty()) {
            model.addAttribute("error", "File is empty");
            return "scroll_create";
        }

        scroll.setContent(scroll.getContentFile().getBytes());
        scroll.setFileName(scroll.getContentFile().getOriginalFilename());

        scrollService.save(scroll);
        return "redirect:/";
    }

    @GetMapping("/scroll/{id}/download")
    public void getDownloadScroll(@PathVariable int id, HttpServletResponse response) throws IOException {
        Optional<Scroll> optionalScroll = scrollService.findById(id);
        if (optionalScroll.isPresent()) {
            Scroll scroll = optionalScroll.get();

            scroll.setDownloads(scroll.getDownloads() + 1);
            scrollService.save(scroll);

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + scroll.getFileName() + "\"");

            response.getOutputStream().write(scroll.getContent());
            response.getOutputStream().flush();
        }
    }

    @GetMapping("/scroll/{id}/delete")
    public String getDeleteScroll(@PathVariable int id, Principal principal) {
        Optional<Scroll> optionalScroll = scrollService.findById(id);
        if (optionalScroll.isPresent()) {
            Scroll scroll = optionalScroll.get();
            if (scroll.getUser().getId() != userService.findByUsername(principal.getName()).getId()) {
                return "redirect:/";
            }
            scrollService.delete(scroll);
        }
        return "redirect:/";
    }
}
