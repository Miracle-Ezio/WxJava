/**
 * Mock 演示数据 + 路由匹配。
 * 仅在 app.globalData.mockMode === true 时启用。
 *
 * 数据完全照 PPT 彭蕾抗衰整体规划方案，便于产品演示。
 */

// ─────────────────────────────────────────────────────────
//  数据
// ─────────────────────────────────────────────────────────

const LOGIN = {
  token: 'mock-jwt-token',
  customerId: 1,
  nickname: '彭蕾',
  avatarUrl: '',
  levelCode: 'GOLD',
  newCustomer: false,
};

const PLAN_SUMMARY = {
  id: 1,
  title: '彭蕾 · 抗衰整体规划方案',
  subtitle: '面部抗衰 · 皮肤管理 · 长期跟踪',
  coverUrl: '',
  status: 4,                 // 执行中
  validFrom: '2025-05-04',
  validTo: '2026-05-04',
  totalPrice: '81160.00',
  discountPrice: '75180.00',
  pushedAt: '2025-05-04T14:30:00',
  totalItems: 13,
  doneItems: 4,
};

const PLAN_DETAIL = {
  plan: {
    ...PLAN_SUMMARY,
    consultantId: 1,
    analysisText:
      '## 面部皮肤状况分析\n\n**优势**：皮肤紧致度相对较好、五官精致，角质层健康程度良好。\n\n' +
      '**劣势**：毛孔粗大、肤色暗黄、色素沉淀形成皮肤底层色斑严重、皱纹加重、皮肤松弛、衰老快。\n\n' +
      '## 面部衰老状况分析\n\n**劣势**：三庭比例上庭偏短，眼周衰老造成双眼皮松垂、眼周细纹增多、' +
      '眼眶骨流失、眼睛微凸；口周衰老、口角囊袋明显、下巴下颌骨衔接不流畅；外轮廓不清晰、' +
      '中面部松垂、苹果肌下移、法令纹加深、太阳穴凹陷。',
  },
  consultantName: '沈妍希',
  sections: [
    { id: 1, sort: 1, type: 'analysis',     title: '面部状况分析及抗衰规划', content: '' },
    { id: 2, sort: 2, type: 'region_plan',  title: '第一步：T 区轮廓固定',
      content:
        '**1、鼻子、眉骨、印堂**\n- 鼻子：收紧鼻背及周边皮肤组织，预防鼻背纹产生、内眼周皮肤组织松垂、立体支撑年轻化\n' +
        '- 眉骨：有效支撑眼部、预防上面部皮肤组织松垂、提升上眼皮，眼眶骨有效支撑\n' +
        '- 印堂：鼻部和眼皮皮肤收紧、有效衔接\n\n**2、下巴下颌骨**\n- 有效支撑预防嘴角肉，' +
        '与下巴做好衔接，减缓双下巴形成，下颌线清晰',
    },
    { id: 3, sort: 3, type: 'region_plan',  title: '第二步：下庭抗衰',
      content:
        '**1、口周年轻化**：解决法令纹、口角囊袋、收紧口周皮肤，下庭年轻化\n\n' +
        '**2、唇**：弱化人中长显老、改善唇纹，让唇部回到年轻态',
    },
    { id: 4, sort: 4, type: 'project_list', title: '皮肤项目建议',
      content:
        '1. **皮肤营养补充**：胶原蛋白类水光、童颜水光\n' +
        '2. **皮肤黑色素改善**：黑金超光子\n' +
        '3. **皮肤松垂改善**：芮艾缇少女枪',
    },
    { id: 5, sort: 5, type: 'material',     title: '抗衰材料推荐',
      content: '伊妍仕少女针 · 艾维岚童颜针 · 人源三型胶原蛋白 · 微笑欣颜' },
    { id: 6, sort: 6, type: 'package',      title: '套餐活动', content: '' },
  ],
  items: [
    { id: 1, projectId: 1, projectName: '黑金超光子',         plannedCount: 3, doneCount: 2,
      unitPrice: '3333.33', activityPrice: '3333.33', totalPrice: '10000.00', status: 2 },
    { id: 2, projectId: 2, projectName: '双生水光',           plannedCount: 5, doneCount: 2,
      unitPrice: '4360.00', activityPrice: '4360.00', totalPrice: '21800.00', status: 2 },
    { id: 3, projectId: 3, projectName: '芮艾缇少女枪',       plannedCount: 1, doneCount: 0,
      unitPrice: '5980.00', activityPrice: '5980.00', totalPrice: '5980.00',  status: 1 },
    { id: 4, projectId: 4, projectName: '伊妍仕少女针 1 代 ×3 支', plannedCount: 3, doneCount: 0,
      unitPrice: '19800.00', activityPrice: '15800.00', totalPrice: '47400.00', status: 1 },
    { id: 5, projectId: 6, projectName: '衡力（包年）',       plannedCount: 3, doneCount: 0,
      unitPrice: '3980.00', activityPrice: '3980.00', totalPrice: '3980.00',  status: 1 },
  ],
};

