import request from '@/plugins/request';

// 门禁卡管理
// 门禁卡管理 - 门禁卡列表
export function getAccessCardList (params) {
    return request({
        url: '/accesscontrol/accesscard/list',
        method: 'get',
        params
    });
}
// 门禁卡管理 - 门禁卡详情
export function getAccessCardDetail (params) {
    return request({
        url: '/accesscontrol/accesscard/detail',
        method: 'get',
        params
    });
}
// 门禁卡管理 - 获取当前卡权限及可分配权限的门禁列表
export function getAccessCardAuth (params) {
    return request({
        url: '/accesscontrol/accesscard/getauth',
        method: 'get',
        params
    });
}
//  门禁卡管理 - 修改门禁卡权限
export function updateAccessCardAuth (data) {
    return request({
        url: '/accesscontrol/accesscard/editauth',
        method: 'post',
        data
    });
}

// 门禁卡管理 - 禁用门禁卡
export function disableAccessCard (params) {
    return request({
        url: '/accesscontrol/accesscard/disable',
        method: 'get',
        params
    });
}

// 门禁卡管理 - 启用门禁卡
export function enableAccessCard (params) {
    return request({
        url: '/accesscontrol/accesscard/enable',
        method: 'get',
        params
    });
}
// 门禁卡管理 - 门禁设备应答接口【设备调用】
export function accessCardRespond (params) {
    return request({
        url: '/accesscontrol/accesscard/notify',
        method: 'get',
        params
    });
}
