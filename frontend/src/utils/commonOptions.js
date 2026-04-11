// 支付方式
const PAYMENT_METHODS = [{
                             value: 1,
                             label: '线上'
                         },
                         {
                             value: 2,
                             label: '线下'
                         }
]
// 支付渠道
const PAYMENT_CHANNELS = [{
                              value: 1,
                              label: '微信支付'
                          },
                          {
                              value: 2,
                              label: '支付宝'
                          },
                          {
                              value: 3,
                              label: '现金'
                          },
                          {
                              value: 4,
                              label: '银行卡'
                          }
]

// 费用类型
const TYPE_LIST = [{
                       value: 1,
                       label: '物业费'
                   },
                   {
                       value: 2,
                       label: '车位费'
                   },
                   {
                       value: 3,
                       label: '采暖费'
                   },
                   {
                       value: 4,
                       label: '水费'
                   },
                   {
                       value: 5,
                       label: '人像录入费'
                   },
                   {
                       value: 6,
                       label: '门禁卡费'
                   }
]
// 费用项目
const ITEM_LIST = [{
                       value: 1,
                       label: '住宅物业费',
                       key: 1
                   },
                   {
                       value: 2,
                       label: '商铺物业费',
                       key: 1
                   },
                   {
                       value: 3,
                       label: '写字楼物业费',
                       key: 1
                   },
                   {
                       value: 4,
                       label: '车库物业费',
                       key: 1
                   },
                   {
                       value: 5,
                       label: '储藏间物业费',
                       key: 1
                   },
                   {
                       value: 6,
                       label: '车位费',
                       key: 2
                   },
                   {
                       value: 7,
                       label: '租赁车位费',
                       key: 2
                   },
                   {
                       value: 8,
                       label: '住宅采暖费',
                       key: 3
                   },
                   {
                       value: 13,
                       label: '商铺采暖费',
                       key: 3
                   },
                   {
                       value: 14,
                       label: '写字楼采暖费',
                       key: 3
                   },
                   {
                       value: 9,
                       label: '住宅水费',
                       key: 4
                   },
                   {
                       value: 10,
                       label: '商铺水费',
                       key: 4
                   },
                   {
                       value: 11,
                       label: '人像录入费',
                       key: 5
                   },
                   {
                       value: 12,
                       label: '门禁卡费',
                       key: 6
                   }
]
// 支付状态
const PAYMENT_STATUS = [{
                            value: 0,
                            label: '待支付'
                        },
                        {
                            value: 1,
                            label: '支付成功'
                        },
                        {
                            value: 2,
                            label: '已取消'
                        },
                        {
                            value: 3,
                            label: '已关闭'
                        },
                        {
                            value: 4,
                            label: '退款中'
                        },
                        {
                            value: 5,
                            label: '已退款'
                        }
]
// 交易类型
const TRANSACTION_STATUS = [{
                                value: 1,
                                label: '付款'
                            },
                            {
                                value: 2,
                                label: '退款'
                            }
]
// 推送状态
const PUSH_STATUS = [{
                         value: 0,
                         label: '待推送'
                     },
                     {
                         value: 1,
                         label: '推送成功'
                     },
                     {
                         value: 2,
                         label: '推送处理中'
                     },
                     {
                         value: 3,
                         label: '推送失败'
                     }
]

