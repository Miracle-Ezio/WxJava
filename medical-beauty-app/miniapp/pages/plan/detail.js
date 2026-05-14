const request = require('../../utils/request');

Page({
  data: {
    detail: null,
    currentAnchor: null,
  },

  onLoad(query) {
    const id = query.id;
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    request.get('/api/plans/' + id)
      .then(detail => {
        const currentAnchor = detail.sections && detail.sections[0] ? detail.sections[0].id : null;
        this.setData({ detail, currentAnchor });
      })
      .catch(() => {});
  },

  onAnchorTap(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({ currentAnchor: id });
    wx.pageScrollTo({
      selector: '#anchor-' + id,
      duration: 250,
      offsetTop: -80,
    });
  },

  onExportPdf() {
    wx.showToast({ title: 'PDF 导出（二期）', icon: 'none' });
  },

  onContactConsultant() {
    wx.showToast({ title: '联系顾问（待接入企微/电话）', icon: 'none' });
  },
});
