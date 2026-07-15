package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.FileType;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
