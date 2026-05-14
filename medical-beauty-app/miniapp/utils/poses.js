/**
 * 4 个固定机位 + 2 个补充机位。
 * code 与后端 com.starry.mb.photo.domain.Pose 保持一致。
 */
const POSES = [
  {
    code: 1,
    key: 'front',
    label: '正面',
    desc: '保持下巴与镜头水平',
    /** 蒙版 SVG 路径（用于拍照引导） */
    maskType: 'oval',
  },
  {
    code: 2,
    key: 'left45',
    label: '左 45°',
    desc: '面部向左转 45 度',
    maskType: 'oval-left',
  },
  {
    code: 3,
    key: 'right45',
    label: '右 45°',
    desc: '面部向右转 45 度',
    maskType: 'oval-right',
  },
  {
    code: 4,
    key: 'top',
    label: '顶光',
    desc: '微低头，自然光源在头顶上方',
    maskType: 'oval',
  },
];

const POSE_BY_CODE = POSES.reduce((m, p) => { m[p.code] = p; return m; }, {});

module.exports = { POSES, POSE_BY_CODE };
