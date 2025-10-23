package com.api.usuarios.controllers.v1.usuarios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.api.usuarios.config.security.SecuredEndpoint;
import com.api.usuarios.entities.roles.Roles;
import com.api.usuarios.entities.usuarios.UsuarioDetalle;
import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.enums.RolesEnum;
import com.api.usuarios.models.Dto.Usuarios.ActualizarUsuarioDTO;
import com.api.usuarios.models.Dto.Usuarios.CambiarPasswordDTO;
import com.api.usuarios.models.Dto.Usuarios.RegistroUsuarioDTO;
import com.api.usuarios.models.Dto.Usuarios.UsuarioDetalleDTO;
import com.api.usuarios.models.Dto.Usuarios.UsuariosDTO;
import com.api.usuarios.services.GenericService.PaginateResult;
import com.api.usuarios.services.RabbitService;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.services.UsuariosService;
import com.api.usuarios.utilities.ImageUtil;
import com.api.usuarios.utilities.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@RestController
@Tag(name = "Usuarios", description = "Endpoint para la gestión de usuarios")
@RequestMapping("/api/v1/usuarios")

public class UsuariosController {

    @Autowired
    private UsuariosService _usuarioService;
    @Autowired
    private ModelMapper _modelMapper;

    @Autowired
    private BCryptPasswordEncoder _passwordEncoder;

    @Autowired
    private RolesService _rolesService;

    @Autowired
    private RabbitService _rabbitService;

    @Autowired
    private Validator _validator;

    public static final String QUEUE_USUARIOS = "usuarios.queue";

    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(
            @Valid @RequestBody RegistroUsuarioDTO dto,
            @AuthenticationPrincipal Usuarios usuario) {

        // Verificamos si el usuario esta logueado y es admin
        boolean esAdmin = usuario != null &&
                RolesEnum.ADMIN.name().equals(usuario.getRol().getDescripcion());
        // Mapeamos el dto a la entidad Usuarios
        Usuarios nuevo = _modelMapper.map(dto, Usuarios.class);

        nuevo.setPassword(_passwordEncoder.encode(dto.getPassword()));
        nuevo.setVerificado(true);
        nuevo.setEstatus(true);
        nuevo.setLastLogin(LocalDateTime.now());

        // Si no es admin y el rol esta nulo seteamos el rol de usuario
        if (!esAdmin || dto.getId_rol() == null) {
            Roles rol = _rolesService.findOneByField("descripcion", "USER").orElseThrow();
            nuevo.setId_rol(rol.getId());
        }

        UsuarioDetalle detalle = _modelMapper.map(dto.getDetalle(), UsuarioDetalle.class);

        // Si la foto no esta vacia la guardamos
        if (detalle.getFoto() != null && !detalle.getFoto().isEmpty()) {
            String ruta = ImageUtil.saveImage("users", detalle.getFoto());
            detalle.setFoto(ruta);
        } else {
            detalle.setFoto("/images/users/default.png");
        }

        nuevo.setDetalle(detalle);
        _usuarioService.save(nuevo);

        return ResponseEntity.ok(
                ResponseUtil.Response("¡Operación exitosa!", "Usuario creado exitosamente"));
    }

    @PutMapping("modificar/{id}")
    @SecuredEndpoint
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> modificarUsuario(
            @PathVariable String id,
            @Valid @RequestBody ActualizarUsuarioDTO dto,
            @AuthenticationPrincipal Usuarios usuarioLogueado) {

        // Verificamos que el usuario logueado sea admin
        boolean esAdmin = usuarioLogueado != null &&
                RolesEnum.valueOf(usuarioLogueado.getRol().getDescripcion()) == RolesEnum.ADMIN;

        // Obtenemos el usuario
        Usuarios usuario = _usuarioService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Obtenemos la ruta de la fotoActual del usuario
        String fotoActual = (usuario.getDetalle().getFoto() != null)
                ? usuario.getDetalle().getFoto()
                : null;

        // Mapeamos el dto al usuario sin perder valores
        _modelMapper.getConfiguration().setSkipNullEnabled(true);
        _modelMapper.map(dto, usuario);

        // Solo admin o si se especifica id_rol
        if (esAdmin && dto.getId_rol() != null) {
            usuario.setId_rol(dto.getId_rol());
        }

        // Si tenemos detalle
        String nuevaFoto = usuario.getDetalle().getFoto();
        if (nuevaFoto != null && !nuevaFoto.isEmpty()) {
            String ruta = ImageUtil.saveImage("users", nuevaFoto);
            usuario.getDetalle().setFoto(ruta);

            // Eliminar foto anterior si no es default
            if (fotoActual != null && !fotoActual.equals("default.png")) {
                ImageUtil.deleteFile(fotoActual);
            }
        }
        // Guardar cambios
        _usuarioService.save(usuario);

        return ResponseEntity.ok(ResponseUtil.Response("¡Operación exitosa!", "Usuario actualizado correctamente"));
    }

