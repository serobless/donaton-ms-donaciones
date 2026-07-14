package cl.duoc.donaton.msdonaciones.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
@Tag(name = "Imágenes", description = "Upload de imágenes vía Cloudinary")
@RequiredArgsConstructor
public class ImagenController {

    private final Cloudinary cloudinary;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Subir imagen a Cloudinary (ADMIN)")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "donaton")
        );
        String url = (String) result.get("secure_url");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
