const request = require('../../utils/request');
const { POSES } = require('../../utils/poses');

Page({
  data: {
    poses: POSES,
    currentPoseIdx: 0,
    currentPose: POSES[0],
    cameraFacing: 'front',
    previewPath: '',
    /** 同机位最新一张的签名 URL，作为对齐 ghost */
    lastPhotoUrl: '',
    uploading: false,
  },

  onLoad(query) {
    if (query.pose) {
      const idx = POSES.findIndex(p => p.code === Number(query.pose));
      if (idx >= 0) this.applyPose(idx);
    } else {
      this.applyPose(0);
    }
  },

  applyPose(idx) {
    const pose = POSES[idx];
    this.setData({
      currentPoseIdx: idx,
      currentPose: pose,
      previewPath: '',
    });
    // 拉同机位最新一张做对齐蒙版
    request.get('/api/photos/timeline?pose=' + pose.code)
      .then(groups => {
        let url = '';
        if (groups && groups.length && groups[0].photos.length) {
          url = groups[0].photos[0].url;
        }
        this.setData({ lastPhotoUrl: url });
      })
      .catch(() => this.setData({ lastPhotoUrl: '' }));
  },

  onSelectPose(e) {
    this.applyPose(Number(e.currentTarget.dataset.idx));
  },

  onFlipCamera() {
    this.setData({
      cameraFacing: this.data.cameraFacing === 'front' ? 'back' : 'front',
    });
  },

  onShutter() {
    const ctx = wx.createCameraContext();
    ctx.takePhoto({
      quality: 'high',
      success: r => this.setData({ previewPath: r.tempImagePath }),
      fail: err => {
        console.error('takePhoto failed', err);
        wx.showToast({ title: '拍照失败，请检查相机权限', icon: 'none' });
      },
    });
  },

  onPickFromAlbum() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album'],
      sizeType: ['original'],
      success: r => {
        if (r.tempFiles && r.tempFiles[0]) {
          this.setData({ previewPath: r.tempFiles[0].tempFilePath });
        }
      },
    });
  },

  onRetake() {
    this.setData({ previewPath: '' });
  },

  onConfirm() {
    if (!this.data.previewPath) return;
    this.setData({ uploading: true });

    request.upload('/api/photos', this.data.previewPath, {
      pose: this.data.currentPose.code,
      visibility: 2,
    })
      .then(() => {
        wx.showToast({ title: '已记录这一刻 ✨', icon: 'none' });
        setTimeout(() => wx.navigateBack(), 800);
      })
      .catch(() => this.setData({ uploading: false }));
  },

  onCameraError(e) {
    console.error('camera error', e);
    wx.showModal({
      title: '相机不可用',
      content: '请在系统设置中允许小程序使用摄像头，或从相册选择照片',
      confirmText: '从相册选',
      success: r => { if (r.confirm) this.onPickFromAlbum(); },
    });
  },

  onClose() {
    wx.navigateBack();
  },
});
