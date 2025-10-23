package com.api.usuarios.seeders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

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
@Profile({"dev", "local"}) // se ejecuta sólo en dev/local
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
    public void run(String... args) {
        System.out.println(">>> [DataSeeder] Iniciando seeding de datos base...");

        seedRoles();
        seedGeneros();
        seedPaises();
        seedUsuarioAdmin();

        System.out.println(">>> [DataSeeder] Finalizado correctamente.");
    }

    // -----------------------------
    // MÉTODOS DE SEEDING
    // -----------------------------

    private void seedRoles() {
        insertIfNotExists(
            () -> rolesService.findOneByField("descripcion", "ADMIN"),
            () -> rolesService.save(Roles.builder()
                .descripcion("ADMIN")
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build())
        );

        insertIfNotExists(
            () -> rolesService.findOneByField("descripcion", "USER"),
            () -> rolesService.save(Roles.builder()
                .descripcion("USER")
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build())
        );
    }

    private void seedGeneros() {
        insertIfNotExists(
            () -> generosService.findOneByField("nombre", "Masculino"),
            () -> generosService.save(Generos.builder()
                .nombre("Masculino")
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build())
        );

        insertIfNotExists(
            () -> generosService.findOneByField("nombre", "Femenino"),
            () -> generosService.save(Generos.builder()
                .nombre("Femenino")
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build())
        );
    }

    private void seedPaises() {
        insertIfNotExists(
            () -> paisesService.findOneByField("nombre", "México"),
            () -> paisesService.save(Paises.builder()
                .nombre("México")
                .nombreCorto("MX")
                .codigoPais("MX")
                .codigoTelefono("+52")
                .estatus(true)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build())
        );
    }

    private void seedUsuarioAdmin() {
        if (usuariosService.count() > 0) return;

        Roles adminRole = rolesService.findOneByField("descripcion", "ADMIN")
            .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado"));
        Generos genero = generosService.findOneByField("nombre", "Masculino")
            .orElseThrow(() -> new IllegalStateException("Género no encontrado"));
        Paises pais = paisesService.findOneByField("nombre", "México")
            .orElseThrow(() -> new IllegalStateException("País no encontrado"));

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
        System.out.println("✔ Usuario admin creado: carlos@example.com");
    }

    // -----------------------------
    // MÉTODO UTILITARIO
    // -----------------------------

    private <T> void insertIfNotExists(Supplier<Optional<T>> finder, Supplier<T> creator) {
        if (finder.get().isEmpty()) {
            T entity = creator.get();
            System.out.println("✔ Insertado: " + entity.getClass().getSimpleName());
        }
    }
}
