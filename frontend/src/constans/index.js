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

const ENTRY_TYPE = [
    {
        value: 1,
        label: '固定车'
    },
    {
        value: 2,
        label: '月租车'
    },
    {
        value: 3,
        label: '白名单车'
    },
    {
        value: 4,
        label: '黑名单车'
    },
    {
        value: 5,
        label: '临时车'
    }, {
        value: 6,
        label: '其他'
    }
    // 入场类型，1固定车，2月租车，3白名单车，4黑名单车，5临时车，6其他
]
const ENTRY_METHODS = [
    {
        value: 1,
        label: '无牌车扫码'
    },
    {
        value: 2,
        label: '自动识别'
    },
    {
        value: 3,
        label: '人工抬杆'
    }
]
export default {
    PAYMENT_METHODS,
    PAYMENT_CHANNELS,
    TYPE_LIST,
    ITEM_LIST,
    PAYMENT_STATUS,
    TRANSACTION_STATUS,
    PUSH_STATUS,
    ENTRY_TYPE,
    ENTRY_METHODS
}
