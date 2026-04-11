const pre = '/system/';

export default {
    path: '/system',
    title: '系统管理',
    header: 'home',
    icon: 'md-options',
    children: [
        {
            path: `${pre}user`,
            title: '管理员管理'
        },
        {
            path: `${pre}menu`,
            title: '权限管理'
        },
        {
            path: `${pre}role`,
            title: '角色管理'
        }
    ]
}
