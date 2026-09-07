package ru.wartesd.controller.secured;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.wartesd.entity.User;
import ru.wartesd.entity.UserRole;
import ru.wartesd.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class PrivateAdminController {
    private final UserService userService;

    public PrivateAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getManagementPage(Model model){
        User user = userService.getCurrentUser();
        model.addAttribute("userName",user.getName());
        if (user.isSuperAdmin()){
            List<User> candidatesToDelete = userService.findAllByRoleIn(Arrays.asList(UserRole.USER,UserRole.ADMIN));
            List<User> candidatesToUpgrade = candidatesToDelete.stream()
                    .filter(User::isSimpleUser)
                    .toList();
            model.addAttribute("candidatesToDelete",candidatesToDelete);
            model.addAttribute("candidatesToUpgrade",candidatesToUpgrade);
        }else {
            List<User> candidatesToDelete = userService.findAllByRoleIn(Collections.singleton(UserRole.USER));
            model.addAttribute("candidatesToDelete",candidatesToDelete);
        }
        return "private/admin/management-page";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam int id) {
        Optional<User> userToBeDeletedOptional = userService.findById(id);
        if (userToBeDeletedOptional.isEmpty()){
            return "redirect:/admin";
        }

        User userToBeDeleted = userToBeDeletedOptional.get();
        User currentUser = userService.getCurrentUser();

        if (userToBeDeleted.isSuperAdmin()) return "redirect:/admin";
        if (userToBeDeleted.isAdmin() && !currentUser.isSuperAdmin()) return "redirect:/admin";

        userService.deleteById(id);
        return "redirect:/admin";
    }
}
