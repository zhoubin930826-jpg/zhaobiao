/**
 * 注册、登录、注销
 * */
import util from '@/libs/util';
import router from '@/router';
import { AccountLogin} from '@api/account';
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
        login ({ dispatch }, {
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
                        try {
                            const resMenus = await listMenus();
                            tree = menuDtosToAuthTree(resMenus);
                        } catch (e) {
                            tree = [];
                        }
                        // if (!tree.length && profile.menus) {
                        //     tree = menuDtosToAuthTree(profile.menus);
                        // }
                        // if (!tree.length) {
                        //     tree = fallbackAuthTree;
                        // }
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
        logout ({ commit, dispatch }, { confirm = false, vm } = {}) {
            async function logout () {
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
                    onOk () {
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
        load ({ state, dispatch }) {
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
