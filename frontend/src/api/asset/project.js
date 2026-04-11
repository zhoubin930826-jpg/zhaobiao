import request from '@/plugins/request';

// 获取项目类型枚举
export function getitemtype (params) {
    return request({
        url: '/asset/item/getitemtype',
        method: 'get',
        params
    });
}
// 获取项目列表
export function getProjectList (params) {
    return request({
        url: '/asset/item/list',
        method: 'get',
        params
    });
}
// 获取项目详情
export function getProjectDetail (params) {
    return request({
        url: '/asset/item/detail',
        method: 'get',
        params
    });
}

// 创建项目
export function CreateProject (data) {
    return request({
        url: '/asset/item/add',
        method: 'post',
        data
    });
}
// 修改项目
export function modifyProject (data) {
    return request({
        url: '/asset/item/edit',
        method: 'post',
        data
    });
}
// 删除项目
export function deleteProject (params) {
    return request({
        url: '/asset/item/delete',
        method: 'get',
        params
    });
}
