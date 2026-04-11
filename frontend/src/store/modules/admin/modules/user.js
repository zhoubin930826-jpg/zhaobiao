/**
 * 用户信息
 * */
export default {
    namespaced: true,
    state: {
        // 用户信息
        info: {},
        AuthTree: []
    },
    mutations: {
        /**
         * @description 修改用户信息
         * @param {Object} state vuex state
         * @param {Boolean} info 用户信息
         * */
        setInfo (state, info) {
            state.info = info;
        }
    },
    actions: {
        /**
         * @description 设置用户数据
         * @param {Object} state vuex state
         * @param {Object} dispatch vuex dispatch
         * @param {*} info info
         */
        set ({ commit, dispatch }, info) {
            return new Promise(async resolve => {
                commit('setInfo', info);
                // 持久化
                await dispatch(
                    'admin/db/set',
                    {
                        dbName: 'sys',
                        path: 'user.info',
                        value: info,
                        user: true
                    },
                    { root: true }
                );
                // end
                resolve();
            });
        },
        setTree ({ state, dispatch }, AuthTree) {
            return new Promise(async resolve => {
                // store 赋值
                state.AuthTree = AuthTree;
                // 持久化
                await dispatch(
                    'admin/db/set',
                    {
                        dbName: 'sys',
                        path: 'user.AuthTree',
                        value: AuthTree,
                        user: true
                    },
                    { root: true }
                );
                // end
                resolve();
            });
        },

        /**
         * @description 从数据库取用户数据
         * @param {Object} state vuex state
         * @param {Object} dispatch vuex dispatch
         */
        load ({ state, dispatch }) {
            return new Promise(async resolve => {
                // store 赋值
                state.info = await dispatch(
                    'admin/db/get',
                    {
                        dbName: 'sys',
                        path: 'user.info',
                        defaultValue: {},
                        user: true
                    },
                    { root: true }
                );
                state.AuthTree = await dispatch(
                    'admin/db/get',
                    {
                        dbName: 'sys',
                        path: 'user.AuthTree',
                        defaultValue: {},
                        user: true
                    },
                    { root: true }
                );
                // end
                resolve();
            });
        }
    }
};
