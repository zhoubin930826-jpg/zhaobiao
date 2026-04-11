import request from '@/plugins/request';

// 车场相机管理
// 车场相机管理 - 车场相机列表
export function getCameraList (params) {
    return request({
        url: '/device/camera/list',
        method: 'get',
        params
    });
}
// 车场相机管理 - 车场相机详情
export function getCameraDetail (params) {
    return request({
        url: '/device/camera/detail',
        method: 'get',
        params
    });
}
// 车场相机管理 - 获取停车场筛选项
export function getParkinglot (params) {
    return request({
        url: '/device/camera/get-parkinglot',
        method: 'get',
        params
    });
}
//  车场相机管理 - 获取停车场出入口筛选项
export function getparkinglotGateway (params) {
    return request({
        url: '/device/camera/get-parkinglot-gateway',
        method: 'get',
        params
    });
}

// 门禁管理 - 获取项目列表筛选项
export function getFilterItemList () {
    return request({
        url: '/device/Cameracontrol/item-filter',
        method: 'get'
    });
}

//  车场相机管理 - 绑定相机设备
export function bindCamera (data) {
    return request({
        url: '/device/camera/bind',
        method: 'post',
        data
    });
}

//  车场相机管理 - 绑定相机设备
export function unbindCamera (params) {
    return request({
        url: '/device/camera/unbind',
        method: 'get',
        params
    });
}
