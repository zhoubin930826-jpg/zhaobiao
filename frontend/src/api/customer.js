import request from '@/plugins/request';
// 获取客户列表
export function getCustomerList (params) {
    return request({
        url: '/customer/customer/list',
        method: 'get',
        params
    });
}
// 获取客户详情
export function getCustomerDetail (params) {
    return request({
        url: '/customer/customer/detail',
        method: 'get',
        params
    });
}

export function createdCustomer (data) {
    return request({
        url: '/customer/customer/add',
        method: 'post',
        data
    });
}
export function updateCustomer (data) {
    return request({
        url: '/customer/customer/edit',
        method: 'post',
        data
    });
}
export function deleteCustomer (params) {
    return request({
        url: '/customer/customer/delete',
        method: 'get',
        params
    });
}

export function getAssetList (params) {
    return request({
        url: '/property/community/list',
        method: 'get',
        params
    });
}

export function bindAsset (data) {
    return request({
        url: '/customer/customer/bindasset',
        method: 'post',
        data
    });
}

export function ubindAsset (data) {
    return request({
        url: '/customer/customer/unbindasset',
        method: 'post',
        data
    });
}

export function getUnbindAssetList (params) {
    return request({
        url: '/customer/customer/searchasset',
        method: 'get',
        params
    });
}

export function getItemList (params) {
    return request({
        url: '/customer/customer/get-item-list',
        method: 'get',
        params
    });
}

export function getTypeList (params) {
    return request({
        url: '/customer/customer/get-item-type',
        method: 'get',
        params
    });
}
