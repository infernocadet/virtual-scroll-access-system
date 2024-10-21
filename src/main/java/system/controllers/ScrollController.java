package system.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import system.models.Scroll;
import system.models.User;
import system.services.ScrollService;
import system.services.UserService;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ScrollController {

    private final UserService userService;
    private final ScrollService scrollService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


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
        scroll.setContentType(scroll.getContentFile().getContentType());

        scrollService.save(scroll);
        return "redirect:/";
    }

    @GetMapping("/scroll/{id}/download")
    @ResponseBody
    public ResponseEntity<byte[]> getDownloadScroll(@PathVariable int id) {
        Optional<Scroll> optionalScroll = scrollService.findById(id);
        if (optionalScroll.isPresent()) {
            Scroll scroll = optionalScroll.get();

            scroll.setDownloads(scroll.getDownloads() + 1);
            scrollService.save(scroll);

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(scroll.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + scroll.getFileName() + "\"")
                    .body(scroll.getContent());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/scroll/{id}/edit")
    public String getEditScroll(@PathVariable int id, Model model, Principal principal) {
        Optional<Scroll> optionalScroll = scrollService.findById(id);
        if (optionalScroll.isPresent()) {
            Scroll scroll = optionalScroll.get();
            if (scroll.getUser().getId() != userService.findByUsername(principal.getName()).getId()) {
                return "redirect:/";
            }
            model.addAttribute("scroll", scroll);
            return "scroll_edit";
        }
        return "redirect:/";
    }

    @PostMapping("/scroll/{id}/edit")
    public String postEditScroll(@PathVariable int id, @ModelAttribute Scroll scroll, Model model, Principal principal) throws IOException {
        Optional<Scroll> optionalScroll = scrollService.findById(id);
        if (optionalScroll.isPresent()) {
            Scroll oldScroll = optionalScroll.get();
            if (oldScroll.getUser().getId() != userService.findByUsername(principal.getName()).getId()) {
                return "redirect:/";
            }

            if (scroll.getName().isEmpty()) {
                model.addAttribute("error", "Name is empty");
                model.addAttribute("scroll", oldScroll);
                return "scroll_edit";
            } else {
                if (scrollService.nameExists(scroll.getName()) && !scroll.getName().equals(oldScroll.getName())) {
                    model.addAttribute("error", "Name already exists");
                    model.addAttribute("scroll", oldScroll);
                    return "scroll_edit";
                }
            }

            oldScroll.setName(scroll.getName());

            if (!scroll.getContentFile().isEmpty()) {
                oldScroll.setFileName(scroll.getContentFile().getOriginalFilename());
                oldScroll.setContent(scroll.getContentFile().getBytes());
                oldScroll.setContentType(scroll.getContentFile().getContentType());
            }

            scrollService.save(oldScroll);
        }
        return "redirect:/";
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

    @GetMapping("/scroll/search")
    public String searchScroll(@RequestParam(value = "uploaderId", required = false) Integer uploaderId,
                               @RequestParam(value = "scrollId", required = false) Integer scrollId,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "startDate", required = false) String startDate,
                               @RequestParam(value = "endDate", required = false) String endDate,
                               Model model) {

        LocalDateTime start = startDate != null && !startDate.isEmpty() ? LocalDateTime.parse(startDate, formatter) : null;
        LocalDateTime end = endDate != null && !endDate.isEmpty() ? LocalDateTime.parse(endDate, formatter) : null;

        List<Scroll> scrolls = scrollService.searchScrolls(uploaderId, scrollId, name, start, end);
        if (scrolls.isEmpty()){
            return "redirect:/";
        }
        model.addAttribute("scrolls", scrolls);

        return "index";  // Or wherever the scroll list is displayed
    }
}
