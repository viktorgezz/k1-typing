package ru.viktorgezz.avatarmodule.client;

import ru.viktorgezz.avatarmodule.dto.ImageDto;

public interface ImageGeneration {

    ImageDto generateImage(Long idUser, String prompt);
}
