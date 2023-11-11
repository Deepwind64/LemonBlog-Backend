package com.bluemsun.lemonserver.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.constant.UrlConstant;
import com.bluemsun.lemoncommon.exception.ResourceNotFoundException;
import com.bluemsun.lemonpojo.entity.MyFile;
import com.bluemsun.lemonpojo.entity.Picture;
import com.bluemsun.lemonpojo.vo.PictureVO;
import com.bluemsun.lemonpojo.vo.FileVO;
import com.bluemsun.lemonserver.dao.FileMapper;
import com.bluemsun.lemonserver.dao.PictureMapper;
import jakarta.annotation.Resource;
import com.bluemsun.lemonserver.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author deepwind
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Resource
    PictureMapper pictureMapper;
    @Resource
    FileMapper fileMapper;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Value("${file.picpath}")
    private String picPath;
    @Value("${file.path}")
    private String filePath;
    @Value("${server.location}")
    public String serverLocation;

    private Picture getRepeatPic(byte[] bytes) {
        String hash = DigestUtil.md5Hex(bytes);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("picture_hash",hash);
        return pictureMapper.selectOne(queryWrapper);
    }

    private MyFile getRepeatFile(byte[] bytes, long userId) {
        String hash = DigestUtil.md5Hex(bytes);
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_hash",hash);
        queryWrapper.eq("user_id",userId);
        return fileMapper.selectOne(queryWrapper);
    }
    private byte[] getFileContent(MultipartFile file) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(file.getInputStream(), os);
        return os.toByteArray();
    }
    @Override
    public PictureVO savePic(MultipartFile file, long userId) throws IOException {
        String fileType = FileService.checkFileName(file);
        byte[] fileContent = getFileContent(file);

        // 保存图片至硬盘并存图片信息入数据库
        Picture picture = getRepeatPic(fileContent);

        if (picture==null){
            // 产生唯一文件名
            String fileUid = IdUtil.simpleUUID();
            // 保存文件
            log.info(picPath + fileUid + "." + fileType);
            File newFile = new File(picPath, fileUid + "." + fileType);
            FileUtil.writeBytes(fileContent, newFile);
            // 在数据库存入信息
            picture = Picture.builder()
                    .name(fileUid)
                    .type(fileType)
                    .hash(DigestUtil.md5Hex(fileContent))
                    .userId(userId)
                    .build();
            pictureMapper.insert(picture);
        }
        // TODO 实现废弃图片自动清理
//        // 保存文件临时列表的Redis键值对，Key为"TEMP_FILE"+文件hash，值为文件id，保存时间12小时
//        // 用于比较无效文件的hash表，表内数据不会失效，以此找出无效文件hashKey为文件hash，值为文件id
//        redisTemplate.opsForValue().set(RedisConstant.TEMP_FILE + hash, myFile.getId().toString(),12, TimeUnit.HOURS);
//        redisTemplate.opsForHash().put(RedisConstant.MAP_TEMP_FILE, hash, myFile.getId().toString());

        // 图片的所有者信息无所谓
        //返回VO对象
        return PictureVO.builder()
                .id(picture.getId())
                .url(serverLocation + UrlConstant.PICTURE_LOCATION + picture.getName() + "." +picture.getType()).build();
    }

    @Override
    public FileVO saveFile(MultipartFile file, Long userId) throws IOException {
        String fileType = FileService.checkFileName(file);
        byte[] fileContent = getFileContent(file);
        MyFile myFile = getRepeatFile(fileContent,userId);
        if (myFile==null){
            // 产生唯一文件名
            String fileUid = IdUtil.simpleUUID();
            // 保存文件
            log.info(filePath + fileUid + "." + fileType);
            File newFile = new File(filePath, fileUid + "." + fileType);
            FileUtil.writeBytes(fileContent, newFile);
            String hash = DigestUtil.md5Hex(fileContent);
            // 在数据库存入信息
            myFile = MyFile.builder()
                    .name(file.getOriginalFilename())
                    .uid(fileUid)
                    .type(fileType)
                    .hash(hash)
                    .userId(userId)
                    .build();
            fileMapper.insert(myFile);
        }
        // 创建包含新所有者的记录
        if(!myFile.getUserId().equals(userId)){
            myFile.setId(null);
            myFile.setUserId(userId);
            fileMapper.insert(myFile);
        }
        return FileVO.builder()
                .id(myFile.getId())
                .filename(myFile.getName())
                .url(getFileURL(myFile)).build();
    }

    @Override
    public long getOwner(int fileId) {
        return getFile(fileId).getUserId();
    }

    @Override
    public MyFile getFile(int fileId) {
        MyFile myFile = fileMapper.selectById(fileId);
        if (myFile==null) {
            throw new ResourceNotFoundException(MessageConstant.RES_NOT_FOUND);
        }
        return myFile;

    }
    String getFileURL(MyFile file){
        return serverLocation + UrlConstant.FILE_LOCATION + file.getUid() + "." +file.getType();
    }

    @Override
    public void removeFile(int fileId) {
        MyFile myFile = getFile(fileId);
        File file = FileUtil.file(filePath, myFile.getUid() + "." + myFile.getType());
        FileUtil.del(file);
        log.info("文件{}，已被删除", file.getAbsolutePath());
        fileMapper.deleteById(fileId);

    }

    @Override
    public FileVO[] getBlogFile(int blogId) {
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("blog_id", blogId);
        List<FileVO> files = new ArrayList<>();
        for(var file: fileMapper.selectList(queryWrapper)){
            files.add(FileVO.builder()
                    .id(file.getId())
                    .url(getFileURL(file))
                    .filename(file.getName()).build());
        }
        return files.toArray(new FileVO[0]);
    }

    @Override
    public void updateBlogId(long userId, int[] files, int blogId) {
        for(int fileId: files){
            // 检查文件是否归该用户所有，且为未设置博客状态
            MyFile myFile = getFile(fileId);
            if(myFile!=null && myFile.getUserId()==userId && myFile.getBlogId()==0){
                fileMapper.updateById(MyFile.builder().id(fileId).blogId(blogId).build());
            }
        }
    }
}
