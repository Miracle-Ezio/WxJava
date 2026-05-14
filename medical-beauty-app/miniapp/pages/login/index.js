const auth = require('../../utils/auth');

Page({
  data: { loading: false },

  onGetUserInfo(e) {
    // 注：自 2022 起 getUserInfo 仅返回匿名信息；
    // 真实昵称 / 头像应在登录后引导用户通过 chooseAvatar / nickName input 完善
    const profile = (e.detail && e.detail.userInfo) || {};
    this.doLogin(profile);
  },

  doLogin(profile) {
    this.setData({ loading: true });
    auth.loginByCode(profile)
      .then(data => {
        wx.showToast({
          title: data.newCustomer ? '欢迎加入 STARRY' : '欢迎回来',
          icon: 'none',
        });
        wx.switchTab({ url: '/pages/home/index' });
      })
      .catch(err => {
        console.error(err);
      })
      .finally(() => this.setData({ loading: false }));
  },

  onProtocol() {
    wx.showModal({
      title: '协议',
      content: '一期演示版，协议页未实现。',
      showCancel: false,
    });
  },
});
