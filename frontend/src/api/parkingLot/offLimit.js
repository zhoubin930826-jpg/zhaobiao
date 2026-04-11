import request from '@/plugins/request';

// 添加禁入车辆
export function addForbiddenVehicle (data) {
    return request({
        url: '/parkinglot/forbidden/add',
        method: 'post',
        data
    });
}
// 编辑禁入车辆
export function editForbiddenVehicle (data) {
    return request({
        url: '/parkinglot/forbidden/edit',
        method: 'post',
        data
    });
}
// 解除禁入
export function deleteForbiddenVehicle (params) {
    return request({
        url: '/parkinglot/forbidden/delete',
        method: 'get',
        params
    });
}
// 获取禁入车辆列表
export function getForbiddenVehicles (params) {
    return request({
        url: '/parkinglot/forbidden/list',
        method: 'get',
        params
    });
}

// 获取禁入车辆详情
export function getForbiddenVehicleDetail (params) {
    return request({
        url: '/parkinglot/forbidden/detail',
        method: 'get',
        params
    });
}
