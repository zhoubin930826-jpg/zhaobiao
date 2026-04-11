import request from '@/plugins/request';

// 缴费管理 - 获取项目类型
export function getItemTypes () {
    return request({
        url: '/finance/payment/get-item-type',
        method: 'get'
    });
}
// 缴费管理 - 获取项目列表
export function getItemList (params) {
    return request({
        url: '/finance/payment/get-item-list',
        method: 'get',
        params
    });
}
// 缴费管理 - 获取项目下资产列表
export function getAssetList (params) {
    return request({
        url: '/finance/payment/get-item-asset',
        method: 'get',
        params
    });
}
// 缴费管理 - 获取资产下订单列表
export function getPaymentList (params) {
    return request({
        url: '/finance/payment/list',
        method: 'get',
        params
    });
}
// 缴费管理 - 订单详情
export function getPaymentDetail (params) {
    return request({
        url: '/finance/payment/detail',
        method: 'get',
        params
    });
}

// 缴费管理 - 调整物业缴费订单减免金额
export function updateReduceAmount (data) {
    return request({
        url: '/finance/payment/reduce',
        method: 'post',
        data
    });
}
// 缴费管理 - 物业缴费订单线下缴费
export function paymentByOffline (data) {
    return request({
        url: '/finance/payment/pay',
        method: 'post',
        data
    });
}
// 缴费管理 - 获取资产下收费规则
export function getAssetRules (params) {
    return request({
        url: '/finance/payment/get-asset-rules',
        method: 'get',
        params
    });
}
// 缴费管理 - 创建生活缴费订单
export function createLifeOrder (data) {
    return request({
        url: '/finance/payment/create',
        method: 'post',
        data
    });
}
// 查看缴费凭证
export function getPaymentVoucher (params) {
    return request({
        url: '/finance/order/payment-voucher',
        method: 'get',
        params
    });
}
