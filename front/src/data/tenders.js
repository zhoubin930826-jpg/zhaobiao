/** 演示数据，后续可替换为接口 */
export const tenderCategories = [
  { value: '', label: '全部类型' },
  { value: '工程', label: '工程类' },
  { value: '货物', label: '货物类' },
  { value: '服务', label: '服务类' }
]

export const tenderRegions = [
  { value: '', label: '全部地区' },
  { value: '北京', label: '北京' },
  { value: '上海', label: '上海' },
  { value: '广东', label: '广东' },
  { value: '浙江', label: '浙江' }
]

const now = Date.now()
const day = 86400000

/** 演示用统一附件实体；上线后可改为每条对应不同 url */
const sampleAttachmentUrl = '/attachments/tender-bidding-file-sample.txt'

export const tenders = [
  {
    id: '1',
    title: '某市智慧政务云平台建设项目公开招标公告',
    category: '服务',
    region: '浙江',
    purchaser: '某市大数据发展管理局',
    budget: '580 万元',
    publishAt: new Date(now - 1 * day).toISOString(),
    deadline: new Date(now + 14 * day).toISOString(),
    status: '进行中',
    summary:
      '建设统一政务云平台，包含计算资源池、存储资源、网络安全设备及三年运维服务。投标人须具备涉密信息系统集成资质或同等能力证明。',
    content: `一、项目概况\n本项目为智慧政务云平台建设，服务期含建设期与三年运维。\n\n二、资格要求\n1. 具有独立法人资格；\n2. 近三年无重大违法记录；\n3. 具备本项目所需技术能力与同类业绩。\n\n三、获取文件\n请于公告发布之日起登录电子招投标平台下载招标文件。\n\n四、递交截止时间\n详见招标文件，逾期不予受理。`,
    attachment: {
      url: sampleAttachmentUrl,
      fileName: '招标文件-智慧政务云平台建设.txt'
    }
  },
  {
    id: '2',
    title: '办公设备集中采购（第二包：网络设备）',
    category: '货物',
    region: '上海',
    purchaser: '某区机关事务服务中心',
    budget: '126 万元',
    publishAt: new Date(now - 2 * day).toISOString(),
    deadline: new Date(now + 10 * day).toISOString(),
    status: '进行中',
    summary: '交换机、防火墙、无线控制器等网络设备一批，具体技术参数以招标文件为准。',
    content: `采购内容：核心交换机 2 台、汇聚交换机若干、下一代防火墙等。\n交货期：合同签订后 30 日内。\n质保：不少于 3 年原厂服务。`,
    attachment: {
      url: sampleAttachmentUrl,
      fileName: '招标文件-办公设备网络设备包.txt'
    }
  },
  {
    id: '3',
    title: '城市道路综合整治工程施工总承包',
    category: '工程',
    region: '广东',
    purchaser: '某市住房和城乡建设局',
    budget: '3200 万元',
    publishAt: new Date(now - 5 * day).toISOString(),
    deadline: new Date(now - 1 * day).toISOString(),
    status: '已截止',
    summary: '道路路面修复、排水改造、交通标线及附属工程，工期 240 日历天。',
    content: `工程范围以施工图纸及工程量清单为准。\n投标人须具备市政公用工程施工总承包一级及以上资质。`,
    attachment: {
      url: sampleAttachmentUrl,
      fileName: '招标文件-城市道路综合整治工程.txt'
    }
  },
  {
    id: '4',
    title: '年度法律顾问及诉讼代理服务采购',
    category: '服务',
    region: '北京',
    purchaser: '某国有企业集团',
    budget: '80 万元/年',
    publishAt: new Date(now - 3 * day).toISOString(),
    deadline: new Date(now + 7 * day).toISOString(),
    status: '进行中',
    summary: '提供常年法律顾问、合同审查、重大决策法律意见及诉讼仲裁代理服务。',
    content: `服务期限：一年，可续签。\n团队要求：主办律师执业满 8 年，具备国资或基建类项目经验。`,
    attachment: {
      url: sampleAttachmentUrl,
      fileName: '招标文件-法律顾问及诉讼代理.txt'
    }
  },
  {
    id: '5',
    title: '医院信息化升级改造（HIS 与电子病历）',
    category: '服务',
    region: '浙江',
    purchaser: '某县人民医院',
    budget: '950 万元',
    publishAt: new Date(now - 8 * day).toISOString(),
    deadline: new Date(now - 2 * day).toISOString(),
    status: '已截止',
    summary: 'HIS、EMR、集成平台等系统建设及数据迁移，须通过等保三级测评配合。',
    content: `实施周期分三期，首期 6 个月内完成核心模块上线。\n供应商须具备医疗信息化成熟产品著作权。`,
    attachment: {
      url: sampleAttachmentUrl,
      fileName: '招标文件-医院信息化升级改造.txt'
    }
  },
  {
    id: '6',
    title: '环卫车辆及配套设备采购项目',
    category: '货物',
    region: '广东',
    purchaser: '某区城市管理综合执法局',
    budget: '410 万元',
    publishAt: new Date(now - 1 * day).toISOString(),
    deadline: new Date(now + 20 * day).toISOString(),
    status: '进行中',
    summary: '压缩式垃圾车、洗扫车、餐厨垃圾车等，排放标准国六及以上。',
    content: `车辆须符合国家机动车公告目录，提供上牌及购置税办理协助。`,
    attachment: {
      url: sampleAttachmentUrl,
      fileName: '招标文件-环卫车辆及配套设备.txt'
    }
  }
]

export function getTenderById(id) {
  return tenders.find((t) => t.id === String(id)) || null
}

export function filterTenders({ category, region, keyword }) {
  const k = (keyword || '').trim().toLowerCase()
  return tenders.filter((t) => {
    if (category && t.category !== category) return false
    if (region && t.region !== region) return false
    if (k && !t.title.toLowerCase().includes(k) && !t.purchaser.toLowerCase().includes(k))
      return false
    return true
  })
}
