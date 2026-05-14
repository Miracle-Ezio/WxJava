const request = require('../../utils/request');
const { POSES } = require('../../utils/poses');
const app = getApp();

Page({
  data: {
    poses: POSES,
    poseFilter: null,     // null 表示全部
    timeline: [],
    quickCompare: null,
    loading: true,
  },

  onShow() {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/index' });
      return;
    }
    this.refresh();
  },

  onPullDownRefresh() {
    this.refresh().finally(() => wx.stopPullDownRefresh());
  },

  refresh() {
    this.setData({ loading: true });
    return Promise.all([
      this.loadTimeline(),
      this.loadQuickCompare(),
    ]).finally(() => this.setData({ loading: false }));
  },

  loadTimeline() {
    const pose = this.data.poseFilter;
    return request.get('/api/photos/timeline' + (pose ? '?pose=' + pose : ''))
      .then(data => this.setData({ timeline: data || [] }))
      .catch(() => this.setData({ timeline: [] }));
  },

  loadQuickCompare() {
    // 默认拿"正面"机位的首张 vs 最新
    return request.get('/api/photos/compare/quick?pose=1')
      .then(data => this.setData({ quickCompare: data || null }))
      .catch(() => this.setData({ quickCompare: null }));
  },

  onPoseFilter(e) {
    const raw = e.currentTarget.dataset.code;
    const code = raw === '' ? null : Number(raw);
    this.setData({ poseFilter: code });
    this.loadTimeline();
  },

  goUpload() {
    wx.navigateTo({ url: '/pages/photos/upload' });
  },

  onPhotoTap(e) {
    const id = e.currentTarget.dataset.id;
    // 单张大图预览：从 timeline 里收集所有 URL
    const urls = [];
    let current = '';
    this.data.timeline.forEach(g => g.photos.forEach(p => {
      urls.push(p.url);
      if (p.id === id) current = p.url;
    }));
    wx.previewImage({ urls, current });
  },

  onPhotoLongPress(e) {
    const id = e.currentTarget.dataset.id;
    wx.showActionSheet({
      itemList: ['作为"对比 A"', '作为"对比 B"', '锁定 / 解锁', '删除'],
      success: r => {
        if (r.tapIndex === 0)      this.pickForCompare(id, 'before');
        else if (r.tapIndex === 1) this.pickForCompare(id, 'after');
        else if (r.tapIndex === 2) this.toggleLock(id);
        else if (r.tapIndex === 3) this.deletePhoto(id);
      },
    });
  },

  pickForCompare(id, slot) {
    const pair = wx.getStorageSync('compare-pair') || {};
    pair[slot] = id;
    wx.setStorageSync('compare-pair', pair);
    if (pair.before && pair.after && pair.before !== pair.after) {
      wx.removeStorageSync('compare-pair');
      wx.navigateTo({
        url: `/pages/photos/compare?before=${pair.before}&after=${pair.after}`,
      });
    } else {
      wx.showToast({
        title: slot === 'before' ? '已选为 A，再长按另一张选 B' : '已选为 B，再长按另一张选 A',
        icon: 'none',
      });
    }
  },

  toggleLock(id) {
    request.post(`/api/photos/${id}/lock?lock=true`)
      .then(() => {
        wx.showToast({ title: '已锁定', icon: 'none' });
        this.loadTimeline();
      });
  },

  deletePhoto(id) {
    wx.showModal({
      title: '删除照片',
      content: '删除后 90 天内可联系顾问找回',
      success: r => {
        if (!r.confirm) return;
        request.del(`/api/photos/${id}`)
          .then(() => {
            wx.showToast({ title: '已删除', icon: 'none' });
            this.refresh();
          });
      },
    });
  },

  goCompare(e) {
    const { before, after } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/photos/compare?before=${before}&after=${after}` });
  },
});
