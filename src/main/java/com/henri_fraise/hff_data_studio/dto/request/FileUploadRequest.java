package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.FileType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "File type is required")
    private FileType fileType;
}
