package com.api.usuarios.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
/**
 * Clase utilitaria para manejar imágenes en Base64.
 * Permite guardar, eliminar y listar imágenes en carpetas dentro de /images.
 */
public class ImageUtil {

    /**
     * Carpeta raíz donde se almacenan las imágenes.
     */
    private static final Path BASE_FOLDER = Paths.get(System.getProperty("user.dir"), "images");

    /**
     * Expresión regular para validar cadenas Base64 de imágenes.
     */
    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^data:image/(png|jpeg|jpg|gif|webp);base64,[A-Za-z0-9+/=]+$");

    /**
     * Valida si un string es una cadena Base64 válida para imagen.
     *
     * @param str Cadena a validar.
     * @return true si es Base64 válido.
     */
    public static boolean isValidBase64(String str) {
        return str != null && BASE64_PATTERN.matcher(str).matches();
    }

    /**
     * Guarda una imagen en base64 dentro de la carpeta indicada.
     *
     * @param folder      Carpeta dentro de /images.
     * @param imageBase64 Imagen en formato Base64 (con encabezado data:image/...).
     * @return Ruta pública relativa (por ejemplo: /images/perfil/uuid.png).
     */
    public static String saveImage(String folder, String imageBase64) {
        if (!isValidBase64(imageBase64)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese una imagen válida");
        }

        try {
            // Extraer formato e información Base64
            String[] parts = imageBase64.split(",");
            String meta = parts[0];
            String base64Data = parts[1];
            String format = meta.split("/")[1].split(";")[0];

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            Path folderPath = BASE_FOLDER.resolve(folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String imageName = UUID.randomUUID() + "." + format;
            Path imagePath = folderPath.resolve(imageName);

            Files.write(imagePath, imageBytes);

            return "/images/" + folder + "/" + imageName;

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar la imagen", e);
        }
    }

    /**
     * Elimina un archivo dado su ruta relativa (por ejemplo:
     * /images/perfil/file.png).
     *
     * @param filePath Ruta del archivo a eliminar.
     */
    public static void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank())
            return;

        try {
            String cleanPath = filePath.replaceFirst("^/+", ""); // quitar barras iniciales
            Path absolutePath = Paths.get(System.getProperty("user.dir"), cleanPath);

            if (Files.exists(absolutePath)) {
                Files.delete(absolutePath);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el archivo", e);
        }
    }

    /**
     * Lista todas las imágenes dentro de una carpeta.
     *
     * @param folder Carpeta dentro de /images.
     * @return Lista de rutas públicas relativas.
     */
    public static List<String> listImages(String folder) {
        Path folderPath = BASE_FOLDER.resolve(folder);
        if (!Files.exists(folderPath))
            return List.of();

        try {
            return Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .map(path -> "/images/" + folder + "/" + path.getFileName())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al listar imágenes", e);
        }
    }
}
