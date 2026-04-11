import request from '@/plugins/request';

export function getMonthlyRentVehicleRules (params) {
    return request({
        url: '/parkinglot/monthlyrent/get-parkinglot-rules',
        method: 'get',
        params
    });
}
// 添加月租车辆
export function addMonthlyRentVehicle (data) {
    return request({
        url: '/parkinglot/monthlyrent/add',
        method: 'post',
        data
    });
}
// 编辑月租车辆
export function editMonthlyRentVehicle (data) {
    return request({
        url: '/parkinglot/monthlyrent/edit',
        method: 'post',
        data
    });
}
// 解除月租
export function deleteMonthlyRentVehicle (params) {
    return request({
        url: '/parkinglot/monthlyrent/delete',
        method: 'get',
        params
    });
}
// 获取月租车辆列表
export function getMonthlyRentVehicles (params) {
    return request({
        url: '/parkinglot/monthlyrent/list',
        method: 'get',
        params
    });
}

// 获取月租车辆详情
export function getMonthlyRentVehicleDetail (params) {
    return request({
        url: '/parkinglot/monthlyrent/detail',
        method: 'get',
        params
    });
}

// 获取月租车续费信息
export function getRenewInfo (params) {
    return request({
        url: '/parkinglot/monthlyrent/renewinfo',
        method: 'get',
        params
    });
}

// 月租车续费
export function createRenew (data) {
    return request({
        url: '/parkinglot/monthlyrent/renewcreate',
        method: 'post',
        data
    });
}

// 修改月租车有效期
export function editDueDate (data) {
    return request({
        url: '/parkinglot/monthlyrent/edit-due-date',
        method: 'post',
        data
    });
}
