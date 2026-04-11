import request from '@/plugins/request';

// 门禁管理
// 门禁管理 - 门禁列表
export function getAccessList (params) {
    return request({
        url: '/device/accesscontrol/list',
        method: 'get',
        params
    });
}
// 门禁管理 - 门禁详情
export function getAccessDetail (params) {
    return request({
        url: '/device/accesscontrol/detail',
        method: 'get',
        params
    });
}
// 门禁管理 - 绑定门禁
export function bindAccess (data) {
    return request({
        url: '/device/accesscontrol/bind',
        method: 'post',
        data
    });
}
//  门禁管理 - 获取项目类型筛选项
export function getFilterItemTypes () {
    return request({
        url: '/device/accesscontrol/item-type-filter',
        method: 'get'
    });
}

// 门禁管理 - 获取项目列表筛选项
export function getFilterItemList () {
    return request({
        url: '/device/accesscontrol/item-filter',
        method: 'get'
    });
}

//  门禁管理 - 获取项目单元筛选项
export function getFilterUniList (params) {
    return request({
        url: '/device/accesscontrol/item-unit-filter',
        method: 'get',
        params
    });
}

//  门禁管理 - 门禁详情内门查看门禁卡信息
export function getAccessCardList (params) {
    return request({
        url: '/device/accesscontrol/accesscard',
        method: 'get',
        params
    });
}

//  门禁管理 - 门禁详情内查看人像信息
export function getAccessAvatarList (params) {
    return request({
        url: '/device/accesscontrol/portrait',
        method: 'get',
        params
    });
}
//  门禁管理 - 门禁详情内查看开门记录
export function getAccessOpenList (params) {
    return request({
        url: '/device/accesscontrol/openrecord',
        method: 'get',
        params
    });
}
