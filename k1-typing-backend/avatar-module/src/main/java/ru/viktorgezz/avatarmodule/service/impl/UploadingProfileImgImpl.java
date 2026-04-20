package ru.viktorgezz.avatarmodule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.viktorgezz.avatarmodule.client.ImageGeneration;
import ru.viktorgezz.avatarmodule.dto.ImageDto;
import ru.viktorgezz.avatarmodule.exception.BusinessException;
import ru.viktorgezz.avatarmodule.exception.ErrorCode;
import ru.viktorgezz.avatarmodule.service.intrf.ProfileImgCommandService;
import ru.viktorgezz.avatarmodule.service.intrf.UploadingProfilePicture;

import java.io.IOException;

import static ru.viktorgezz.avatarmodule.util.CurrentUserUtils.getCurrentIdUser;

@Service
@RequiredArgsConstructor
public class UploadingProfileImgImpl implements UploadingProfilePicture {

    private final ProfileImgCommandService profileImgCommandService;
    private final ImageGeneration imageGeneration;

    @Override
    public void uploadCustomImg(MultipartFile file) {
        final Long idUser = getCurrentIdUser();
        final long coastUploading = 2L;

        final String contentType = file.getContentType();
        if (contentType == null || !isSupportedType(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        ImageDto imageDto;
        try {
            imageDto = new ImageDto(
                    file.getBytes(),
                    contentType
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_PROCESSING_ERROR);
        }

        profileImgCommandService.setAvatar(idUser, coastUploading, imageDto);
    }

    @Override
    public void uploadGeneratedImg(String prompt) {
        final Long idUser = getCurrentIdUser();
        final long coastGeneration = 4L;

        ImageDto imageDto = imageGeneration.generateImage(idUser, prompt);
        profileImgCommandService.setAvatar(idUser, coastGeneration, imageDto);
    }

    private static boolean isSupportedType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif");
    }
}
