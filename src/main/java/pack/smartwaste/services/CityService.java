package pack.smartwaste.services;

import org.springframework.stereotype.Service;
import pack.smartwaste.models.user.City;
import pack.smartwaste.rep.CityRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {
    private CityRepository cityRepository;
    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }
    public List<City> getAll() {
        return cityRepository.findAll();
    }
    public Optional<City> getCityByName(String name) {
        return cityRepository.findCitiesByVille(name);
    }
}