// 用 unsplash 演示人像（开发工具里勾选"不校验合法域名"即可加载）
const FACE = (seed) => `https://picsum.photos/seed/starry-${seed}/600/800`;

const PHOTOS_TIMELINE = [
  {
    month: '2026-05',
    photos: [
      { id: 11, pose: 1, poseLabel: '正面',  shotAt: '2026-05-12T10:30:00', url: FACE(11), locked: 0 },
      { id: 12, pose: 2, poseLabel: '左 45°', shotAt: '2026-05-12T10:31:00', url: FACE(12), locked: 0 },
      { id: 13, pose: 3, poseLabel: '右 45°', shotAt: '2026-05-12T10:32:00', url: FACE(13), locked: 0 },
    ],
  },
  {
    month: '2026-02',
    photos: [
      { id: 7,  pose: 1, poseLabel: '正面',  shotAt: '2026-02-10T11:00:00', url: FACE(7),  locked: 0 },
      { id: 8,  pose: 2, poseLabel: '左 45°', shotAt: '2026-02-10T11:01:00', url: FACE(8),  locked: 0 },
    ],
  },
  {
    month: '2025-11',
    photos: [
      { id: 3,  pose: 1, poseLabel: '正面',  shotAt: '2025-11-08T09:30:00', url: FACE(3),  locked: 0 },
      { id: 4,  pose: 2, poseLabel: '左 45°', shotAt: '2025-11-08T09:31:00', url: FACE(4),  locked: 0 },
      { id: 5,  pose: 3, poseLabel: '右 45°', shotAt: '2025-11-08T09:32:00', url: FACE(5),  locked: 0 },
      { id: 6,  pose: 4, poseLabel: '顶光',  shotAt: '2025-11-08T09:33:00', url: FACE(6),  locked: 1 },
    ],
  },
  {
    month: '2025-05',
    photos: [
      { id: 1,  pose: 1, poseLabel: '正面',  shotAt: '2025-05-04T15:00:00', url: FACE(1),  locked: 0 },
      { id: 2,  pose: 2, poseLabel: '左 45°', shotAt: '2025-05-04T15:01:00', url: FACE(2),  locked: 0 },
    ],
  },
];

const QUICK_COMPARE = {
  mode: 'first-vs-latest',
  before: PHOTOS_TIMELINE[3].photos[0], // 2025-05 首张
  after:  PHOTOS_TIMELINE[0].photos[0], // 2026-05 最新
};

const PROJECTS = [
  { id: 1, name: '黑金超光子',     category: '光电', durationMin: 60, unitPrice: '3333.33',
    description: '改善皮肤黑色素沉淀、肤色暗黄、毛孔粗大、细纹' },
  { id: 2, name: '双生水光',       category: '注射', durationMin: 90, unitPrice: '4360.00',
    description: '皮肤营养补充：胶原蛋白类水光 + 童颜水光' },
  { id: 3, name: '芮艾缇少女枪',   category: '光电', durationMin: 60, unitPrice: '5980.00',
    description: '皮肤松垂改善 · 紧致提升' },
  { id: 4, name: '伊妍仕少女针',   category: '注射', durationMin: 90, unitPrice: '15800.00',
    description: 'PCL 类长效胶原刺激剂 · 抗衰填充' },
  { id: 5, name: '艾维岚童颜针',   category: '注射', durationMin: 60, unitPrice: '8800.00',
    description: 'PLLA 类胶原刺激剂' },
  { id: 6, name: '衡力（包年）',   category: '注射', durationMin: 30, unitPrice: '3980.00',
    description: '川字纹放松 · 抗皱' },
];

