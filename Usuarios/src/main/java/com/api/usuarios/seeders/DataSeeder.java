package com.api.usuarios.seeders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.api.usuarios.entities.generos.Generos;
import com.api.usuarios.entities.paises.Paises;
import com.api.usuarios.entities.roles.Roles;
import com.api.usuarios.entities.usuarios.UsuarioDetalle;
import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.services.GenerosService;
import com.api.usuarios.services.PaisesService;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.services.UsuariosService;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RolesService rolesRepo;
    private final GenerosService generosRepo;
    private final PaisesService paisesRepo;
    private final UsuariosService usuariosRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(RolesService rolesRepo, GenerosService generosRepo,
                      PaisesService paisesRepo, UsuariosService usuariosRepo,
                      BCryptPasswordEncoder passwordEncoder) {
        this.rolesRepo = rolesRepo;
        this.generosRepo = generosRepo;
        this.paisesRepo = paisesRepo;
        this.usuariosRepo = usuariosRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Ejecutando DataSeeder <<<");

        // --- Roles ---
        if (rolesRepo.count() == 0) {
            Roles admin = Roles.builder()
                    .descripcion("ADMIN")
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();
            Roles user = Roles.builder()
                    .descripcion("USER")
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();
            rolesRepo.save(admin);
            rolesRepo.save(user);
        }

        // --- Géneros ---
        if (generosRepo.count() == 0) {
            Generos masculino = Generos.builder()
                    .nombre("Masculino")
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();
            Generos femenino = Generos.builder()
                    .nombre("Femenino")
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();
            generosRepo.save(masculino);
            generosRepo.save(femenino);
        }

        // --- Países ---
        if (paisesRepo.count() == 0) {
            Paises mexico = Paises.builder()
                    .nombre("México")
                    .nombreCorto("MX")
                    .codigoPais("MX")
                    .codigoTelefono("+52")
                    .estatus(true)
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();
            paisesRepo.save(mexico);
        }

        // --- Usuario de prueba ---
        if (usuariosRepo.count() == 0) {
            Roles userRole = rolesRepo.findAll().stream()
                    .filter(r -> r.getDescripcion().equals("USER"))
                    .findFirst()
                    .orElseThrow();

            Generos genero = generosRepo.findAll().stream()
                    .filter(g -> g.getNombre().equals("Masculino"))
                    .findFirst()
                    .orElseThrow();

            Paises pais = paisesRepo.findAll().stream()
                    .filter(p -> p.getNombre().equals("México"))
                    .findFirst()
                    .orElseThrow();

            UsuarioDetalle detalle = UsuarioDetalle.builder()
                    .nombres("Carlos")
                    .apellidos("Sandoval")
                    .biografia("Usuario de prueba")
                    .telefono("1234567890")
                    .pais(pais)
                    .fechaNacimiento(LocalDate.of(2001, 2, 14))
                    .genero(genero)
                    .build();

            Usuarios usuario = Usuarios.builder()
                    .correo("carlos@example.com")
                    .username("carlos")
                    .password(passwordEncoder.encode("1234")) // contraseña encriptada con BCrypt
                    .roles(userRole)
                    .estatus(true)
                    .verificado(true)
                    .lastLogin(LocalDateTime.now())
                    .detalle(detalle)
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();

            usuariosRepo.save(usuario);
        }

        System.out.println(">>> DataSeeder completado <<<");
    }
}
