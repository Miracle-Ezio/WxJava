const request = require('./request');
const app = getApp();

/**
 * 完整登录流程：
 *   1. wx.login -> code
 *   2. 调用后端 /api/auth/wx-login
 *   3. 写入 token + customer 资料
 */
function loginByCode(profile = {}) {
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