// 资产状态
const ASSET_STATUS = [{
                          value: '0',
                          label: '闲置',
                          color: '#13c2c2'
                      },
                      {
                          value: '1',
                          label: '入住',
                          color: '#fa8c16'
                      },
                      {
                          value: '2',
                          label: '装修',
                          color: '#ed4014'
                      },
                      {
                          value: '3',
                          label: '过户',
                          color: '#ff9900'
                      },
                      {
                          value: '4',
                          label: '自用',
                          color: '#19be6b'
                      },
                      {
                          value: '5',
                          label: '租用',
                          color: '#2db7f5'
                      }
]
const BIND_STATUS = [{
                         value: 'all',
                         label: '全部'
                     },
                     {
                         value: '0',
                         label: '未绑定'
                     },
                     {
                         value: '1',
                         label: '已绑定'
                     }
]
// 车牌种类
const VEHICLE_CARD_TYPES = [{
                                value: 1,
                                label: '小型汽车号牌'
                            },

                            {
                                value: 2,
                                label: '大型汽车号牌'
                            }
]
// 号牌颜色选项
const VEHICLE_CARD_COLORS = [{
                                 value: 1,
                                 label: '黄色'
                             }, {
                                 value: 2,
                                 label: '蓝色'
                             },
                             {
                                 value: 3,
                                 label: '绿色'
                             }, {
                                 value: 4,
                                 label: '黑色'
                             }
]
// 车辆类型
const VEHICLE_TYPES = [{
                           value: 1,
                           label: '中型载客汽车'
                       }, {
                           value: 2,
                           label: '小型载客汽车'
                       }, {
                           value: 3,
                           label: '微型载客汽车'
                       },
                       {
                           value: 4,
                           label: '中型载货汽车'
                       }, {
                           value: 5,
                           label: '小型载货汽车'
                       },
                       {
                           value: 6,
                           label: '微型载货汽车'
                       },
                       {
                           value: 7,
                           label: '轻便摩托车'
                       },
                       {
                           value: 8,
                           label: '普通摩托车'
                       }
]
// 车辆颜色
const VEHICLE_COLORS = [{
    value: 1,
    label: '白色'
}, {
    value: 2,
    label: '黑色'
}, {
    value: 3,
    label: '红色'
}, {
    value: 4,
    label: '蓝色'
}, {
    value: 5,
    label: '银色'
}, {
    value: 6,
    label: '香槟色'
}]
// 车辆状态
const VEHICLE_STATUS = [{
                            value: 'all',
                            label: '全部'
                        },
                        {
                            value: '2',
                            label: '已到期'
                        },
                        {
                            value: '1',
                            label: '未到期'
                        }
]
const WHITELIST_TYPE = [
    { value: 1, label: '工作人员车辆' },
    {
        value: 2,
        label: '社区/政府车辆'
    },
    {
        value: 3,
        label: '长期送货车辆'
    },
    {
        value: 4,
        label: '其他'
    }

]

const VEHICLE_USED_TYPE = [
    { value: 'all', label: '全部' },
    { value: '1', label: '固定车' },
    { value: '2', label: '月租车' },
    { value: '3', label: '白名单车' },
    { value: '4', label: '黑名单车' },
    { value: '5', label: '临时车' },
    { value: '6', label: '其他' }
]

const ENTER_OUT_METHODS = [
    { value: '1', label: '无牌车扫码' },
    { value: '2', label: '自动识别' },
    { value: '3', label: '人工抬杆' }

]
const FORBIDDEN_TYPE = [
    { value: '1', label: '违停处罚车辆' },
    { value: '2', label: '套牌车辆' },
    { value: '3', label: '其他' }
]

const LIST_MAP = {
    pushStatus: PUSH_STATUS,
    transactionStatus: TRANSACTION_STATUS,
    paymentStatus: PAYMENT_STATUS,
    itemList: ITEM_LIST,
    typeList: TYPE_LIST,
    paymentChannels: PAYMENT_CHANNELS,
    paymentMethods: PAYMENT_METHODS,
    assetStatus: ASSET_STATUS,
    bindStatus: BIND_STATUS,
    vehicleCardTypes: VEHICLE_CARD_TYPES,
    vehicleCardColor: VEHICLE_CARD_COLORS,
    vehicleTypes: VEHICLE_TYPES,
    vehicleColors: VEHICLE_COLORS,
    vehicleStatus: VEHICLE_STATUS,
    whitelistType: WHITELIST_TYPE,
    vehicleUsedType: VEHICLE_USED_TYPE,
    enterOutMethods: ENTER_OUT_METHODS,
    forbiddenType: FORBIDDEN_TYPE
}
/**
 * @params {string|number} value
 * @params {string} key  keyof maps
 * @return {string} label
 */
export function changeValueToLabel (value, key) {
    let target = LIST_MAP[key]
    if (!target) {
        return new TypeError('key is not belong LIST_MAP ' + Object.keys(LIST_MAP))
    }
    if (target.length < 1) {
        return '';
    }
    let data = target.find((item) => Number(item.value) === Number(value));
    return data && data.label;
}
export {
    TYPE_LIST,
    ITEM_LIST,
    TRANSACTION_STATUS,
    PUSH_STATUS,
    PAYMENT_CHANNELS,
    PAYMENT_METHODS,
    PAYMENT_STATUS,
    ASSET_STATUS,
    BIND_STATUS,
    VEHICLE_CARD_TYPES,
    VEHICLE_CARD_COLORS,
    VEHICLE_TYPES,
    VEHICLE_COLORS,
    VEHICLE_STATUS,
    WHITELIST_TYPE,
    VEHICLE_USED_TYPE,
    ENTER_OUT_METHODS,
    FORBIDDEN_TYPE
}
