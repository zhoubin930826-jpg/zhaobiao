package com.zhaobiao.admin.service;

import com.zhaobiao.admin.dto.file.FileUploadResponse;
import com.zhaobiao.admin.entity.TenderFileStorage;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    List<FileUploadResponse> store(List<MultipartFile> files);

    Resource loadAsResource(TenderFileStorage storage);

    void deleteStoredFile(TenderFileStorage storage);
}
