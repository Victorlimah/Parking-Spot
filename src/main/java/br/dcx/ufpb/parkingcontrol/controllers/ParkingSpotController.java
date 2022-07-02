package br.dcx.ufpb.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.dcx.ufpb.parkingcontrol.dtos.ParkingSpotDto;
import br.dcx.ufpb.parkingcontrol.models.ParkingSpotModel;
import br.dcx.ufpb.parkingcontrol.services.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
  
  final ParkingSpotService parkingSpotService;

  public ParkingSpotController(ParkingSpotService parkingSpotService) {
    this.parkingSpotService = parkingSpotService;
  }

  @PostMapping
  public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {

    if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Lincese Plate Car is already registered");
    }

    if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot Number is already in use");
    }

    if(parkingSpotService.existsByApartamentAndBlock(parkingSpotDto.getApartament(), parkingSpotDto.getBlock())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already registered from this apartament and block");
    }

    var parkingSpotModel = new ParkingSpotModel();
    BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);

    parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
    return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
  }

  @GetMapping
  public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> getOneParkingSpot (@PathVariable(value = "id") UUID id) {
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

    if(!parkingSpotModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found: Parking Spot not found");
    }

    return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteParkingSpot (@PathVariable(value = "id") UUID id) {
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

    if(!parkingSpotModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found: Parking Spot not found");
    }

    parkingSpotService.delete(parkingSpotModelOptional.get());
    return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully");
  }

  @PutMapping("/{id}")
  public ResponseEntity<Object> updateParkingSpot (@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto) {
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

    if(!parkingSpotModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found: Parking Spot not found");
    }

    var parkingSpotModel = parkingSpotModelOptional.get();
    BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);

    parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
    parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());

    return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
  }
}
