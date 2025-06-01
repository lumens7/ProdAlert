package br.com.lumens.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/*
Criado por Luís
*/

@RestController
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/index.html");
    }
}