const APPOINTMENTS = [
  {
    id: 101, storeId: 1, storeName: '思达芮旗舰店',
    projectId: 2, projectName: '双生水光', unitPrice: '4360.00', durationMin: 90,
    startAt: getRelativeISO(3, 14, 0),
    endAt:   getRelativeISO(3, 15, 30),
    status: 2, statusLabel: '已确认', source: 1,
    consultantName: '沈妍希',
    canCancel: true,
  },
  {
    id: 100, storeId: 1, storeName: '思达芮旗舰店',
    projectId: 1, projectName: '黑金超光子', unitPrice: '3333.33', durationMin: 60,
    startAt: getRelativeISO(-21, 11, 0),
    endAt:   getRelativeISO(-21, 12, 0),
    status: 4, statusLabel: '已完成', source: 1,
    consultantName: '沈妍希',
    canCancel: false,
  },
  {
    id: 99, storeId: 1, storeName: '思达芮旗舰店',
    projectId: 1, projectName: '黑金超光子', unitPrice: '3333.33', durationMin: 60,
    startAt: getRelativeISO(-49, 11, 0),
    endAt:   getRelativeISO(-49, 12, 0),
    status: 4, statusLabel: '已完成', source: 1,
    consultantName: '沈妍希',
    canCancel: false,
  },
];

function getRelativeISO(daysOffset, hour, minute) {
  const d = new Date();
  d.setHours(hour, minute, 0, 0);
  d.setDate(d.getDate() + daysOffset);
  return d.getFullYear() + '-' +
    pad(d.getMonth() + 1) + '-' +
    pad(d.getDate()) + 'T' +
    pad(d.getHours()) + ':' +
    pad(d.getMinutes()) + ':00';
}
function pad(n) { return n < 10 ? '0' + n : '' + n; }

function buildAvailability(date, projectId) {
  const project = PROJECTS.find(p => p.id === Number(projectId)) || PROJECTS[0];
  const slots = [];
  const booked = new Set(['10:30', '11:00', '14:00', '14:30', '16:30']);
  for (let h = 10; h < 21; h++) {
    for (let m = 0; m < 60; m += 30) {
      const time = pad(h) + ':' + pad(m);
      const endHour = h + Math.floor((m + project.durationMin) / 60);
      if (endHour > 21) continue;
      const available = !booked.has(time);
      slots.push({ time, available, used: available ? 0 : 2 });
    }
  }
  return {
    date, storeId: 1, projectId: project.id, projectName: project.name,
    durationMin: project.durationMin, openAt: '10:00', closeAt: '21:00',
    capacity: 2, slots,
  };
}

// ─────────────────────────────────────────────────────────
//  路由匹配
// ─────────────────────────────────────────────────────────

