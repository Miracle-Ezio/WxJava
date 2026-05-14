package com.starry.mb.photo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starry.mb.common.context.PrincipalContext;
import com.starry.mb.common.exception.BizException;
import com.starry.mb.photo.domain.CustomerPhoto;
import com.starry.mb.photo.domain.Pose;
import com.starry.mb.photo.dto.CompareVO;
import com.starry.mb.photo.dto.PhotoTimelineVO;
import com.starry.mb.photo.dto.PhotoVO;
import com.starry.mb.photo.dto.UploadPhotoRequest;
import com.starry.mb.photo.mapper.CustomerPhotoMapper;
import com.starry.mb.storage.StorageProperties;
import com.starry.mb.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PhotoService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final long MAX_PHOTO_SIZE = 20L * 1024 * 1024; // 20 MB

    private final CustomerPhotoMapper photoMapper;
    private final StorageService storage;
    private final StorageProperties storageProps;

    public PhotoService(CustomerPhotoMapper photoMapper,
                        StorageService storage,
                        StorageProperties storageProps) {
        this.photoMapper = photoMapper;
        this.storage = storage;
        this.storageProps = storageProps;
    }

    // ─────────────────────────────────────────────────────────
    //  上传（后端中转）
    // ─────────────────────────────────────────────────────────

    @Transactional
    public PhotoVO upload(MultipartFile file, UploadPhotoRequest req) {
        if (file == null || file.isEmpty()) throw new BizException(40000, "请选择照片");
        if (file.getSize() > MAX_PHOTO_SIZE) throw new BizException(40000, "照片不能超过 20MB");
        Pose.ofCode(req.getPose());

        Long tid = requireTenant();
        Long cid = requireCustomerId();

        String ext = pickExt(file.getOriginalFilename(), file.getContentType());
        LocalDate today = LocalDate.now();
        String objectKey = String.format("tenants/%d/customers/%d/%04d/%02d/%s.%s",
                tid, cid, today.getYear(), today.getMonthValue(),
                UUID.randomUUID().toString().replace("-", ""),
                ext);

        try (var in = file.getInputStream()) {
            storage.upload(objectKey, in, file.getSize(),
                    file.getContentType() == null ? "image/jpeg" : file.getContentType());
        } catch (IOException e) {
            throw new BizException(50000, "上传失败：" + e.getMessage());
        }

        CustomerPhoto p = new CustomerPhoto();
        p.setCustomerId(cid);
        p.setPose(req.getPose());
        p.setBodyPart(req.getBodyPart());
        p.setShotAt(LocalDateTime.now());
        p.setObjectKey(objectKey);
        p.setSizeBytes((int) file.getSize());
        p.setTreatmentRecordId(req.getTreatmentRecordId());
        p.setUploadedBy(1);
        p.setVisibility(req.getVisibility() == null ? 2 : req.getVisibility());
        p.setLocked(0);
        p.setRemark(req.getRemark());
        photoMapper.insert(p);

        return toVO(p);
    }

    private String pickExt(String filename, String contentType) {
        String name = filename == null ? "" : filename.toLowerCase();
        for (String ext : new String[] {"jpg", "jpeg", "png", "heic", "webp"}) {
            if (name.endsWith("." + ext)) return ext;
        }
        if (contentType != null) {
            return switch (contentType) {
                case "image/png"  -> "png";
                case "image/heic" -> "heic";
                case "image/webp" -> "webp";
                default -> "jpg";
            };
        }
        return "jpg";
    }

    // ─────────────────────────────────────────────────────────
    //  查询
    // ─────────────────────────────────────────────────────────

    /** 按月分组的时间轴；当 pose 给定时只取该机位。 */
    public List<PhotoTimelineVO> timeline(Integer pose) {
        Long tid = requireTenant();
        Long cid = requireCustomerId();

        LambdaQueryWrapper<CustomerPhoto> q = new LambdaQueryWrapper<CustomerPhoto>()
                .eq(CustomerPhoto::getTenantId, tid)
                .eq(CustomerPhoto::getCustomerId, cid)
                .orderByDesc(CustomerPhoto::getShotAt);
        if (pose != null) q.eq(CustomerPhoto::getPose, pose);
        List<CustomerPhoto> photos = photoMapper.selectList(q);

        Map<String, List<PhotoVO>> grouped = new LinkedHashMap<>();
        for (CustomerPhoto p : photos) {
            String month = p.getShotAt().format(MONTH_FMT);
            grouped.computeIfAbsent(month, k -> new ArrayList<>()).add(toVO(p));
        }
        List<PhotoTimelineVO> result = new ArrayList<>(grouped.size());
        grouped.forEach((m, list) -> {
            PhotoTimelineVO vo = new PhotoTimelineVO();
            vo.setMonth(m);
            vo.setPhotos(list);
            result.add(vo);
        });
        return result;
    }

    /** 同机位首张 vs 最新一张的快捷对比。 */
    public CompareVO quickCompare(int pose) {
        Long tid = requireTenant();
        Long cid = requireCustomerId();
        Pose.ofCode(pose);

        CustomerPhoto first = photoMapper.selectOne(new LambdaQueryWrapper<CustomerPhoto>()
                .eq(CustomerPhoto::getTenantId, tid)
                .eq(CustomerPhoto::getCustomerId, cid)
                .eq(CustomerPhoto::getPose, pose)
                .orderByAsc(CustomerPhoto::getShotAt)
                .last("LIMIT 1"));
        CustomerPhoto latest = photoMapper.selectOne(new LambdaQueryWrapper<CustomerPhoto>()
                .eq(CustomerPhoto::getTenantId, tid)
                .eq(CustomerPhoto::getCustomerId, cid)
                .eq(CustomerPhoto::getPose, pose)
                .orderByDesc(CustomerPhoto::getShotAt)
                .last("LIMIT 1"));

        CompareVO vo = new CompareVO();
        vo.setMode("first-vs-latest");
        if (first != null)  vo.setBefore(toVO(first));
        if (latest != null && (first == null || !Objects.equals(latest.getId(), first.getId()))) {
            vo.setAfter(toVO(latest));
        }
        return vo;
    }

    /** 任意两张照片对比。 */
    public CompareVO compareTwo(long beforeId, long afterId) {
        CompareVO vo = new CompareVO();
        vo.setBefore(detail(beforeId));
        vo.setAfter(detail(afterId));
        vo.setMode("custom");
        return vo;
    }

    public PhotoVO detail(long id) {
        Long tid = requireTenant();
        CustomerPhoto p = photoMapper.selectById(id);
        if (p == null || p.getDeletedAt() != null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "照片不存在");
        }
        Long cid = PrincipalContext.customerId();
        if (cid != null && !p.getCustomerId().equals(cid)) {
            throw new BizException(40300, "无权访问");
        }
        if (cid != null && Integer.valueOf(1).equals(p.getLocked())) {
            throw new BizException(40300, "照片已锁定");
        }
        return toVO(p);
    }

    // ─────────────────────────────────────────────────────────
    //  删除 / 锁定
    // ─────────────────────────────────────────────────────────

    public void softDelete(long id) {
        Long tid = requireTenant();
        CustomerPhoto p = photoMapper.selectById(id);
        if (p == null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "照片不存在");
        }
        Long cid = PrincipalContext.customerId();
        if (cid != null && !p.getCustomerId().equals(cid)) {
            throw new BizException(40300, "无权删除");
        }
        photoMapper.deleteById(id);
        // COS 对象保留 90 天由调度任务硬删
    }

    public void toggleLock(long id, boolean lock) {
        Long tid = requireTenant();
        CustomerPhoto p = photoMapper.selectById(id);
        if (p == null || !p.getTenantId().equals(tid)) {
            throw new BizException(40400, "照片不存在");
        }
        Long cid = PrincipalContext.customerId();
        if (cid == null || !p.getCustomerId().equals(cid)) {
            throw new BizException(40300, "仅本人可锁定 / 解锁");
        }
        p.setLocked(lock ? 1 : 0);
        photoMapper.updateById(p);
    }

    // ─────────────────────────────────────────────────────────
    //  内部
    // ─────────────────────────────────────────────────────────

    private PhotoVO toVO(CustomerPhoto p) {
        PhotoVO vo = new PhotoVO();
        vo.setId(p.getId());
        vo.setCustomerId(p.getCustomerId());
        vo.setPose(p.getPose());
        vo.setPoseLabel(Pose.ofCode(p.getPose()).getLabel());
        vo.setBodyPart(p.getBodyPart());
        vo.setShotAt(p.getShotAt());
        vo.setWidth(p.getWidth());
        vo.setHeight(p.getHeight());
        vo.setVisibility(p.getVisibility());
        vo.setLocked(p.getLocked());
        vo.setRemark(p.getRemark());

        long ttl = storageProps.getSignedUrlTtlSeconds();
        if (storage.isReady()) {
            vo.setUrl(storage.signedGetUrl(p.getObjectKey(), ttl));
            if (p.getThumbKey() != null) {
                vo.setThumbUrl(storage.signedGetUrl(p.getThumbKey(), ttl));
            }
        }
        return vo;
    }

    private Long requireTenant() {
        Long tid = PrincipalContext.tenantId();
        if (tid == null) throw new BizException(40100, "未登录");
        return tid;
    }

    private Long requireCustomerId() {
        Long cid = PrincipalContext.customerId();
        if (cid == null) throw new BizException(40300, "仅客户可操作");
        return cid;
    }
}
