import request from '@/plugins/request';

/* ========== 用户管理（管理员）========== */

export function listAdminUsers () {
    return request({
        url: '/admin/admin-users',
        method: 'get'
    });
}

/** @deprecated 旧物业接口名；现等价于拉取全量用户后由前端筛选分页 */
export function GetUserList () {
    return listAdminUsers();
}

export function createAdminUser (data) {
    return request({
        url: '/admin/admin-users',
        method: 'post',
        data
    });
}

export function updateAdminUser (userId, data) {
    return request({
        url: `/admin/admin-users/${userId}`,
        method: 'put',
        data
    });
}

export function updateAdminUserRoles (userId, data) {
    return request({
        url: `/admin/admin-users/${userId}/roles`,
        method: 'put',
        data
    });
}

export function updateAdminUserStatus (userId, data) {
    return request({
        url: `/admin/admin-users/${userId}/status`,
        method: 'put',
        data
    });
}

export function resetAdminUserPassword (userId, data) {
    return request({
        url: `/admin/admin-users/${userId}/password`,
        method: 'put',
        data
    });
}

// 兼容旧方法名
export const listUsers = listAdminUsers;
export const updateUserRoles = updateAdminUserRoles;

/* ========== 会员管理 ========== */

export function listMembers () {
    return request({
        url: '/admin/members',
        method: 'get'
    });
}

export function getMemberDetail (memberId) {
    return request({
        url: `/admin/members/${memberId}`,
        method: 'get'
    });
}

export function createMember (data) {
    return request({
        url: '/admin/members',
        method: 'post',
        data
    });
}

export function updateMember (memberId, data) {
    return request({
        url: `/admin/members/${memberId}`,
        method: 'put',
        data
    });
}

export function updateMemberDownloadAccess (memberId, data) {
    return request({
        url: `/admin/members/${memberId}/download-access`,
        method: 'put',
        data
    });
}

export function updateMemberStatus (memberId, data) {
    return request({
        url: `/admin/members/${memberId}/status`,
        method: 'put',
        data
    });
}

export function resetMemberPassword (memberId, data) {
    return request({
        url: `/admin/members/${memberId}/password`,
        method: 'put',
        data
    });
}

export function listBusinessTypeOptions () {
    return request({
        url: '/admin/business-types/options',
        method: 'get'
    });
}

export function listBusinessTypes () {
    return request({
        url: '/admin/business-types',
        method: 'get'
    });
}

export function createBusinessType (data) {
    return request({
        url: '/admin/business-types',
        method: 'post',
        data
    });
}

export function updateBusinessType (businessTypeId, data) {
    return request({
        url: `/admin/business-types/${businessTypeId}`,
        method: 'put',
        data
    });
}

export function updateBusinessTypeStatus (businessTypeId, data) {
    return request({
        url: `/admin/business-types/${businessTypeId}/status`,
        method: 'put',
        data
    });
}

export function deleteBusinessType (businessTypeId) {
    return request({
        url: `/admin/business-types/${businessTypeId}`,
        method: 'delete'
    });
}

/* ========== 招标管理 ========== */

export function listTenders (params) {
    return request({
        url: '/admin/tenders',
        method: 'get',
        params
    });
}

export function getTenderDetail (tenderId) {
    return request({
        url: `/admin/tenders/${tenderId}`,
        method: 'get'
    });
}

export function createTender (data) {
    return request({
        url: '/admin/tenders',
        method: 'post',
        data
    });
}

export function updateTender (tenderId, data) {
    return request({
        url: `/admin/tenders/${tenderId}`,
        method: 'put',
        data
    });
}

export function deleteTender (tenderId) {
    return request({
        url: `/admin/tenders/${tenderId}`,
        method: 'delete'
    });
}

