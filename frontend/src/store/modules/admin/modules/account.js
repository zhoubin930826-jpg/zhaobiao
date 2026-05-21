/**
 * 注册、登录、注销
 * */
import util from '@/libs/util';
import router from '@/router';
import { AccountLogin } from '@api/account';
import { listMenus } from '@api/system';
import { menuDtosToAuthTree } from '@/libs/bid-menu';

import { Modal } from 'view-design';

const fallbackAuthTree = [
    {
        id: '1',
        title: '系统管理',
        icon: '',
        fe_path: '/system',
        child: [
            { id: '4', title: '用户管理', icon: '', fe_path: '/system/user' },
            { id: '5', title: '角色管理', icon: '', fe_path: '/system/role' },
            { id: '6', title: '菜单管理', icon: '', fe_path: '/system/menu' }
        ]
    }
];

export default {
    namespaced: true,
    actions: {
        /**
         * @description 登录
         * @param {Object} param context
         * @param {Object} param username {String} 用户账号
         * @param {Object} param password {String} 密码
         * @param {Object} param route {Object} 登录成功后定向的路由对象 任何 vue-router 支持的格式
         */
        login({ dispatch }, {
            username = '',
            password = ''
        } = {}) {
            return new Promise((resolve, reject) => {
                // 开始请求登录接口
                AccountLogin({
                    username,
                    password
                })
                    .then(async res => {
                        // OpenAPI LoginResponse: { token, tokenType, expireSeconds, user }
                        util.cookies.set('token', res.token);
                        const profile = res.user || {};
                        const info = {
                            ...profile,
                            name: profile.realName || profile.username,
                            nickname: profile.realName || profile.username
                        };
                        await dispatch('admin/user/set', info, { root: true });
                        let tree = [];
                        // 登录返回的 user 信息里通常已经带有角色菜单树（避免额外请求 /admin/menus）

                        if (profile.menus && profile.menus.length) {
                            tree = menuDtosToAuthTree([
                                {
                                    "id": 4,
                                    "code": "DASHBOARD",
                                    "name": "工作台",
                                    "type": "MENU",
                                    "routePath": "/dashboard/console",
                                    "component": "dashboard/console",
                                    "icon": "House",
                                    "sortOrder": 10,
                                    "visible": true,
                                    "enabled": true,
                                    "description": "系统首页",
                                    "children": []
                                },
                                {
                                    "id": 6,
                                    "code": "SYSTEM_ROOT",
                                    "name": "系统管理",
                                    "type": "DIRECTORY",
                                    "routePath": "/system",
                                    "component": "",
                                    "icon": "Setting",
                                    "sortOrder": 30,
                                    "visible": true,
                                    "enabled": true,
                                    "description": "系统管理目录",
                                    "children": [
                                        {
                                            "id": 7,
                                            "code": "SYSTEM_ADMIN_USER",
                                            "name": "管理员管理",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/system/user",
                                            "component": "sys/user",
                                            "icon": "UserFilled",
                                            "sortOrder": 10,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "管理员账号管理页面",
                                            "children": [
                                                {
                                                    "id": 19,
                                                    "code": "ADMIN_USER_CREATE_BUTTON",
                                                    "name": "新增管理员按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 7,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 10,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "新增管理员按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 20,
                                                    "code": "ADMIN_USER_EDIT_BUTTON",
                                                    "name": "编辑管理员按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 7,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 20,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "编辑管理员按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 21,
                                                    "code": "ADMIN_USER_STATUS_BUTTON",
                                                    "name": "管理员状态按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 7,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 30,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "修改管理员状态按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 22,
                                                    "code": "ADMIN_USER_PASSWORD_BUTTON",
                                                    "name": "管理员重置密码按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 7,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 40,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "管理员重置密码按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 23,
                                                    "code": "ADMIN_USER_ROLE_BUTTON",
                                                    "name": "管理员角色按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 7,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 50,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "管理员角色按钮",
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "id": 8,
                                            "code": "SYSTEM_MEMBER_USER",
                                            "name": "会员管理",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/system/member",
                                            "component": "sys/member",
                                            "icon": "User",
                                            "sortOrder": 20,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "会员账号管理页面",
                                            "children": [
                                                {
                                                    "id": 17,
                                                    "code": "MEMBER_CREATE_BUTTON",
                                                    "name": "新增会员按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 8,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 5,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "新增会员按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 18,
                                                    "code": "MEMBER_EDIT_BUTTON",
                                                    "name": "编辑会员按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 8,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 8,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "编辑会员按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 24,
                                                    "code": "MEMBER_DOWNLOAD_BUTTON",
                                                    "name": "会员下载权限按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 8,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 10,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "会员下载权限按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 25,
                                                    "code": "MEMBER_STATUS_BUTTON",
                                                    "name": "会员状态按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 8,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 20,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "会员状态按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 26,
                                                    "code": "MEMBER_PASSWORD_BUTTON",
                                                    "name": "会员重置密码按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 8,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 30,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "会员重置密码按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 41,
                                                    "code": "MEMBER_DELETE_BUTTON",
                                                    "name": "删除会员按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 8,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 40,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "删除会员按钮",
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "id": 9,
                                            "code": "SYSTEM_BUSINESS_TYPE",
                                            "name": "类型管理",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/system/business-type",
                                            "component": "sys/business-type",
                                            "icon": "Collection",
                                            "sortOrder": 30,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "业务类型管理页面",
                                            "children": [
                                                {
                                                    "id": 27,
                                                    "code": "BUSINESS_TYPE_CREATE_BUTTON",
                                                    "name": "新增类型按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 9,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 10,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "新增类型按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 28,
                                                    "code": "BUSINESS_TYPE_EDIT_BUTTON",
                                                    "name": "编辑类型按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 9,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 20,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "编辑类型按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 29,
                                                    "code": "BUSINESS_TYPE_STATUS_BUTTON",
                                                    "name": "类型状态按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 9,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 30,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "类型状态按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 30,
                                                    "code": "BUSINESS_TYPE_DELETE_BUTTON",
                                                    "name": "删除类型按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 9,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 40,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "删除类型按钮",
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "id": 10,
                                            "code": "SYSTEM_TENDER",
                                            "name": "招标管理",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/tenders",
                                            "component": "sys/tender",
                                            "icon": "Document",
                                            "sortOrder": 40,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "招标信息管理页面",
                                            "children": [
                                                {
                                                    "id": 31,
                                                    "code": "TENDER_CREATE_BUTTON",
                                                    "name": "新增招标按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 10,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 10,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "新增招标按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 32,
                                                    "code": "TENDER_EDIT_BUTTON",
                                                    "name": "编辑招标按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 10,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 20,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "编辑招标按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 33,
                                                    "code": "TENDER_DELETE_BUTTON",
                                                    "name": "删除招标按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 10,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 30,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "删除招标按钮",
                                                    "children": []
                                                },
                                                {
                                                    "id": 34,
                                                    "code": "TENDER_UPLOAD_BUTTON",
                                                    "name": "上传附件按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 10,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 40,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "上传招标附件按钮",
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "id": 13,
                                            "code": "SYSTEM_ROLE",
                                            "name": "角色管理",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/system/role",
                                            "component": "sys/role",
                                            "icon": "Avatar",
                                            "sortOrder": 70,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "角色管理页面",
                                            "children": [
                                                {
                                                    "id": 37,
                                                    "code": "ROLE_EDIT_BUTTON",
                                                    "name": "角色维护按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 13,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 10,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "角色维护按钮",
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "id": 15,
                                            "code": "SYSTEM_MENU",
                                            "name": "菜单管理",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/system/menu",
                                            "component": "sys/menu",
                                            "icon": "Menu",
                                            "sortOrder": 90,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "菜单管理页面",
                                            "children": [
                                                {
                                                    "id": 39,
                                                    "code": "MENU_EDIT_BUTTON",
                                                    "name": "菜单维护按钮",
                                                    "type": "BUTTON",
                                                    "parentId": 15,
                                                    "routePath": "",
                                                    "component": "",
                                                    "icon": "",
                                                    "sortOrder": 10,
                                                    "visible": false,
                                                    "enabled": true,
                                                    "description": "菜单维护按钮",
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "id": 16,
                                            "code": "SYSTEM_OPERATION_LOG",
                                            "name": "操作日志",
                                            "type": "MENU",
                                            "parentId": 6,
                                            "routePath": "/log",
                                            "component": "system/log",
                                            "icon": "Tickets",
                                            "sortOrder": 100,
                                            "visible": true,
                                            "enabled": true,
                                            "description": "操作日志页面",
                                            "children": []
                                        }
                                    ]
                                }
                            ]);
                        } else {
                            try {
                                const resMenus = await listMenus();
                                tree = menuDtosToAuthTree(resMenus);
                            } catch (e) {
                                tree = [];
                            }
                        }
                        if (!tree.length) {
                            tree = fallbackAuthTree;
                        }
                        await dispatch('admin/user/setTree', tree, { root: true });
                        // 用户登录后从持久化数据加载一系列的设置
                        await dispatch('load');
                        // 结束
                        resolve();
                    })
                    .catch(err => {
                        // console.log('err: ', err);
                        reject(err);
                    })
            })
        },
        /**
         * @description 退出登录
         * */
        logout({ commit, dispatch }, { confirm = false, vm } = {}) {
            async function logout() {
                // 删除cookie
                util.cookies.remove('token');
                util.cookies.remove('uuid');
                // 清空 vuex 用户信息
                await dispatch('admin/user/set', {}, { root: true });
                await dispatch('admin/db/set', {
                    dbName: 'sys',
                    path: 'page.opened',
                    value: [],
                    user: true
                }, { root: true });
                // 清除路由信息
                await dispatch('admin/user/setTree', {}, { root: true });

                // 清空 vuex 用户信息
                await dispatch('admin/user/set', {}, { root: true });
                // 跳转路由
                router.push({
                    name: 'login'
                });
            }

            if (confirm) {
                Modal.confirm({
                    title: vm.$t('basicLayout.logout.confirmTitle'),
                    content: vm.$t('basicLayout.logout.confirmContent'),
                    onOk() {
                        logout();
                    }
                });
            } else {
                logout();
            }
        },
        /**
         * @description 用户登录后从持久化数据加载一系列的设置
         * @param {Object} state vuex state
         * @param {Object} dispatch vuex dispatch
         */
        load({ state, dispatch }) {
            return new Promise(async resolve => {
                // 加载用户登录信息
                await dispatch('admin/user/load', null, { root: true });
                // 持久化数据加载上次退出时的多页列表
                await dispatch('admin/page/openedLoad', null, { root: true });
                // end
                resolve();
            })
        }
    }
};
