package cl.duoc.donaton.msdonaciones.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
@Tag(name = "Imágenes", description = "Upload y servicio de imágenes")
public class ImagenController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Subir imagen (ADMIN)")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {
        Path dir = Paths.get(UPLOAD_DIR);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String filename = System.currentTimeMillis() + "_" + Paths.get(file.getOriginalFilename()).getFileName();
        Files.copy(file.getInputStream(), dir.resolve(filename));
        return ResponseEntity.ok(Map.of("url", "/api/imagenes/uploads/" + filename));
    }

    @GetMapping("/uploads/{nombre}")
    @Operation(summary = "Servir imagen (público)")
    public ResponseEntity<byte[]> serveFile(@PathVariable String nombre) throws IOException {
        String safeName = Paths.get(nombre).getFileName().toString();
        Path filePath = Paths.get(UPLOAD_DIR).resolve(safeName);
        if (!Files.exists(filePath)) return ResponseEntity.notFound().build();
        byte[] data = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        contentType != null ? contentType : "application/octet-stream"))
                .body(data);
    }
}
