import request from '@/plugins/request';

// 费用规则 - 规则列表
export function getRuleList (params) {
    return request({
        url: '/finance/feerules/list',
        method: 'get',
        params
    });
}
// 费用规则 - 规则详情
export function getRuleDetail (params) {
    return request({
        url: '/finance/feerules/detail',
        method: 'get',
        params
    });
}
// 费用规则 - 添加规则
export function addRule (data) {
    return request({
        url: '/finance/feerules/add',
        method: 'post',
        data
    });
}
// 费用规则 - 编辑规则
export function editRule (data) {
    return request({
        url: '/finance/feerules/edit',
        method: 'post',
        data
    });
}

// 费用规则 - 删除规则
export function deleteRule (params) {
    return request({
        url: '/finance/feerules/delete',
        method: 'get',
        params
    });
}

// 费用规则 - 获取项目列表（楼栋）
export function getItem (params) {
    return request({
        url: 'finance/feerules/getitem',
        method: 'get',
        params
    });
}

// 费用规则 - 添加额外收费项目
export function addExtraCharge (data) {
    return request({
        url: 'finance/feerules/add-extra-charge',
        method: 'post',
        data
    });
}

// 费用规则 - 删除额外收费项目
export function delExtraCharge (params) {
    return request({
        url: 'finance/feerules/del-extra-charge',
        method: 'get',
        params
    });
}

// 费用规则 - 删除费用规则子规则
export function delSubRules (params) {
    return request({
        url: '/finance/feerules/del-sub-rules',
        method: 'get',
        params
    });
}
