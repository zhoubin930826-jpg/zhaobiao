import request from '@/plugins/request';

// 固定车辆管理 - 获取停车场筛选项
export function getParkingLotList (params) {
    return request({
        url: '/parkinglot/fixed/get-parkinglot-list',
        method: 'get',
        params
    });
}

// 固定车辆管理 - 获取停车场区域筛选项
/**
 *
 * @param {parkinglot_id} parkinglot_id 停车场id
 * @returns
 */
export function getParkingLotItems (params) {
    return request({
        url: '/parkinglot/fixed/get-parkinglot-item',
        method: 'get',
        params
    });
}

// 固定车辆管理 - 获取停车场区域下资产筛选项
/**
 * @param parkinglot_id  停车场id
 * @param item_id  区域id
 * @returns
 */
export function getParkingLotAssets (params) {
    return request({
        url: '/parkinglot/fixed/get-parkinglot-asset',
        method: 'get',
        params
    });
}

// 添加固定车辆
export function addContantVehicle (data) {
    return request({
        url: '/parkinglot/fixed/add',
        method: 'post',
        data
    });
}
// 编辑固定车辆
export function editContantVehicle (data) {
    return request({
        url: '/parkinglot/fixed/edit',
        method: 'post',
        data
    });
}

// 删除固定车辆
export function deleteContantVehicle (params) {
    return request({
        url: '/parkinglot/fixed/delete',
        method: 'get',
        params
    });
}
// 获取固定车辆列表
export function getContantVehicleList (params) {
    return request({
        url: '/parkinglot/fixed/list',
        method: 'get',
        params
    });
}

// 获取固定车辆详情
export function getContantVehicleDetail (params) {
    return request({
        url: '/parkinglot/fixed/detail',
        method: 'get',
        params
    });
}
