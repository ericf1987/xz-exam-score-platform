package com.xz.scorep.manager.webui;

import com.xz.scorep.manager.manager.ExecutorAgent;
import com.xz.scorep.manager.manager.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes(WebUIController.SESSION_USER_KEY)
public class WebUIController {

    @Autowired
    private ManagerService managerService;

    public static final String SESSION_USER_KEY = "sessionUser";

    @ModelAttribute(SESSION_USER_KEY)
    public SessionUser createSessionUser() {
        return SessionUser.newAnonymousUser();
    }

    @GetMapping("/")
    public ModelAndView index(@ModelAttribute(SESSION_USER_KEY) SessionUser sessionUser) {

        if (!sessionUser.isLoggedIn()) {
            return new ModelAndView("login");
        } else {
            ModelAndView modelAndView = new ModelAndView("index").addObject(SESSION_USER_KEY, sessionUser);
            return dashboard(modelAndView);
        }
    }

    private ModelAndView dashboard(ModelAndView modelAndView) {
        modelAndView.addObject("agents", managerService.listExecutorAgents());
        return modelAndView;
    }

    @PostMapping("/login")
    public ModelAndView login(
            @ModelAttribute(SESSION_USER_KEY) SessionUser sessionUser,
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        if (username.equals("xzadmin") && password.equals("changsha123")) {
            sessionUser.setLoggedIn(true);
            sessionUser.setUserId("0");
            sessionUser.setUserName("管理员");
            return new ModelAndView("redirect:/");
        } else {
            return new ModelAndView("login").addObject("message", "用户名或密码不正确");
        }
    }

    @GetMapping("/aggregating")
    public ModelAndView aggregating(
            @RequestParam("server") String server
    ) {
        String[] host_port = server.split(":");
        ExecutorAgent executorAgent = managerService.getExecutorAgent(host_port[0], Integer.parseInt(host_port[1]));
        return new ModelAndView("aggregating").addObject("agent", executorAgent);
    }
}
