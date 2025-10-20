package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.AssignManagerDTO;
import rs.ftn.newnow.dto.ManagerDTO;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.Manages;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ManagesRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagesService {

    private final ManagesRepository managesRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public List<ManagerDTO> getLocationManagers(Long locationId) {
        log.info("Fetching managers for location ID: {}", locationId);
        
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
        
        if (location.getDeleted()) {
            throw new IllegalArgumentException("Location is deleted");
        }
        
        List<Manages> activeManagers = managesRepository.findActiveManagersByLocation(locationId, LocalDate.now());
        
        return activeManagers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignManager(Long locationId, AssignManagerDTO assignManagerDTO) {
        log.info("Assigning user {} as manager to location {}", assignManagerDTO.getUserId(), locationId);
        
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
        
        if (location.getDeleted()) {
            throw new IllegalArgumentException("Location is deleted");
        }
        
        User user = userRepository.findById(assignManagerDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + assignManagerDTO.getUserId()));
        
        List<Manages> existingActive = managesRepository.findActiveManagement(
                user.getId(), locationId, LocalDate.now());
        
        if (!existingActive.isEmpty()) {
            throw new IllegalArgumentException("User is already a manager of this location");
        }
        
        user.getRoles().add(Role.ROLE_MANAGER);
        userRepository.save(user);
        
        Manages manages = new Manages();
        manages.setUser(user);
        manages.setLocation(location);
        manages.setStartDate(LocalDate.now());
        
        managesRepository.save(manages);
        log.info("Successfully assigned user {} as manager to location {}", user.getId(), locationId);
    }

    @Transactional
    public void removeManager(Long locationId, Long userId) {
        log.info("Removing manager {} from location {}", userId, locationId);
        
        locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        List<Manages> activeManagements = managesRepository.findActiveManagement(
                userId, locationId, LocalDate.now());
        
        if (activeManagements.isEmpty()) {
            throw new IllegalArgumentException("User is not an active manager of this location");
        }
        
        for (Manages manages : activeManagements) {
            manages.setEndDate(LocalDate.now());
            managesRepository.save(manages);
        }
        
        List<Manages> allActiveManagements = managesRepository.findActiveByUserId(userId, LocalDate.now());
        if (allActiveManagements.isEmpty()) {
            user.getRoles().remove(Role.ROLE_MANAGER);
            userRepository.save(user);
            log.info("Removed ROLE_MANAGER from user {} as they have no active managements", userId);
        }
        
        log.info("Successfully removed manager {} from location {}", userId, locationId);
    }

    private ManagerDTO convertToDTO(Manages manages) {
        ManagerDTO dto = new ManagerDTO();
        dto.setUserId(manages.getUser().getId());
        dto.setName(manages.getUser().getName());
        dto.setEmail(manages.getUser().getEmail());
        dto.setStartDate(manages.getStartDate());
        dto.setEndDate(manages.getEndDate());
        dto.setActive(manages.isActive());
        return dto;
    }
}
