package ru.wartesd.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.wartesd.entity.User;
import ru.wartesd.entity.UserRole;
import ru.wartesd.service.UserService;

import java.util.Collections;
import java.util.Set;

@Controller
public class PublicAuthorizationController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Autowired
    public PublicAuthorizationController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registration")
    public String getAuthorizationPage(){
        return "public/authorization/registration-page";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model, @RequestParam(required = false) String error){
        if (error!=null){
            model.addAttribute("isAuthenticationFailed",true);
        }
        return "public/authorization/login-page";
    }

    @PostMapping("/registration")
    public String createUserAccount(@RequestParam(name = "name") String name,
                                    @RequestParam(name = "email") String email,
                                    @RequestParam(name = "password") String password,
                                    HttpServletRequest request,
                                    HttpServletResponse response){
        String encodedPassword = passwordEncoder.encode(password);
        userService.save(new User(name,email,encodedPassword, UserRole.USER));
        forceAutoLogin(email,password, request, response);
        return "redirect:/account";
    }

    private void forceAutoLogin(String email, String password, HttpServletRequest request, HttpServletResponse response){
        Set<SimpleGrantedAuthority> roles = Collections.singleton(UserRole.USER.toAuthority());
        Authentication authentication = new UsernamePasswordAuthenticationToken(email,password,roles);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
