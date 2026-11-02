package com.cmpt276g12.RecQ.participant;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FacilityDiscoveryController {

    @GetMapping("/facilities")
    public String showFacilities() {
        return "participant/facilities";
    }
}