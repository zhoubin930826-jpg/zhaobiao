import request from '@/plugins/request';

// 财务管理 - 周期性缴费订单生成记录列表
export function getCycleOrderList (params) {
    return request({
        url: '/finance/order/createlist',
        method: 'get',
        params
    });
}
// 财务管理 - 生成周期性费用订单
export function createOrder (data) {
    return request({
        url: '/finance/order/create',
        method: 'post',
        data
    });
}
// 财务管理 - 生成失败记录列表
export function getFailedOrderList (params) {
    return request({
        url: '/finance/order/create-faild',
        method: 'get',
        params
    });
}
// 财务管理 - 重新生成账单
export function regenerate (data) {
    return request({
        url: '/finance/order/regenerate',
        method: 'post',
        data
    });
}
// 财务管理 - 按资产编号搜索资产
export function getSingleAssetList (params) {
    return request({
        url: '/finance/order/search-asset',
        method: 'get',
        params
    });
}
// 财务管理 - 订单列表
export function getLifeOrderList (params) {
    return request({
        url: '/finance/order/list',
        method: 'get',
        params
    });
}
// 财务管理 - 订单详情
export function getCycelOrderDetail (params) {
    return request({
        url: '/finance/order/list',
        method: 'get',
        params
    });
}
// 财务管理 - 停车缴费订单列表
export function getParkingOrderList (params) {
    return request({
        url: '/finance/parking/order-list',
        method: 'get',
        params
    });
}
// 财务管理 - 停车缴费订单详情
export function getParkingOrderDetail (params) {
    return request({
        url: '/finance/parking/order-detail',
        method: 'get',
        params
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
