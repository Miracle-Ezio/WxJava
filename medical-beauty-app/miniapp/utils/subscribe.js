/**
 * 订阅消息授权封装。
 *
 * 注意：真实模板 ID 需在微信公众平台「小程序 / 订阅消息」中申请通过后填入。
 * 演示阶段把占位 TPL_* 当成"已知模板"模拟流程，真机调用 wx.requestSubscribeMessage
 * 时若 ID 无效会被微信拒绝，但流程不会崩。
 */

const TEMPLATES = {
  appointmentConfirmed: 'TPL_APPT_CONFIRMED',
  reminder24h:          'TPL_REMINDER_24H',
  reminder2h:           'TPL_REMINDER_2H',
  planPushed:           'TPL_PLAN_PUSHED',
};

/**
 * 一键引导订阅多条模板。微信限制一次最多 3 条。
 * @param  {string[]} ids 模板 ID 数组
 * @returns Promise，永远 resolve（拒绝授权也不阻塞业务）
 */
function request(ids) {
  return new Promise(resolve => {
    if (!ids || ids.length === 0) { resolve({}); return; }
    if (!wx.requestSubscribeMessage) { resolve({}); return; }
    wx.requestSubscribeMessage({
      tmplIds: ids.slice(0, 3),
      success: r => resolve(r),
      fail:    () => resolve({}),
    });
  });
}

/** 客户下单后调一次：让 TA 授权"预约确认 + 到店提醒"。 */
function requestForAppointment() {
  return request([
    TEMPLATES.appointmentConfirmed,
    TEMPLATES.reminder24h,
    TEMPLATES.reminder2h,
  ]);
}

/** 客户登录后调一次：让 TA 授权"规划方案推送"。 */
function requestForPlanPushed() {
  return request([TEMPLATES.planPushed]);
}

module.exports = { TEMPLATES, request, requestForAppointment, requestForPlanPushed };
