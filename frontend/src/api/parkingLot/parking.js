import request from '@/plugins/request';

// 停车场管理
// 停车场管理 - 停车场列表
export function getParkingLotList (params) {
    return request({
        url: '/parkinglot/parkinglot/list',
        method: 'get',
        params
    });
}
// 停车场管理 - 停车场详情
export function getParkingLotDetail (params) {
    return request({
        url: '/parkinglot/parkinglot/detail',
        method: 'get',
        params
    });
}
// 停车场管理 - 添加停车场
export function addParkingLot (data) {
    return request({
        url: '/parkinglot/parkinglot/add',
        method: 'post',
        data
    });
}
// 停车场管理 - 编辑停车场
export function editParkingLot (data) {
    return request({
        url: '/parkinglot/parkinglot/edit',
        method: 'post',
        data
    });
}

// 停车场管理 - 删除停车场
export function deleteParkingLot (params) {
    return request({
        url: '/parkinglot/parkinglot/delete',
        method: 'get',
        params
    });
}

// 停车场管理 - 停车场收费规则详情
export function getRulesDetail (params) {
    return request({
        url: '/parkinglot/parkinglot/rules-detail',
        method: 'get',
        params
    });
}
// 停车场管理 - 编辑停车场收费规则
export function editRulesDetail (data) {
    return request({
        url: '/parkinglot/parkinglot/edit-rules',
        method: 'post',
        data
    });
}

//  停车场管理 - 获取停车场区域筛选项
export function getAreaFilter (params) {
    return request({
        url: '/parkinglot/parkinglot/get-item-filter',
        method: 'get',
        params
    });
}
//  停车场管理 - 获取停车场入口筛选项
export function getEntranceFilter (params) {
    return request({
        url: '/parkinglot/parkinglot/get-entrance-filter',
        method: 'get',
        params
    });
}

// 停车场管理 - 获取停车场出口筛选项
export function getExitFilter (params) {
    return request({
        url: '/parkinglot/parkinglot/get-exit-filter',
        method: 'get',
        params
    });
}
// 停车场管理 - 查看停车场二维码
export function getQRCode (params) {
    return request({
        url: '/parkinglot/parkinglot/view-qrcode',
        method: 'get',
        params
    });
}

// 停车场管理 - 设置出入口广告机
export function setPlayer (data) {
    return request({
        url: '/parkinglot/parkinglot/setup-player',
        method: 'post',
        data
    });
}
