package com.cmpt276g12.RecQ.participant;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import static org.junit.jupiter.api.Assertions.*;

class FacilityDiscoveryControllerTest {

    @Test
    void facilitiesPageReturnsCorrectView() {
        FacilityDiscoveryController controller =
                new FacilityDiscoveryController();

        assertEquals(
                "participant/facilities",
                controller.showFacilities()
        );
    }

    @Test
    void facilitiesRouteIsCorrect() throws Exception {
        GetMapping mapping = FacilityDiscoveryController.class
                .getMethod("showFacilities")
                .getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(
                new String[]{"/facilities"},
                mapping.value()
        );
    }

    @Test
    void frontendFilesExist() {
        ClassLoader loader = getClass().getClassLoader();

        assertNotNull(loader.getResource(
                "templates/participant/facilities.html"
        ));

        assertNotNull(loader.getResource(
                "static/js/facilities.js"
        ));

        assertNotNull(loader.getResource(
                "static/css/epic2.css"
        ));
    }
}