package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagesServiceTest {

    @Mock
    private ManagesRepository managesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private ManagesService managesService;

    private User user;
    private Location location;
    private Manages manages;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setRoles(new HashSet<>(Collections.singletonList(Role.ROLE_USER)));

        location = new Location();
        location.setId(1L);
        location.setName("Test Location");
        location.setDeleted(false);

        manages = new Manages();
        manages.setId(1L);
        manages.setUser(user);
        manages.setLocation(location);
        manages.setStartDate(LocalDate.now());
    }

    @Test
    void getLocationManagers_Success() {
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(managesRepository.findActiveManagersByLocation(anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));

        List<ManagerDTO> result = managesService.getLocationManagers(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getUserId());
        verify(locationRepository).findById(1L);
        verify(managesRepository).findActiveManagersByLocation(eq(1L), any(LocalDate.class));
    }

    @Test
    void getLocationManagers_LocationNotFound() {
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            managesService.getLocationManagers(1L)
        );
    }

    @Test
    void getLocationManagers_LocationDeleted() {
        location.setDeleted(true);
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));

        assertThrows(IllegalArgumentException.class, () -> 
            managesService.getLocationManagers(1L)
        );
    }

    @Test
    void assignManager_Success() {
        AssignManagerDTO dto = new AssignManagerDTO(1L);
        
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(managesRepository.save(any(Manages.class))).thenReturn(manages);

        managesService.assignManager(1L, dto);

        assertTrue(user.getRoles().contains(Role.ROLE_MANAGER));
        verify(userRepository).save(user);
        verify(managesRepository).save(any(Manages.class));
    }

    @Test
    void assignManager_UserNotFound() {
        AssignManagerDTO dto = new AssignManagerDTO(1L);
        
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            managesService.assignManager(1L, dto)
        );
    }

    @Test
    void assignManager_AlreadyManager() {
        AssignManagerDTO dto = new AssignManagerDTO(1L);
        
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));

        assertThrows(IllegalArgumentException.class, () -> 
            managesService.assignManager(1L, dto)
        );
    }

    @Test
    void removeManager_Success() {
        user.getRoles().add(Role.ROLE_MANAGER);
        
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));
        when(managesRepository.findActiveByUserId(anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(managesRepository.save(any(Manages.class))).thenReturn(manages);
        when(userRepository.save(any(User.class))).thenReturn(user);

        managesService.removeManager(1L, 1L);

        assertFalse(user.getRoles().contains(Role.ROLE_MANAGER));
        verify(managesRepository).save(manages);
        verify(userRepository).save(user);
    }

    @Test
    void removeManager_NotAManager() {
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> 
            managesService.removeManager(1L, 1L)
        );
    }
}
