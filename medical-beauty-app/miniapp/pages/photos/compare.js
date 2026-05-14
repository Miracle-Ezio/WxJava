const request = require('../../utils/request');

Page({
  data: {
    before: null,
    after: null,
    splitX: 50,       // 百分比 0~100
    stageWidth: 375,  // px，舞台宽度（用于上层图缩放）
    mode: 'slider',
  },

  onLoad(query) {
    const beforeId = query.before;
    const afterId = query.after;
    if (!beforeId || !afterId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }

    // 拿到舞台宽度
    const sys = wx.getSystemInfoSync();
    this.setData({ stageWidth: sys.windowWidth });

    request.get(`/api/photos/compare?before=${beforeId}&after=${afterId}`)
      .then(data => {
        if (!data || !data.before || !data.after) {
          wx.showToast({ title: '照片不存在', icon: 'none' });
          return;
        }
        if (data.before.pose !== data.after.pose) {
          wx.showToast({ title: '机位不同，对比效果可能受影响', icon: 'none', duration: 1800 });
        }
        this.setData({ before: data.before, after: data.after });
      });
  },

  onModeChange(e) {
    this.setData({ mode: e.currentTarget.dataset.mode });
  },

  onTouchStart(e) { this.updateSplit(e); },
  onTouchMove(e)  { this.updateSplit(e); },

  updateSplit(e) {
    if (!e.touches || !e.touches[0]) return;
    const x = e.touches[0].clientX;
    const w = this.data.stageWidth || 375;
    let pct = (x / w) * 100;
    pct = Math.max(0, Math.min(100, pct));
    this.setData({ splitX: pct });
  },
});
