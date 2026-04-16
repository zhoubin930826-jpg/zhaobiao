import BasicLayout from '@/layouts/basic-layout';

const meta = {
    auth: true
};

const pre = 'system-';

export default {
    path: '/system',
    name: 'system',
    redirect: {
        name: `${pre}user`
    },
    meta,
    component: BasicLayout,
    children: [
        {
            path: 'user',
            name: `${pre}user`,
            meta: {
                ...meta,
                title: '管理员管理'
            },
            component: () => import('@/pages/sys/user')
        },
        {
            path: 'member',
            name: `${pre}member`,
            meta: {
                ...meta,
                title: '会员管理'
            },
            component: () => import('@/pages/sys/member')
        },
        {
            path: 'business-type',
            name: `${pre}business-type`,
            meta: {
                ...meta,
                title: '类型管理'
            },
            component: () => import('@/pages/sys/business-type')
        },
        {
            path: 'menu',
            name: `${pre}menu`,
            meta: {
                ...meta,
                title: '菜单权限管理'
            },
            component: () => import('@/pages/sys/menu')
        },
        {
            path: 'role',
            name: `${pre}role`,
            meta: {
                ...meta,
                title: '角色权限管理'
            },
            component: () => import('@/pages/sys/role')
        },
        {
            path: 'permissions',
            name: `${pre}permissions`,
            meta: {
                ...meta,
                title: '权限管理'
            },
            component: () => import('@/pages/sys/permissions')
        }
    ]
};