const ROUTES = [
  { method: 'POST', test: p => p === '/api/auth/wx-login',                 fn: () => LOGIN },

  { method: 'GET',  test: p => p === '/api/plans/mine',                    fn: () => [PLAN_SUMMARY] },
  { method: 'GET',  test: p => /^\/api\/plans\/\d+$/.test(p),              fn: () => PLAN_DETAIL },

  { method: 'GET',  test: p => /^\/api\/photos\/timeline/.test(p),         fn: (p) => filterTimelineByPose(p) },
  { method: 'GET',  test: p => /^\/api\/photos\/compare\/quick/.test(p),   fn: () => QUICK_COMPARE },
  { method: 'GET',  test: p => /^\/api\/photos\/compare\?/.test(p),        fn: (p) => buildCompare(p) },
  { method: 'POST', test: p => p === '/api/photos',                        fn: () => fakePhoto() },
  { method: 'POST', test: p => /^\/api\/photos\/\d+\/lock/.test(p),        fn: () => null },
  { method: 'DELETE', test: p => /^\/api\/photos\/\d+$/.test(p),           fn: () => null },

  { method: 'GET',  test: p => p === '/api/projects',                      fn: () => PROJECTS },
  { method: 'GET',  test: p => /^\/api\/appointments\/availability/.test(p), fn: (p) => mockAvailability(p) },
  { method: 'GET',  test: p => p === '/api/appointments/mine',             fn: () => APPOINTMENTS },
  { method: 'GET',  test: p => /^\/api\/appointments\/\d+$/.test(p),       fn: (p) => mockAppointmentDetail(p) },
  { method: 'POST', test: p => p === '/api/appointments',                  fn: (_, body) => mockBook(body) },
  { method: 'POST', test: p => /^\/api\/appointments\/\d+\/cancel/.test(p), fn: () => null },
];

function pathOf(fullPath) {
  const idx = fullPath.indexOf('?');
  return idx > 0 ? fullPath.slice(0, idx) : fullPath;
}

function queryOf(fullPath) {
  const idx = fullPath.indexOf('?');
  if (idx < 0) return {};
  const q = {};
  fullPath.slice(idx + 1).split('&').forEach(kv => {
    const [k, v] = kv.split('=');
    q[decodeURIComponent(k)] = decodeURIComponent(v || '');
  });
  return q;
}

function filterTimelineByPose(fullPath) {
  const pose = Number(queryOf(fullPath).pose);
  if (!pose) return PHOTOS_TIMELINE;
  return PHOTOS_TIMELINE
    .map(g => ({ month: g.month, photos: g.photos.filter(p => p.pose === pose) }))
    .filter(g => g.photos.length);
}

function buildCompare(fullPath) {
  const q = queryOf(fullPath);
  const all = PHOTOS_TIMELINE.flatMap(g => g.photos);
  return {
    mode: 'custom',
    before: all.find(p => p.id === Number(q.before)) || all[0],
    after:  all.find(p => p.id === Number(q.after))  || all[1],
  };
}

function fakePhoto() {
  const id = Math.floor(Math.random() * 9000 + 1000);
  return {
    id, pose: 1, poseLabel: '正面',
    shotAt: new Date().toISOString().slice(0, 19),
    url: FACE(id), locked: 0,
  };
}

function mockAvailability(fullPath) {
  const q = queryOf(fullPath);
  return buildAvailability(q.date, q.projectId);
}

function mockAppointmentDetail(fullPath) {
  const id = Number(fullPath.match(/\/(\d+)$/)[1]);
  const a = APPOINTMENTS.find(x => x.id === id);
  return a || APPOINTMENTS[0];
}

function mockBook(body) {
  const project = PROJECTS.find(p => p.id === body.projectId) || PROJECTS[0];
  const id = Math.floor(Math.random() * 9000 + 1000);
  const startAt = body.startAt;
  const startDt = new Date(startAt.replace(' ', 'T'));
  const endDt = new Date(startDt.getTime() + project.durationMin * 60000);
  const endAt = endDt.toISOString().slice(0, 19);
  const newAppt = {
    id, storeId: 1, storeName: '思达芮旗舰店',
    projectId: project.id, projectName: project.name,
    unitPrice: project.unitPrice, durationMin: project.durationMin,
    startAt, endAt, status: 1, statusLabel: '待确认', source: 1,
    consultantName: '沈妍希', customerNote: body.customerNote,
    canCancel: true,
  };
  APPOINTMENTS.unshift(newAppt);
  return newAppt;
}

// ─────────────────────────────────────────────────────────
//  对外 API
// ─────────────────────────────────────────────────────────

function match(method, fullPath, body) {
  const p = pathOf(fullPath);
  for (const r of ROUTES) {
    if (r.method === method && r.test(fullPath)) {
      const data = r.fn(fullPath, body);
      return Promise.resolve(data);
    }
  }
  return null; // 不匹配，回落到真实请求
}

module.exports = { match };
