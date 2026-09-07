package ru.wartesd.controller.secured;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.wartesd.entity.RecordStatus;
import ru.wartesd.entity.User;
import ru.wartesd.entity.dto.RecordsContainerDto;
import ru.wartesd.service.RecordService;
import ru.wartesd.service.UserService;


@RequestMapping("/account")
@Controller
public class PrivateAccountController {
    private final UserService userService;
    private final RecordService recordService;

    @Autowired
    public PrivateAccountController(UserService userService, RecordService recordService) {
        this.userService = userService;
        this.recordService = recordService;
    }

    @GetMapping
    public String getMainPage(HttpServletRequest request, @NonNull Model model, @RequestParam(name = "filter", required = false) String filterMode){
        RecordsContainerDto container = recordService.findAllRecords(filterMode);
        model.addAttribute("userName", container.getUsername());
        model.addAttribute("numberOfDoneRecords",container.getNumberOfDoneRecords());
        model.addAttribute("numberOfActiveRecords", container.getNumberOfActiveRecords());
        model.addAttribute("records", container.getRecords());
        return "private/account-page";
    }

    @PostMapping(value = "/add-record")
    public String addRecord(@RequestParam(name = "title" ) String title){
        recordService.saveRecord(title);
        return "redirect:/account";
    }

    @PostMapping(value = "/make-record-done")
    public String makeRecordDone(@RequestParam(name = "id") int id,
                                 @RequestParam(name = "filter", required = false) String filterMode){
        recordService.updateRecordStatus(id,RecordStatus.DONE);
        return "redirect:/account" + (filterMode!= null && !filterMode.isBlank() ? "?filter=" + filterMode : "") ;
    }

    @PostMapping(value = "/delete-record")
    public String deleteRecord(@RequestParam(name = "id") int id,
                               @RequestParam(name = "filter", required = false) String filterMode){
        recordService.deleteRecord(id);
        return "redirect:/account" + (filterMode!= null && !filterMode.isBlank() ? "?filter=" + filterMode : "") ;
    }
}
