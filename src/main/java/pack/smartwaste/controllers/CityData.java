package pack.smartwaste.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pack.smartwaste.models.user.City;
import pack.smartwaste.services.CityService;

import java.util.List;

@RestController
@RequestMapping("/data")
public class CityData {
    private CityService cityService;

    public CityData(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("cities")
    public ResponseEntity<List<City>> cities() {
        return new ResponseEntity<>(cityService.getAll(), HttpStatus.OK);
    }
}
