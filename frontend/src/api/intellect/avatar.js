import request from '@/plugins/request';

// 智能门禁
// 人像信息管理 - 人像信息列表
export function getAvatarList (params) {
    return request({
        url: '/accesscontrol/portrait/list',
        method: 'get',
        params
    });
}
// 人像信息管理 - 人像信息详情
export function getAvatarDetail (params) {
    return request({
        url: '/accesscontrol/portrait/detail',
        method: 'get',
        params
    });
}

//  人像信息管理 - 禁用人像【只有人像状态=3已生效时可禁用】
export function disableAvatar (params) {
    return request({
        url: '/accesscontrol/portrait/disable',
        method: 'get',
        params
    });
}

// 人像信息管理 - 启用人像【只有人像状态=3已生效时可启用】
export function enableAvatar (params) {
    return request({
        url: '/accesscontrol/portrait/enable',
        method: 'get',
        params
    });
}

// 人像信息推送 - 门禁设备应答接口【设备调用】
export function notifyAvatar (data) {
    return request({
        url: '/accesscontrol/portrait/notify',
        method: 'post',
        data
    });
}
