const request = require('../../utils/request');
const app = getApp();

Page({
  data: { list: [], loading: true },

  onShow() {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/index' });
      return;
    }
    this.refresh();
  },

  refresh() {
    this.setData({ loading: true });
    return request.get('/api/appointments/mine')
      .then(list => this.setData({ list: list || [], loading: false }))
      .catch(() => this.setData({ loading: false }));
  },

  goNew() {
    wx.navigateTo({ url: '/pages/appointment/new' });
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/appointment/detail?id=' + e.currentTarget.dataset.id });
  },
});
