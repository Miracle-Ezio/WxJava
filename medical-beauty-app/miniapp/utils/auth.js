const request = require('./request');
const app = getApp();

/**
 * 完整登录流程：
 *   1. wx.login -> code
 *   2. 调用后端 /api/auth/wx-login
 *   3. 写入 token + customer 资料
 *
 * mockMode：跳过 wx.login，直接调用被 mock 拦截的请求路径，
 *           保证演示模式下登录流程依然走得通。
 */
function loginByCode(profile = {}) {
  if (app.globalData.mockMode) {
    return request.post('/api/auth/wx-login', {
      code: 'mock-code',
      nickname: profile.nickName || '',
      avatarUrl: profile.avatarUrl || '',
    }, { silentAuth: true }).then(data => {
      app.setSession(data);
      return data;
    });
  }

  return new Promise((resolve, reject) => {
    wx.login({
      success: ({ code }) => {
        if (!code) {
          reject(new Error('wx.login 未返回 code'));
          return;
        }
        request.post('/api/auth/wx-login', {
          code,
          nickname: profile.nickName,
          avatarUrl: profile.avatarUrl,
          gender: profile.gender,
        }, { silentAuth: true })
          .then(data => {
            app.setSession(data);
            resolve(data);
          })
          .catch(reject);
      },
      fail: reject,
    });
  });
}

function ensureLogin() {
  return new Promise((resolve, reject) => {
    if (app.isLoggedIn()) {
      resolve();
      return;
    }
    wx.reLaunch({ url: '/pages/login/index' });
    reject(new Error('未登录'));
  });
}

module.exports = { loginByCode, ensureLogin };
