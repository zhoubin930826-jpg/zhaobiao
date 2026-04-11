import request from '@/plugins/request';

// 添加白名单车辆
export function addWhitelist (data) {
    return request({
        url: '/parkinglot/whitelist/add',
        method: 'post',
        data
    });
}
// 编辑白名单车辆
export function editWhitelist (data) {
    return request({
        url: '/parkinglot/whitelist/edit',
        method: 'post',
        data
    });
}
// 解除白名单
export function deleteWhitelist (params) {
    return request({
        url: '/parkinglot/whitelist/delete',
        method: 'get',
        params
    });
}
// 获取固定车辆列表
export function getWhitelists (params) {
    return request({
        url: '/parkinglot/whitelist/list',
        method: 'get',
        params
    });
}

// 获取固定车辆详情
export function getWhitelistDetail (params) {
    return request({
        url: '/parkinglot/whitelist/detail',
        method: 'get',
        params
    });
}
