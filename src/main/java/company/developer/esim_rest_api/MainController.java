package company.developer.esim_rest_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path="/device")
public class MainController {

    @Autowired
    private DeviceRepository deviceRepository;

    @PostMapping(path="/add") // Map ONLY POST Requests
    public @ResponseBody String addNewDevice(@RequestParam String name
            , @RequestParam String email) {

        Device n = new Device();
        n.setName(name);
        n.setEmail(email);
        deviceRepository.save(n);
        return "Saved";
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<Device> getAllDevices() {
        // This returns a JSON or XML with the users
        return deviceRepository.findAll();
    }
}
