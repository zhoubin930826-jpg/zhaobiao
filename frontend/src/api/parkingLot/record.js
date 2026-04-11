import request from '@/plugins/request';

// 获取固定车辆列表
export function getRecordList (params) {
    return request({
        url: '/parkinglot/vehicleaccess/list',
        method: 'get',
        params
    });
}

// 获取固定车辆详情
export function getRecordDetail (params) {
    return request({
        url: '/parkinglot/vehicleaccess/detail',
        method: 'get',
        params
    });
}
