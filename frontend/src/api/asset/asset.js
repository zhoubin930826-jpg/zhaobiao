import request from '@/plugins/request';
// 资产管理
// 资产管理 - 获取项目列表筛选项
export function getAssetType (params) {
    return request({
        url: '/asset/asset/getitemlist',
        method: 'get',
        params
    });
}
// 资产管理 - 资产列表
export function getAssetList (params) {
    return request({
        url: '/asset/asset/list',
        method: 'get',
        params
    });
}
// 资产管理 - 资产详情
export function getAssetDetail (params) {
    return request({
        url: '/asset/asset/detail',
        method: 'get',
        params
    });
}
// 资产管理 - 添加资产
export function createdAsset (data) {
    return request({
        url: '/asset/asset/add',
        method: 'post',
        data
    });
}
// 资产管理 - 编辑资产
export function updateAsset (data) {
    return request({
        url: '/asset/asset/edit',
        method: 'post',
        data
    });
}
//  资产管理 - 删除资产
export function deleteAsset (params) {
    return request({
        url: '/asset/asset/delete',
        method: 'get',
        params
    });
}
// 资产管理 - 搜索客户列表
export function getCustomerList (params) {
    return request({
        url: '/asset/asset/searchcustomer',
        method: 'post',
        data: params
    });
}
// 资产管理 - 绑定客户（已绑定情况下再次提交为修改更新）
export function bindCustomer (data) {
    return request({
        url: '/asset/asset/bindcustomer',
        method: 'post',
        data
    });
}
// 资产管理 - 解绑客户
export function unbindCustomer (data) {
    return request({
        url: '/asset/asset/unbindcustomer',
        method: 'post',
        data
    });
}

// 资产管理 - 修改特殊费用规则
export function editRules (data) {
    return request({
        url: '/asset/asset/editrules',
        method: 'post',
        data
    });
}

// 资产管理 - 修改资产账单设置
export function editBilling (data) {
    return request({
        url: '/asset/asset/editbilling',
        method: 'post',
        data
    });
}
