import BasicLayout from '@/layouts/basic-layout';

const meta = {
    auth: true
};

const pre = 'tenders-';

export default {
    path: '/tenders',
    name: 'tenders',
    meta,
    component: BasicLayout,
    children: [
        {
            path: '',
            name: `${pre}index`,
            meta: {
                ...meta,
                title: '招标管理'
            },
            component: () => import('@/pages/sys/tender')
        }
    ]
};
