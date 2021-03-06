package com.jamesknights.application.controller;

import com.jamesknights.common.user.model.User;
import com.jamesknights.common.user.service.UserService;
import com.jamesknights.common.user.service.impl.LocalUserService;
import com.jamesknights.common.util.JSONTool;
import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRequestController {

    private static final Logger LOG = Logger.getLogger(UserRequestController.class);
    private UserService userService;
    private final JSONTool jsonTool= new JSONTool();
    private final PolicyFactory policy = (PolicyFactory) Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    
    @Bean
    private UserService userService() {
        userService = new LocalUserService("userService");
        userService.start();
        return userService;
    }
    
    public UserRequestController() {
        
    }
    
    @CrossOrigin
    @MessageMapping("/user/create")
    @SendTo("/user/created")
    @RequestMapping(value = "/createUser", method = RequestMethod.POST)
    public User createUser(@RequestParam(value="emailAddress") String emailAddress, @RequestParam(value="forename") String forename, 
            @RequestParam(value="lastname") String lastname, @RequestParam(value="password") String password, String[] options) {
        
        User result = null;
        
        if (userService.isRunning()) {
            try {
                User user = userService.createUser(emailAddress, sanitiseInput(forename), sanitiseInput(lastname), sanitiseInput(password), true);
                if (user != null) {
                    LOG.debug("User Created");
                    result = user;
                } else {
                    LOG.debug("Duplicate User");
                }
            } catch (Exception e) {
                LOG.error("Unable to create User" + e.getMessage());
            }
        }
        
        return result;
    }
    
    @RequestMapping(value = "/findUser", method = RequestMethod.GET)
    public User findUser (@RequestParam(value="uId") long uId) {
        
        User result = null;
        
        if (userService.isRunning()) {
            try {
                result = userService.findUser(uId);
            } catch (Exception e) {
                LOG.error("Unable to create User" + e.getMessage());
            }
        }
        
        return result;
    }
    
    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    public boolean updateUser (@RequestParam(value="uId") long uId, @RequestParam(value="data") String data) {
        HashMap<String, Object> request = (HashMap<String, Object>) jsonTool.parseMap(data);
        boolean result = false;
        
        if (userService.isRunning()) {
            try {
                 result = userService.updateUser(uId, sanitiseMap(request));
            } catch (Exception e) {
                LOG.error("Unable to create User" + e.getMessage());
            }
        }
        
        return result;
    }
    
    @RequestMapping(value = "/deleteUser", method = RequestMethod.DELETE)
    public boolean deleteUser (@RequestParam(value="uId") long uId) {
        
        boolean result = false;
        
        if (userService.isRunning()) {
            try {
                 result = userService.deleteUser(uId);
            } catch (Exception e) {
                LOG.error("Unable to delete User" + e.getMessage());
            }
        }
        
        return result;
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public User login (@RequestParam(value="password") String password, @RequestParam(value="emailAddress") String emailAddress) {
        
        User result = null;
        
        if (userService.isRunning()) {
            try {
                 result = userService.userLogin(sanitiseInput(password), emailAddress);
            } catch (Exception e) {
                LOG.error("Unable to login User" + e.getMessage());
            }
        }
        
        return result;
    }
    
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public boolean changePassword (@RequestParam(value="oldPassword") String oldPassword, @RequestParam(value="newPassword") String newPassword, 
            @RequestParam(value="emailAddress") String emailAddress) {
        
        boolean result = false;
        
        if (userService.isRunning()) {
            try {
                 result = userService.changePassword(sanitiseInput(oldPassword), sanitiseInput(newPassword), emailAddress);
            } catch (Exception e) {
                LOG.error("Unable to change User password" + e.getMessage());
            }
        }
        
        return result;
    }
    
    private String sanitiseInput (String input) {
        String result = null;
        if (!input.isEmpty()) {
            result = policy.sanitize(input);
        }
        return result;
    }
    
    private HashMap<String, Object> sanitiseMap (HashMap<String, Object> input) {
        if (input != null) {
            for (Map.Entry entry : input.entrySet()) {
                if (!entry.getKey().equals("emailAddress")) {
                    entry.setValue(sanitiseInput((String) entry.getValue()));
                }
                
            }
        }
        return input;
    }
    
}