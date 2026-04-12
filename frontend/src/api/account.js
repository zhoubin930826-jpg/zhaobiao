import request from '@/plugins/request';

/** 登录 OpenAPI: LoginRequest */
export function AccountLogin (data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    });
}

/** 注册 OpenAPI: RegisterRequest */
export function AccountRegister (data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    });
}

/** 当前用户 OpenAPI: GET /api/auth/me */
export function getUserInfo (params) {
    return request({
        url: '/auth/me',
        method: 'get',
        params
    });
}

/** 个人资料：PUT /api/profile，请求体与后端 ProfileUpdateRequest 字段一致（扁平 JSON） */
export function updateProfile (profileUpdateRequest) {
    return request({
        url: '/profile',
        method: 'put',
        data: profileUpdateRequest
    });
}

/** 兼容旧方法名：更新基本信息 */
export function updateinfo (data) {
    return updateProfile(data);
}

/** 修改密码：文档中 ProfileUpdateRequest 含 password / confirmPassword（无单独改密接口） */
export function Updatepwd ({ newPassword, confirmPassword }) {
    return updateProfile({
        password: newPassword,
        confirmPassword
    });
}

export function getuploadtoken (params) {
    return request({
        url: '/file/file/getuploadtoken',
        method: 'get',
        type: 'file',
        params
    });
}

export function saveFile (data) {
    return request({
        url: '/file/file/savefile',
        method: 'post',
        type: 'file',
        data
    });
}
