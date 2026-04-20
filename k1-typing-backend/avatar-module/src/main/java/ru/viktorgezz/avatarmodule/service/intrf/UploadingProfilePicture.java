package ru.viktorgezz.avatarmodule.service.intrf;

import org.springframework.web.multipart.MultipartFile;

public interface UploadingProfilePicture {

    void uploadCustomImg(MultipartFile file);

    void uploadGeneratedImg(String prompt);
}
