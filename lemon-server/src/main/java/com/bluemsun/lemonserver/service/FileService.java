package com.bluemsun.lemonserver.service;

import ch.qos.logback.classic.model.LoggerModel;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.exception.InvalidFileException;
import com.bluemsun.lemonpojo.entity.MyFile;
import com.bluemsun.lemonpojo.vo.FileVO;
import com.bluemsun.lemonpojo.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static java.io.File.separator;

public interface FileService {

    /**
     * 检查文件名并获取文件类型
     * @param file spring文件
     * @return 文件类型
     */
    static String checkFileName(MultipartFile file){
        // TODO 增加可选的扩展名识别
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".") || fileName.contains(separator)){
            throw new InvalidFileException(MessageConstant.INVALID_FILE);
        }
        // 按 . 分割
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

    PictureVO savePic(MultipartFile file, long userId) throws IOException;

    FileVO saveFile(MultipartFile file, Long userId) throws IOException;

    long getOwner(int fileId);

    void removeFile(int fileId);

    MyFile getFile(int fileId);

    FileVO[] getBlogFile(int blogId);
    void updateBlogId(long userId, int[] files, int blogId);
}
