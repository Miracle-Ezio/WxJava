package com.starry.mb.photo;

import com.starry.mb.common.web.ApiResponse;
import com.starry.mb.photo.dto.CompareVO;
import com.starry.mb.photo.dto.PhotoTimelineVO;
import com.starry.mb.photo.dto.PhotoVO;
import com.starry.mb.photo.dto.UploadPhotoRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * 上传一张照片。multipart/form-data：
     *   - file:           二进制文件
     *   - pose:           1~6
     *   - bodyPart:       可选
     *   - visibility:     1/2/3，默认 2
     *   - remark:         可选
     */
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<PhotoVO> upload(@RequestPart("file") MultipartFile file,
                                       @ModelAttribute UploadPhotoRequest req) {
        return ApiResponse.ok(photoService.upload(file, req));
    }

    /** 时间轴（按月分组）。pose 可选，传则只看该机位。 */
    @GetMapping("/timeline")
    public ApiResponse<List<PhotoTimelineVO>> timeline(
            @RequestParam(required = false) Integer pose) {
        return ApiResponse.ok(photoService.timeline(pose));
    }

    /** 同机位"首张 vs 最新"快捷对比。 */
    @GetMapping("/compare/quick")
    public ApiResponse<CompareVO> quickCompare(@RequestParam int pose) {
        return ApiResponse.ok(photoService.quickCompare(pose));
    }

    /** 指定两张照片对比。 */
    @GetMapping("/compare")
    public ApiResponse<CompareVO> compare(@RequestParam long before,
                                          @RequestParam long after) {
        return ApiResponse.ok(photoService.compareTwo(before, after));
    }

    @GetMapping("/{id}")
    public ApiResponse<PhotoVO> detail(@PathVariable long id) {
        return ApiResponse.ok(photoService.detail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        photoService.softDelete(id);
        return ApiResponse.ok();
    }

    /** 锁定 / 解锁（员工不可见）。 */
    @PostMapping("/{id}/lock")
    public ApiResponse<Void> lock(@PathVariable long id,
                                  @RequestParam(defaultValue = "true") boolean lock) {
        photoService.toggleLock(id, lock);
        return ApiResponse.ok();
    }
}
