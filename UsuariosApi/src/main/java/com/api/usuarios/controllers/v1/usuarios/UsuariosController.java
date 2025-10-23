package com.api.usuarios.controllers.v1.usuarios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.api.usuarios.config.security.SecuredEndpoint;
import com.api.usuarios.entities.roles.Roles;
import com.api.usuarios.entities.usuarios.UsuarioDetalle;
import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.models.Dto.Usuarios.ActualizarUsuarioDTO;
import com.api.usuarios.models.Dto.Usuarios.RegistroUsuarioDTO;
import com.api.usuarios.models.Dto.Usuarios.UsuariosDTO;
import com.api.usuarios.services.GenericService.PaginateResult;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.services.UsuariosService;
import com.api.usuarios.utilities.ImageUtil;
import com.api.usuarios.utilities.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(
            @Valid @RequestBody RegistroUsuarioDTO dto,
            @AuthenticationPrincipal Usuarios usuario) {

        boolean esAdmin = usuario != null && "ADMIN".equals(usuario.getRol().getDescripcion());

        // Mapear DTO principal a entidad
        Usuarios nuevo = _modelMapper.map(dto, Usuarios.class);

        // Password codificada
        nuevo.setPassword(_passwordEncoder.encode(dto.getPassword()));
        nuevo.setVerificado(true);
        nuevo.setEstatus(true);
        nuevo.setLastLogin(LocalDateTime.now());

        // Asignar rol
        if (!esAdmin || dto.getId_rol() == null) {
            Roles rol = _rolesService.findOneByField("descripcion", "USER").orElseThrow();
            nuevo.setId_rol(rol.getId());
        }

        // Mapear detalle
        UsuarioDetalle detalle = _modelMapper.map(dto.getDetalle(), UsuarioDetalle.class);

        // Guardar foto si viene
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
    public ResponseEntity<?> modificarUsuario(
            @PathVariable String id,
            @Valid @RequestBody ActualizarUsuarioDTO dto,
            @AuthenticationPrincipal Usuarios usuarioLogueado) {

        boolean esAdmin = usuarioLogueado != null && "ADMIN".equals(usuarioLogueado.getRol().getDescripcion());

        // Obtener usuario desde DB
        Optional<Usuarios> optUsuario = _usuarioService.findById(id);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseUtil.Response("Usuario no encontrado"));
        }

        Usuarios usuario = optUsuario.get();

        // Actualizar campos básicos
        if (dto.getUsername() != null)
            usuario.setUsername(dto.getUsername());
        if (dto.getCorreo() != null)
            usuario.setCorreo(dto.getCorreo());

        // Solo admin o si se especifica id_rol
        if (esAdmin && dto.getId_rol() != null) {
            usuario.setId_rol(dto.getId_rol());
        }

        // Actualizar detalle
        UsuarioDetalle detalle = usuario.getDetalle();
        if (dto.getDetalle() != null) {
            if (dto.getDetalle().getNombres() != null)
                detalle.setNombres(dto.getDetalle().getNombres());
            if (dto.getDetalle().getApellidos() != null)
                detalle.setApellidos(dto.getDetalle().getApellidos());

            // Manejo de foto
            String nuevaFoto = dto.getDetalle().getFoto();
            if (nuevaFoto != null && !nuevaFoto.isEmpty()) {
                String fotoActual = detalle.getFoto();
                String ruta = ImageUtil.saveImage("users", nuevaFoto);
                detalle.setFoto(ruta);

                // Eliminar foto anterior si no es default
                if (fotoActual != null && !fotoActual.equals("default.png")) {
                    ImageUtil.deleteFile(fotoActual);
                }
            }
        }

        usuario.setDetalle(detalle);

        // Guardar cambios
        _usuarioService.save(usuario);

        return ResponseEntity.ok(ResponseUtil.Response("Usuario actualizado correctamente"));
    }

    @GetMapping("/listado")
    @SecuredEndpoint
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
                ResponseUtil.Response("Operación Exitosa", dtoResult));
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

}