    @GetMapping("/listado")
    @SecuredEndpoint
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listado de usuarios", description = "Obtiene un listado de usuarios con paginación, búsqueda y ordenamiento")
    public ResponseEntity<?> listado(
            @Parameter(description = "Número de página (default 1)", schema = @Schema(defaultValue = "1")) @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Cantidad de elementos por página (default 10)", schema = @Schema(defaultValue = "10")) @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Texto de búsqueda global") @RequestParam(required = false) String search,

            @Parameter(description = "Campos donde aplicar búsqueda") @RequestParam(required = false) List<String> searchFields,

            @Parameter(description = "Campos y dirección de ordenamiento, ejemplo: id:ASC") @RequestParam(required = false) Map<String, String> order) {

        if (searchFields == null)
            searchFields = List.of();
        if (order == null)
            order = Map.of();

        PaginateResult<Usuarios> result = _usuarioService.paginate(
                page,
                limit,
                search,
                searchFields,
                null,
                order);

        List<UsuariosDTO> dtoList = result.data().stream()
                .map(usuario -> _modelMapper.map(usuario, UsuariosDTO.class))
                .toList();

        PaginateResult<UsuariosDTO> dtoResult = new PaginateResult<>(dtoList, result.total());

        return ResponseEntity.ok(
                ResponseUtil.Response("Operación Exitosa", dtoResult.data()));
    }

    @DeleteMapping("eliminar/{id}")
    @SecuredEndpoint

    public ResponseEntity<?> eliminarUsuario(@PathVariable String id) {

        Boolean estatus = _usuarioService.cambiarEstatus(id);
        String estatusStr = "Se cambio al estatus : " + (estatus ? "ACTIVO" : "INACTIVO");
        return new ResponseEntity<>(
                ResponseUtil.Response("Operación Exitosa", estatusStr),
                HttpStatus.OK);
    }

    @PostMapping(value = "/importar-usuarios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecuredEndpoint
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> importarUsuarios(@RequestParam MultipartFile file,
            @AuthenticationPrincipal Usuarios usuarioLogueado) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.Response("El archivo CSV está vacío"));
        }

        boolean esAdmin = usuarioLogueado != null &&
                "ADMIN".equalsIgnoreCase(usuarioLogueado.getRol().getDescripcion());

        List<RegistroUsuarioDTO> listaUsuarios = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        int filasProcesadas = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader() // usa la primera fila como encabezado
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                filasProcesadas++;
                try {
                    Map<String, String> fila = record.toMap();
                    fila.replaceAll((k, v) -> v != null ? v.trim().replace("\"", "") : null);

                    RegistroUsuarioDTO dto = _modelMapper.map(fila, RegistroUsuarioDTO.class);

                    UsuarioDetalleDTO detalle = new UsuarioDetalleDTO();
                    detalle.setNombres(fila.get("nombres"));
                    detalle.setApellidos(fila.get("apellidos"));
                    detalle.setFoto(null); // la foto se maneja en el consumer
                    dto.setDetalle(detalle);

                    // Si no es admin o el rol no está especificado, el consumer lo asigna por
                    // defecto
                    if (!esAdmin || dto.getId_rol() == null) {
                        dto.setId_rol(null);
                    }

                    listaUsuarios.add(dto);

                } catch (Exception e) {
                    errores.add("Fila " + filasProcesadas + ": " + e.getMessage());
                }
            }

            // Enviar todo el lote como JSON al RabbitMQ
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(listaUsuarios);
            _rabbitService.enviarMensaje(QUEUE_USUARIOS, json);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ResponseUtil.Response("Error procesando CSV: " + e.getMessage()));
        }

        // Construir respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("filasProcesadas", filasProcesadas);
        resultado.put("filasEnviadas", listaUsuarios.size());
        resultado.put("errores", errores);

        return ResponseEntity.ok(ResponseUtil.Response("Importación finalizada", resultado));
    }

    @PutMapping("cambiar-password")
    @SecuredEndpoint
    public ResponseEntity<?> cambiarPassword(
        @Valid @RequestBody CambiarPasswordDTO dto,
        @AuthenticationPrincipal Usuarios usuarioLogueado) {

        _usuarioService.CambiarPassword(usuarioLogueado, dto);
        
        return ResponseEntity.ok(ResponseUtil.Response("¡Operacion exitosa!", "¡Se cambio la contraseña con exito!"));
    }

}
