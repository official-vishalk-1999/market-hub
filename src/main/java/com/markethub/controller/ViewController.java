package com.markethub.controller;

import com.markethub.entity.*;
import com.markethub.repository.*;
import com.markethub.kafka.OrderProducer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrderProducer orderProducer;

    public ViewController(ProductRepository productRepository,
                          OrderRepository orderRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository,
                          OrderProducer orderProducer) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.orderProducer = orderProducer;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("username") != null;
    }

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }

    private boolean isUser(HttpSession session) {
        return "USER".equals(session.getAttribute("role"));
    }

    private void setNoCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    @GetMapping("/")
    public String home(HttpSession session,
                       HttpServletResponse response) {

        setNoCache(response);

        if (isLoggedIn(session)) {
            if (isAdmin(session)) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/products";
            }
        }

        return "home";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session,
                                 Model model,
                                 HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        model.addAttribute("username", session.getAttribute("username"));
        return "admin-dashboard";
    }

    @GetMapping("/admin/add-product")
    public String addProductPage(HttpSession session,
                                 Model model,
                                 HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        model.addAttribute("username", session.getAttribute("username"));
        return "add-product";
    }

    @PostMapping("/admin/add-product")
    public String saveProduct(@RequestParam String name,
                              @RequestParam double price,
                              @RequestParam int stock,
                              HttpSession session) {

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);

        productRepository.save(product);

        return "redirect:/admin/products?success=added";
    }

    @GetMapping("/admin/products")
    public String viewProducts(Model model,
                               HttpSession session,
                               HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("username", session.getAttribute("username"));

        return "admin-products";
    }

    @GetMapping("/admin/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              Model model,
                              HttpSession session,
                              HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        model.addAttribute("product", productRepository.findById(id).orElse(null));
        model.addAttribute("username", session.getAttribute("username"));

        return "edit-product";
    }

    @PostMapping("/admin/update")
    public String updateProduct(@RequestParam Long id,
                                @RequestParam String name,
                                @RequestParam double price,
                                @RequestParam int stock,
                                HttpSession session) {

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        Product product = productRepository.findById(id).orElse(null);

        if (product != null) {
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            productRepository.save(product);
        }

        return "redirect:/admin/products?success=updated";
    }

    @GetMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                HttpSession session) {

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        productRepository.deleteById(id);

        return "redirect:/admin/products?success=deleted";
    }

    @GetMapping("/admin/notifications")
    public String adminNotifications(Model model,
                                     HttpSession session,
                                     HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        model.addAttribute("notifications", notificationRepository.findAll());
        model.addAttribute("username", session.getAttribute("username"));

        return "admin-notifications";
    }

    @GetMapping("/admin/users")
    public String viewUsers(Model model,
                            HttpSession session,
                            HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isAdmin(session)) return "redirect:/";

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("username", session.getAttribute("username"));

        return "admin-users";
    }

    @GetMapping("/user/products")
    public String userProducts(Model model,
                               HttpSession session,
                               HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isUser(session)) return "redirect:/";

        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("username", session.getAttribute("username"));

        return "user-products";
    }

    @GetMapping("/user/buy/{productId}")
    public String buyProduct(@PathVariable Long productId,
                             HttpSession session) {

        if (!isLoggedIn(session) || !isUser(session)) return "redirect:/";

        String username = (String) session.getAttribute("username");

        Product product = productRepository.findById(productId).orElse(null);

        if (product != null && product.getStock() > 0) {

            product.setStock(product.getStock() - 1);
            productRepository.save(product);

            Order order = new Order();
            order.setProductId(productId);
            order.setQuantity(1);
            order.setStatus("CREATED");
            order.setUsername(username);
            order.setCreatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            String event = savedOrder.getId() + "," +
                    savedOrder.getProductId() + "," +
                    savedOrder.getQuantity();

            orderProducer.sendOrderEvent(event);
        }

        return "redirect:/user/products?success=ordered";
    }

    @GetMapping("/user/orders")
    public String userOrders(Model model,
                             HttpSession session,
                             HttpServletResponse response) {

        setNoCache(response);

        if (!isLoggedIn(session) || !isUser(session)) return "redirect:/";

        String username = (String) session.getAttribute("username");

        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(o -> username.equals(o.getUsername()))
                .filter(o -> o.getStatus().equals("CREATED"))
                .toList();

        Map<Long, Integer> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getProductId,
                        Collectors.summingInt(Order::getQuantity)
                ));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : grouped.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);

            if (product != null) {
                Map<String, Object> row = new HashMap<>();
                row.put("productName", product.getName());
                row.put("quantity", entry.getValue());
                row.put("productId", product.getId());
                result.add(row);
            }
        }

        model.addAttribute("orders", result);
        model.addAttribute("username", session.getAttribute("username"));

        return "user-orders";
    }

    @GetMapping("/user/cancel/{productId}")
    public String cancelOrder(@PathVariable Long productId,
                              HttpSession session) {

        if (!isLoggedIn(session) || !isUser(session)) return "redirect:/";

        String username = (String) session.getAttribute("username");

        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(o -> username.equals(o.getUsername()))
                .filter(o -> o.getProductId().equals(productId))
                .filter(o -> o.getStatus().equals("CREATED"))
                .toList();

        int totalQty = orders.stream().mapToInt(Order::getQuantity).sum();

        for (Order o : orders) {
            o.setStatus("CANCELLED");
            orderRepository.save(o);
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setStock(product.getStock() + totalQty);
            productRepository.save(product);
        }

        return "redirect:/user/orders?success=cancelled";
    }
}