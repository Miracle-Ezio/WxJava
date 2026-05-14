const request = require('../../utils/request');

Page({
  data: { plans: [], loading: true },

  onLoad() {
    request.get('/api/plans/mine')
      .then(plans => this.setData({ plans: plans || [], loading: false }))
      .catch(() => this.setData({ loading: false }));
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/plan/detail?id=' + e.currentTarget.dataset.id });
  },
});
