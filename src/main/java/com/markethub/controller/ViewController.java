package com.markethub.controller;

import com.markethub.repository.NotificationRepository;
import com.markethub.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final ProductRepository products;
    private final NotificationRepository notifications;

    public ViewController(ProductRepository products, NotificationRepository notifications) {
        this.products = products;
        this.notifications = notifications;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", products.findAll());
        model.addAttribute("notifications", notifications.findAllByOrderByIdDesc());
        return "index";
    }
}
