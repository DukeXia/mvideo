package com.mvideo.video.service.impl;

import com.github.pagehelper.PageHelper;
import com.mvideo.common.dto.PageQueryDto;
import com.mvideo.common.dto.PageResultDto;
import com.mvideo.common.util.PageUtil;
import com.mvideo.video.dal.dao.VideoCheckMapper;
import com.mvideo.video.dal.dao.VideoMapper;
import com.mvideo.video.dal.po.Video;
import com.mvideo.video.dal.po.VideoCheck;
import com.mvideo.video.dto.*;
import com.mvideo.video.util.PluploadUtil;
import com.mvideo.video.service.IVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
 * Created by admin on 16/12/7.
 */
@RestController
@RequestMapping("/video")
public class VideoServiceImpl implements IVideoService {

    @Autowired
    private PluploadUtil pluploadUtil;

    @Autowired
    private VideoCheckMapper videoCheckMapper;

    @Autowired
    private VideoMapper videoMapper;

    @RequestMapping("/checkUpload")
    public CheckResult checkUpload(CheckUpload checkUpload) throws Exception {
        String tmpFileName = "upload/" + checkUpload.getUserId() + checkUpload.getFilename() + "_tmp_";
        checkUpload.setFilename(tmpFileName);
        List<VideoCheck> videoChecks = videoCheckMapper.selectByTmpFileNameLimitOne(checkUpload);
        CheckResult checkResult = new CheckResult();
        if (videoChecks.size() != 0) {
            Integer currentChunk = videoChecks.get(0).getCurrentChunk();
            checkResult.setOffset((currentChunk + 1) * checkUpload.getChunk_size());
            checkResult.setMessage("exist");
        } else {
            checkResult.setMessage("not exist");
        }
        return checkResult;
    }

    @RequestMapping("/upload")
    public void upload(Plupload plupload, HttpServletRequest request) throws Exception {
        if (plupload.getChunk() >= plupload.getChunks()) {
            return;
        }
        String fileDir = "upload";
        plupload.setRequest(request);
        //文件存储绝对路径 request.getSession().getServletContext().getRealPath("/")
        File dir = new File(request.getContextPath() + fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        plupload.setName(plupload.getName().replaceAll(" ", ""));
        pluploadUtil.upload(plupload, dir, request);
    }

    @Override
    @RequestMapping("/getRecentlyVideos")
    public List<Video> getRecentlyVideos(PageQueryDto pageQueryDto) {
        PageHelper.startPage(pageQueryDto.getPageNum(), pageQueryDto.getPageSize());
        return videoMapper.getRecentlyVideos();
    }

    @Override
    @RequestMapping("/getOnlineVideos")
    public List<Video> getOnlineVideos() {
        PageHelper.startPage(1, 6);
        return videoMapper.getOnlineVideos();
    }

    @Override
    @RequestMapping("/getOnUpcomingChannels")
    public List<Video> getOnUpcomingChannels() {
        PageHelper.startPage(1, 10);
        return videoMapper.getOnUpcomingChannels();
    }

    @RequestMapping("/getHistoryVideo")
    public PageResultDto<Video> getHistoryVideo(VideoHistoryQueryDto videoHistoryQueryDto) {
        PageHelper.startPage(videoHistoryQueryDto.getPageNum(), videoHistoryQueryDto.getPageSize());
        List<Video> videos = videoMapper.getHistoryVideo(videoHistoryQueryDto.getUserId());
        return PageUtil.getPageResult(videos);
    }

    @RequestMapping("/search")
    public PageResultDto<Video> search(VideoSearchQueryDto videoSearchQueryDto) {
        PageHelper.startPage(videoSearchQueryDto.getPageNum(), videoSearchQueryDto.getPageSize());
        List<Video> videos = videoMapper.search(videoSearchQueryDto.getKeyWord());
        return PageUtil.getPageResult(videos);
    }

}
