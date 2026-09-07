package ru.wartesd.controller.secured;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.wartesd.entity.User;
import ru.wartesd.entity.UserRole;
import ru.wartesd.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PrivateAdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PrivateAdminController privateAdminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(privateAdminController).build();
    }

    @Test
    @DisplayName("GET /admin - Супер-администратор видит кандидатов с ролями USER и ADMIN")
    void getManagementPage_WhenSuperAdmin_ShouldShowUsersAndAdmins() throws Exception {
        User superAdmin = new User("SuperAdmin", "super@test.com", "pass", UserRole.SUPER_ADMIN);
        User regularUser = new User("User1", "user1@test.com", "pass", UserRole.USER);
        User adminUser = new User("Admin1", "admin1@test.com", "pass", UserRole.ADMIN);
        List<User> candidates = Arrays.asList(regularUser, adminUser);

        when(userService.getCurrentUser()).thenReturn(superAdmin);
        when(userService.findAllByRoleIn(Arrays.asList(UserRole.USER, UserRole.ADMIN))).thenReturn(candidates);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("private/admin/management-page"))
                .andExpect(model().attribute("userName", "SuperAdmin"))
                .andExpect(model().attribute("candidatesToDelete", candidates));

        verify(userService, times(1)).findAllByRoleIn(Arrays.asList(UserRole.USER, UserRole.ADMIN));
    }

    @Test
    @DisplayName("GET /admin - Обычный администратор видит только кандидатов с ролью USER")
    void getManagementPage_WhenRegularAdmin_ShouldShowOnlyUsers() throws Exception {
        User admin = new User("Admin", "admin@test.com", "pass", UserRole.ADMIN);
        User regularUser = new User("User1", "user1@test.com", "pass", UserRole.USER);
        List<User> candidates = Collections.singletonList(regularUser);

        when(userService.getCurrentUser()).thenReturn(admin);
        when(userService.findAllByRoleIn(Collections.singleton(UserRole.USER))).thenReturn(candidates);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("private/admin/management-page"))
                .andExpect(model().attribute("userName", "Admin"))
                .andExpect(model().attribute("candidatesToDelete", candidates));

        verify(userService, times(1)).findAllByRoleIn(Collections.singleton(UserRole.USER));
    }

    @Test
    @DisplayName("POST /admin/delete-user - Редирект, если пользователь не найден")
    void deleteUser_WhenUserNotFound_ShouldRedirectAndNotDelete() throws Exception {
        when(userService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/delete-user").param("id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("POST /admin/delete-user - Запрет удаления супер-администратора")
    void deleteUser_WhenTargetIsSuperAdmin_ShouldRedirectAndNotDelete() throws Exception {
        User superAdminTarget = new User("TargetSuper", "super2@test.com", "pass", UserRole.SUPER_ADMIN);
        User currentSuperAdmin = new User("CurrentSuper", "super1@test.com", "pass", UserRole.SUPER_ADMIN);

        when(userService.findById(1)).thenReturn(Optional.of(superAdminTarget));
        when(userService.getCurrentUser()).thenReturn(currentSuperAdmin);

        mockMvc.perform(post("/admin/delete-user").param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("POST /admin/delete-user - Администратор не может удалить другого администратора")
    void deleteUser_WhenTargetIsAdminAndCurrentIsAdmin_ShouldRedirectAndNotDelete() throws Exception {
        User adminTarget = new User("TargetAdmin", "target@test.com", "pass", UserRole.ADMIN);
        User currentAdmin = new User("CurrentAdmin", "current@test.com", "pass", UserRole.ADMIN);

        when(userService.findById(2)).thenReturn(Optional.of(adminTarget));
        when(userService.getCurrentUser()).thenReturn(currentAdmin);

        mockMvc.perform(post("/admin/delete-user").param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("POST /admin/delete-user - Супер-администратор успешно удаляет администратора")
    void deleteUser_WhenTargetIsAdminAndCurrentIsSuperAdmin_ShouldDeleteAndRedirect() throws Exception {
        User adminTarget = new User("TargetAdmin", "target@test.com", "pass", UserRole.ADMIN);
        User currentSuperAdmin = new User("CurrentSuper", "current@test.com", "pass", UserRole.SUPER_ADMIN);

        when(userService.findById(2)).thenReturn(Optional.of(adminTarget));
        when(userService.getCurrentUser()).thenReturn(currentSuperAdmin);

        mockMvc.perform(post("/admin/delete-user").param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, times(1)).deleteById(2);
    }

    @Test
    @DisplayName("POST /admin/delete-user - Администратор успешно удаляет обычного пользователя")
    void deleteUser_WhenTargetIsRegularUserAndCurrentIsAdmin_ShouldDeleteAndRedirect() throws Exception {
        User regularUserTarget = new User("TargetUser", "target@test.com", "pass", UserRole.USER);
        User currentAdmin = new User("CurrentAdmin", "current@test.com", "pass", UserRole.ADMIN);

        when(userService.findById(3)).thenReturn(Optional.of(regularUserTarget));
        when(userService.getCurrentUser()).thenReturn(currentAdmin);

        mockMvc.perform(post("/admin/delete-user").param("id", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService, times(1)).deleteById(3);
    }
}