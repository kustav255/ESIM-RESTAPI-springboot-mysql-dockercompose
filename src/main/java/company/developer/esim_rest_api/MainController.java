package company.developer.esim_rest_api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/devices")
public class MainController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Operation(
            summary = "Get all devices",
            description = "Returns a list of all devices. Optionally filter by brand or state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of devices returned successfully")
    })
    @GetMapping
    public @ResponseBody ResponseEntity<Iterable<Device>> getAllDevices(
        @Parameter(description = "Filter devices by brand (case-insensitive)") @RequestParam(required = false) String brand,
        @Parameter(description = "Filter devices by state (AVAILABLE, INUSE, INACTIVE)") @RequestParam(required = false) STATE state) {

        // If brand is provided, filter by brand
        if (brand != null) {
            return ResponseEntity.ok(deviceRepository.findByBrandContainingIgnoreCase(brand));
        }
        // If state is provided, filter by state
        else if (state != null) {
            return ResponseEntity.ok(deviceRepository.findByState(state));
        }
        // If no filters are provided, return all devices
        return ResponseEntity.ok(deviceRepository.findAll());
    }

    @Operation(
            summary = "Add a new device",
            description = "Creates a new device with the given name and brand. State is set to AVAILABLE by default."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created successfully")
    })
    @PostMapping
    public @ResponseBody ResponseEntity<String> addNewDevice(
            @Parameter(description = "Device name", required = true) @RequestParam String name,
            @Parameter(description = "Device brand", required = true) @RequestParam String brand) {

        Device n = new Device();
        n.setName(name);
        n.setBrand(brand);
        n.setState(STATE.AVAILABLE);
        deviceRepository.save(n);
        return ResponseEntity.status(201).body("Saved");
    }

    @Operation(
            summary = "Get device by ID",
            description = "Returns a device by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Not Found")))
    })
    @GetMapping(path="/{id}")
    public @ResponseBody ResponseEntity<Device> getDeviceById(
            @Parameter(description = "Device ID", required = true) @PathVariable Integer id) {
        return deviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Update device name and/or brand",
            description = "Updates the name and/or brand of a device by ID. Only allowed if device is not in use."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "No fields to update"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Device in use, cannot update")
    })
    @PutMapping(path="/{id}")
    public @ResponseBody ResponseEntity<String> updateDevice(
            @Parameter(description = "Device ID", required = true) @PathVariable Integer id,
            @Parameter(description = "New device name") @RequestParam(required = false) String name,
            @Parameter(description = "New device brand")  @RequestParam(required = false) String brand) {

        // If both name and brand are null, return the message below
        if(name == null && brand == null){
            return ResponseEntity.badRequest().body("No fields to update");
        }

        // Check if device exists and update its fields
        return deviceRepository.findById(id).map(device -> {
            // Only update if the device is not in use
            if (!device.getState().equals(STATE.INUSE)) {
                if (name != null) device.setName(name);
                if (brand != null) device.setBrand(brand);
                deviceRepository.save(device);
                return ResponseEntity.ok("Updated");
            }
            // If the device is in use, return a conflict (409) response
            return ResponseEntity.status(409).body("Not updated. Device in-use");
        }).orElse(ResponseEntity.notFound().build());
    }


    @Operation(
            summary = "Update device state",
            description = "Updates the state of a device (AVAILABLE, INUSE, INACTIVE) by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device state updated"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    // Update device state by ENUM
    @PatchMapping("/{id}")
    public @ResponseBody ResponseEntity<String> updateDeviceState(
        @Parameter(description = "Device ID", required = true) @PathVariable Integer id,
        @Parameter(description = "New device state (AVAILABLE, INUSE, INACTIVE)", required = true) @RequestParam STATE state) {

        // Check if the device exists and update its state to the ENUM options
        // AVAILABLE, INUSE, INACTIVE
        return deviceRepository.findById(id).map(device -> {
            device.setState(state);
            deviceRepository.save(device);
            return ResponseEntity.ok("Device state updated to: " + state.getState());
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Delete device by ID",
            description = "Deletes a device by its ID. Only allowed if device is not in use."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Device in use, cannot delete")
    })
    @DeleteMapping(path="/{id}")
    public @ResponseBody ResponseEntity<String> deleteDevice(
            @Parameter(description = "Device ID", required = true) @PathVariable Integer id) {

        // Check if the device exists and delete it
        return deviceRepository.findById(id).map(device -> {
            // Only delete if the device is not in use
            if (!device.getState().equals(STATE.INUSE)) {
                deviceRepository.deleteById(id);
                return ResponseEntity.ok("Deleted");
            }
            return ResponseEntity.status(409).body("Not deleted. Device in-use");
        }).orElse(ResponseEntity.notFound().build());
    }
}