export function uploadTenderFiles (files) {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return request({
        url: '/admin/files/upload',
        method: 'post',
        data: formData,
        timeout: 120000,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export function auditUser () {
    return Promise.reject(new Error('管理员账号不支持审核，请使用状态接口 updateAdminUserStatus'));
}

export function listUserAuditRecords () {
    return Promise.resolve([]);
}

/* ========== 角色 ========== */

export function listRoles () {
    return request({
        url: '/admin/roles',
        method: 'get'
    });
}

export const getRoleList = listRoles;

export function createRole (data) {
    return request({
        url: '/admin/roles',
        method: 'post',
        data
    });
}

export function updateRole (roleId, data) {
    return request({
        url: `/admin/roles/${roleId}`,
        method: 'put',
        data
    });
}

export function deleteRole (roleId) {
    return request({
        url: `/admin/roles/${roleId}`,
        method: 'delete'
    });
}

/* ========== 权限 ========== */

export function listPermissions () {
    return request({
        url: '/admin/permissions',
        method: 'get'
    });
}

export function createPermission (data) {
    return request({
        url: '/admin/permissions',
        method: 'post',
        data
    });
}

export function updatePermission (permissionId, data) {
    return request({
        url: `/admin/permissions/${permissionId}`,
        method: 'put',
        data
    });
}

export function deletePermission (permissionId) {
    return request({
        url: `/admin/permissions/${permissionId}`,
        method: 'delete'
    });
}

/* ========== 菜单 ========== */

export function listMenus () {
    return request({
        url: '/admin/menus',
        method: 'get'
    });
}

export const GetMenuList = listMenus;

function normalizeMenuPayload (data) {
    const name = data.name || data.title;
    const sortOrder = Number(data.sortOrder != null ? data.sortOrder : data.sort) || 0;
    const visible = data.visible != null
        ? data.visible
        : (data.status === '1' || data.status === 1);
    const enabled = data.enabled != null
        ? data.enabled
        : (data.status === '1' || data.status === 1);
    return {
        code: data.code || `MENU_${Date.now()}`,
        name,
        type: data.type || 'MENU',
        parentId: data.parentId != null && data.parentId !== '' ? data.parentId : undefined,
        routePath: data.routePath || data.fe_path || '',
        component: data.component || '',
        icon: data.icon || '',
        sortOrder,
        visible,
        enabled,
        permissionCode: data.permissionCode || '',
        description: data.description || data.remark || ''
    };
}

export function createMenu (data) {
    return request({
        url: '/admin/menus',
        method: 'post',
        data: normalizeMenuPayload(data)
    });
}

/** 兼容原「组织」新增表单字段 */
export function addOrg (data) {
    const parentId = data.pid && data.pid !== 0 ? data.pid : undefined;
    return createMenu({
        ...data,
        title: data.title,
        parentId,
        code: data.code || `MENU_${parentId || 'root'}_${Date.now()}`,
        type: data.menuType || 'MENU',
        fe_path: data.fe_path || '',
        icon: data.icon || '',
        sort: data.sort,
        status: data.status,
        remark: data.remark
    });
}

export function updateAdminMenu (menuId, data) {
    return request({
        url: `/admin/menus/${menuId}`,
        method: 'put',
        data: normalizeMenuPayload({ ...data, id: menuId })
    });
}

/** 兼容旧版 UpdateMenu({ id, ... }) */
export function UpdateMenu (data) {
    return updateAdminMenu(data.id, data);
}

export function deleteMenu (menuId) {
    return request({
        url: `/admin/menus/${menuId}`,
        method: 'delete'
    });
}

export function DeleteOrgMultiple (params) {
    const id = params.ids != null ? params.ids : params.id;
    return deleteMenu(id);
}

export function editOrg (data) {
    return updateAdminMenu(data.id, data);
}

/* ========== 操作日志 ========== */

export function listOperationLogs () {
    return request({
        url: '/admin/operation-logs',
        method: 'get'
    });
}

/* ========== 旧物业接口占位（避免其它页面 import 报错）========== */

export function getRoleDetail (params) {
    return listRoles().then(list => {
        const row = (list || []).find(r => r.id === params.id);
        return row || {};
    });
}

export function getUserDetail () {
    return Promise.resolve({});
}

export function getauthDetail () {
    return Promise.resolve({
        organize_detail: {},
        role_list: []
    });
}

export function getOrgList () {
    return Promise.resolve([]);
}

export function getAuthList () {
    return listPermissions();
}

export function DeleteUser () {
    return Promise.reject(new Error('当前后端未提供删除用户接口'));
}

export function UpdateUser () {
    return Promise.reject(new Error('请使用「分配角色」接口 updateUserRoles'));
}

export function CreateUser () {
    return Promise.reject(new Error('请使用开放注册接口 POST /api/auth/register'));
}
