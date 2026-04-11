import request from '@/plugins/request';
// 获取小区列表
export function GetcommunityList (params) {
    return request({
        url: '/property/community/list',
        method: 'get',
        params
    });
}
// 获取小区账号列表
export function GetAccountList (params) {
    return request({
        url: '/property/community/getaccountlist',
        method: 'get',
        params
    });
}

// 获取字典表
export function GetDictionary (params) {
    return request({
        url: '/region/region/getdictionary',
        method: 'get',
        params
    });
}
// 新增小区
export function createCommunity (data) {
    return request({
        url: '/property/community/add',
        method: 'post',
        data
    });
}
// 编辑详情数据
export function EditCommunity (data) {
    return request({
        url: '/property/community/edit',
        method: 'post',
        data
    });
}
// 获取小区详情
export function GetCommunity (params) {
    return request({
        url: '/property/community/detail',
        method: 'get',
        params
    });
}

// 删除小区
export function DelCommunity (params) {
    return request({
        url: '/property/community/delete',
        method: 'get',
        params
    });
}
// 物业账号列表
export function GetPropertyAccountList (params) {
    return request({
        url: '/property/account/list',
        method: 'get',
        params
    });
}

// 账号详情
export function GetPropertyAccount (params) {
    return request({
        url: '/property/account/detail',
        method: 'get',
        params
    });
}
// 删除账号
export function DelPropertyAccount (params) {
    return request({
        url: '/property/account/delete',
        method: 'get',
        params
    });
}

// 获取小区角色列表
export function GelPropertyAccountRoleList (params) {
    return request({
        url: '/property/account/getrolefilter',
        method: 'get',
        params
    });
}
// 添加账号
export function createCommunityAccount (data) {
    return request({
        url: '/property/account/add',
        method: 'post',
        data
    });
}

// 编辑账号
export function EditCommunityAccount (data) {
    return request({
        url: '/property/account/edit',
        method: 'post',
        data
    });
}
// 获取小区列表
export function getAccountcommunityList (params) {
    return request({
        url: '/property/account/getcommunitylist',
        method: 'get',
        params
    });
}
