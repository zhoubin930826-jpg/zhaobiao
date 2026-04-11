import BasicLayout from '@/layouts/basic-layout';

const meta = {
    auth: true
};

const pre = 'profile-';

export default {
    path: '/profile',
    name: 'profile',
    redirect: {
        name: `${pre}index`
    },
    meta,
    component: BasicLayout,
    children: [
        {
            path: 'index',
            name: `${pre}index`,
            meta: {
                ...meta,
                title: '个人中心'
            },
            component: () => import('@/pages/profile')
        }
    ]
};
