package com.mktplace.service;

import com.mktplace.dto.ProjectAssetDtos.ProjectAssetResponse;
import com.mktplace.enums.AssetType;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Project;
import com.mktplace.model.ProjectAsset;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProjectAssetService {
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("application/zip", "application/x-zip-compressed", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final ProjectService projectService;
    private final ProjectAssetRepository projectAssetRepository;
    private final StorageService storageService;
    private final long maxFileSizeBytes;

    public ProjectAssetService(ProjectService projectService,
                               ProjectAssetRepository projectAssetRepository,
                               StorageService storageService,
                               @Value("${app.storage.max-file-size-bytes:10485760}") long maxFileSizeBytes) {
        this.projectService = projectService;
        this.projectAssetRepository = projectAssetRepository;
        this.storageService = storageService;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public ProjectAssetResponse upload(User user, Long projectId, MultipartFile file, AssetType type) {
        Project project = projectService.getEntity(projectId);
        if (!project.getSeller().getId().equals(user.getId())) throw new BusinessException("Projeto não pertence ao usuário", HttpStatus.FORBIDDEN);
        validate(file, type);

        String extension = extension(file.getOriginalFilename());
        String storageKey = "projects/" + projectId + "/" + type.name().toLowerCase() + "/" + UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        try {
            storageService.upload(storageKey, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("Falha ao ler arquivo enviado", HttpStatus.BAD_REQUEST);
        }

        ProjectAsset saved = projectAssetRepository.save(ProjectAsset.builder()
                .project(project)
                .type(type)
                .originalFilename(file.getOriginalFilename() == null ? "arquivo" : file.getOriginalFilename())
                .storageKey(storageKey)
                .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                .sizeBytes(file.getSize())
                .createdAt(Instant.now())
                .build());

        return toResponse(saved);
    }

    public List<ProjectAssetResponse> list(User user, Long projectId) {
        Project project = projectService.getEntity(projectId);
        if (!project.getSeller().getId().equals(user.getId())) throw new BusinessException("Projeto não pertence ao usuário", HttpStatus.FORBIDDEN);
        return projectAssetRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::toResponse).toList();
    }

    public ResponseEntity<Resource> download(User user, Long projectId, Long assetId) {
        Project project = projectService.getEntity(projectId);
        if (!project.getSeller().getId().equals(user.getId())) throw new BusinessException("Projeto não pertence ao usuário", HttpStatus.FORBIDDEN);

        ProjectAsset asset = projectAssetRepository.findByIdAndProjectId(assetId, projectId)
                .orElseThrow(() -> new BusinessException("Arquivo não encontrado", HttpStatus.NOT_FOUND));

        InputStreamResource resource = new InputStreamResource(storageService.read(asset.getStorageKey()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(asset.getContentType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(asset.getOriginalFilename()).build());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private ProjectAssetResponse toResponse(ProjectAsset asset) {
        return new ProjectAssetResponse(asset.getId(), asset.getProject().getId(), asset.getType(), asset.getOriginalFilename(), asset.getContentType(), asset.getSizeBytes(), asset.getCreatedAt(), "/api/projects/" + asset.getProject().getId() + "/assets/" + asset.getId() + "/download");
    }

    private void validate(MultipartFile file, AssetType type) {
        if (file == null || file.isEmpty()) throw new BusinessException("Arquivo obrigatório", HttpStatus.BAD_REQUEST);
        if (file.getSize() > maxFileSizeBytes) throw new BusinessException("Arquivo excede limite de tamanho", HttpStatus.BAD_REQUEST);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (type == AssetType.IMAGE && !ALLOWED_IMAGE_TYPES.contains(contentType)) throw new BusinessException("Tipo de imagem não suportado", HttpStatus.BAD_REQUEST);
        if (type == AssetType.DOCUMENT && !ALLOWED_DOCUMENT_TYPES.contains(contentType)) throw new BusinessException("Tipo de documento não suportado (use ZIP/PDF/DOC)", HttpStatus.BAD_REQUEST);
    }

    private String extension(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
