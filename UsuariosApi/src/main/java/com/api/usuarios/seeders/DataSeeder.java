package com.api.usuarios.seeders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
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
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final RolesService rolesService;
    private final GenerosService generosService;
    private final PaisesService paisesService;
    private final UsuariosService usuariosService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public DataSeeder(RolesService rolesService,
                      GenerosService generosService,
                      PaisesService paisesService,
                      UsuariosService usuariosService,
                      BCryptPasswordEncoder passwordEncoder,
                      MongoTemplate mongoTemplate) {
        this.rolesService = rolesService;
        this.generosService = generosService;
        this.paisesService = paisesService;
        this.usuariosService = usuariosService;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Ejecutando DataSeeder <<<");

        // --- Roles ---
        if (rolesService.count() == 0) {
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
            rolesService.save(admin);
            rolesService.save(user);
        }

        // --- Géneros ---
        if (generosService.count() == 0) {
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
            generosService.save(masculino);
            generosService.save(femenino);
        }

        // --- Países ---
        if (paisesService.count() == 0) {
            Paises mexico = Paises.builder()
                    .nombre("México")
                    .nombreCorto("MX")
                    .codigoPais("MX")
                    .codigoTelefono("+52")
                    .estatus(true)
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();
            paisesService.save(mexico);
        }

       

        // --- Usuario de prueba ---
        if (usuariosService.count() == 0) {
            Roles adminRole = rolesService.findAll().stream()
                    .filter(r -> r.getDescripcion().equals("ADMIN"))
                    .findFirst()
                    .orElseThrow();

            Generos genero = generosService.findAll().stream()
                    .filter(g -> g.getNombre().equals("Masculino"))
                    .findFirst()
                    .orElseThrow();

            Paises pais = paisesService.findAll().stream()
                    .filter(p -> p.getNombre().equals("México"))
                    .findFirst()
                    .orElseThrow();

            UsuarioDetalle detalle = UsuarioDetalle.builder()
                    .nombres("Carlos")
                    .apellidos("Sandoval")
                    .telefono("1234567890")
                    .id_pais(pais.getId())
                    .foto("/images/users/default.png")
                    .fechaNacimiento(LocalDate.of(2001, 2, 14))
                    .id_genero(genero.getId())
                    .build();

            Usuarios usuario = Usuarios.builder()
                    .correo("carlos@example.com")
                    .username("carlos")
                    .password(passwordEncoder.encode("1234"))
                    .id_rol(adminRole.getId())
                    .estatus(true)
                    .verificado(true)
                    .lastLogin(LocalDateTime.now())
                    .detalle(detalle)
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .build();

            usuariosService.save(usuario);
        }

        System.out.println(">>> DataSeeder completado <<<");
    }
}
