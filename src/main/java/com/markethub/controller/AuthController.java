package com.markethub.controller;

import com.markethub.entity.User;
import com.markethub.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpSession session) {

        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null &&
                user.getPassword().equals(password) &&
                user.getRole().equals(role)) {

            session.setAttribute("username", username);
            session.setAttribute("role", role);

            if (role.equals("ADMIN")) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/products";
            }
        }

        return "redirect:/?error=true";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/auth/register?error=true";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");

        userRepository.save(user);

        return "redirect:/?registered=true";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         HttpSession session) {

        session.invalidate();

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        return "redirect:/";
    }
}