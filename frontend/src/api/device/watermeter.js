import request from '@/plugins/request';

// 水表管理
// 获取水表列表
export function getWatermeterList (params) {
    return request({
        url: '/device/watermeter/list',
        method: 'get',
        params
    });
}
// 水表管理 - 水表详情
export function getWatermeterDetail (params) {
    return request({
        url: '/device/watermeter/detail',
        method: 'get',
        params
    });
}
// 水表管理 - 水表上报记录列表
export function getReportList (params) {
    return request({
        url: '/device/watermeter/reportlist',
        method: 'get',
        params
    });
}
// 水表管理 - 绑定水表
export function bindWatermeter (data) {
    return request({
        url: '/device/watermeter/bind',
        method: 'post',
        data
    });
}
// 水表管理 - 获取资产列表【排除已绑定过水表的资产】
export function getAssetListOfWater () {
    return request({
        url: '/device/watermeter/getassetlist',
        method: 'get'
    });
}

// 水表管理 - 抄表
export function readMeter (params) {
    return request({
        url: '/device/watermeter/readmeter',
        method: 'get',
        params
    });
}

// 水表管理 - 远程控阀
export function controlValue (params) {
    return request({
        url: '/device/watermeter/controlvalue',
        method: 'get',
        params
    });
}
