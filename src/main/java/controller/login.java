package controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class login {
    @PostMapping ("/submit/login")
    public string login(
        @RequestParam 
    )
}
