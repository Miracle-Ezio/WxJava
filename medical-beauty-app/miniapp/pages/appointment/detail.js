const request = require('../../utils/request');

Page({
  data: { detail: null, id: null },

  onLoad(query) {
    this.setData({ id: query.id });
    this.load();
  },

  load() {
    return request.get('/api/appointments/' + this.data.id)
      .then(detail => this.setData({ detail }))
      .catch(() => {});
  },

  onCancel() {
    wx.showModal({
      title: '取消预约',
      content: '确认取消这次预约？最迟可在开始前 2 小时取消',
      confirmText: '取消预约',
      cancelText: '再想想',
      success: r => {
        if (!r.confirm) return;
        request.post('/api/appointments/' + this.data.id + '/cancel', { reason: '客户主动取消' })
          .then(() => {
            wx.showToast({ title: '已取消', icon: 'none' });
            this.load();
          });
      },
    });
  },
});
